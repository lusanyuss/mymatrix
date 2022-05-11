package com.youku.onetrace.plugin.trace

import com.youku.onetrace.javalib.util.Log
import com.youku.onetrace.plugin.compat.AgpCompat.Companion.asmApi
import com.youku.onetrace.plugin.trace.item.TraceMethod
import com.youku.onetrace.plugin.trace.retrace.MappingCollector
import org.objectweb.asm.*
import org.objectweb.asm.tree.MethodNode
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipFile


class MethodCollector(
    private val executor: ExecutorService,
    private val mappingCollector: MappingCollector,
    private val methodId: AtomicInteger,
    private val configuration: Configuration,
    val collectedMethodMap: ConcurrentHashMap<String?, TraceMethod?>
) {
    //className->superName
    val collectedClassExtendMap = ConcurrentHashMap<String?, String?>()
    
    //被忽略的方法搜集:名称->方法信息bean
    private val collectedIgnoreMethodMap = ConcurrentHashMap<String?, TraceMethod?>()
    private val ignoreCount = AtomicInteger()
    private val incrementCount = AtomicInteger()
    
    /**
     * 对所有的类进行插桩,并保存,收集的方法和忽略方法
     *
     * @param srcFolderList
     * @param dependencyJarList
     * @kotlin.jvm.Throws ExecutionException
     * @kotlin.jvm.Throws InterruptedException
     */
    @kotlin.jvm.Throws(ExecutionException::class, InterruptedException::class)
    fun collect(srcFolderList: Set<File>, dependencyJarList: Set<File>) {
        val futures: MutableList<Future<*>> = LinkedList()
        for (srcFile in srcFolderList) { //文件全部装到classFileList
            val classFileList = ArrayList<File>()
            if(srcFile.isDirectory) {
                listClassFiles(classFileList, srcFile)
            } else {
                classFileList.add(srcFile)
            }
            
            //对源码的所有的文件进行插桩
            for (classFile in classFileList) {
                futures.add(executor.submit(CollectSrcTask(classFile)))
            }
        }
        
        //对jar文件进行插桩
        for (jarFile in dependencyJarList) {
            futures.add(executor.submit(CollectJarTask(jarFile)))
        }
        for (future in futures) {
            future.get()
        }
        futures.clear()
        
        //保存忽略收集的方法
        futures.add(executor.submit { saveIgnoreCollectedMethod(mappingCollector) })
        
        //保存收集的方法
        futures.add(executor.submit { saveCollectedMethod(mappingCollector) })
        /**
         * 链表填满后就可以获取顺序结果
         */
        for (future in futures) {
            future.get()
        }
        futures.clear()
    }
    
    /**
     * 源码插桩线程
     */
    internal inner class CollectSrcTask(var classFile: File?) : Runnable {
        override fun run() {
            var `is`: InputStream? = null
            try {
                `is` = FileInputStream(classFile)
                val classReader = ClassReader(`is`)
                val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
                val visitor: ClassVisitor = TraceClassAdapter(asmApi, classWriter)
                classReader.accept(visitor, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    `is`!!.close()
                } catch (e: Exception) {
                }
            }
        }
    }
    
    /**
     * jar包插桩线程
     */
    internal inner class CollectJarTask(var fromJar: File) : Runnable {
        override fun run() {
            var zipFile: ZipFile? = null
            try {
                zipFile = ZipFile(fromJar)
                val enumeration = zipFile.entries()
                while (enumeration.hasMoreElements()) {
                    val zipEntry = enumeration.nextElement()
                    val zipEntryName = zipEntry.name
                    if(isNeedTraceFile(zipEntryName)) {
                        val inputStream = zipFile.getInputStream(zipEntry)
                        val classReader = ClassReader(inputStream)
                        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
                        val visitor: ClassVisitor = TraceClassAdapter(asmApi, classWriter)
                        classReader.accept(visitor, 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    zipFile!!.close()
                } catch (e: Exception) {
                    Log.e(TAG, "close stream err! fromJar:%s", fromJar.absolutePath)
                }
            }
        }
    }
    
    /**
     * 保存收集到的忽略的收集到的方法
     *
     * @param mappingCollector
     */
    private fun saveIgnoreCollectedMethod(mappingCollector: MappingCollector) {
        val methodMapFile = File(configuration.ignoreMethodMapFilePath!!)
        if(!methodMapFile.parentFile.exists()) {
            methodMapFile.parentFile.mkdirs()
        }
        val ignoreMethodList: MutableList<TraceMethod?> = ArrayList()
        ignoreMethodList.addAll(collectedIgnoreMethodMap.values)
        Log.i(TAG, "[saveIgnoreCollectedMethod] size:%s path:%s", collectedIgnoreMethodMap.size, methodMapFile.absolutePath)
        
        //排序
        Collections.sort(ignoreMethodList) { o1, o2 -> o1?.mClassName!!.compareTo(o2?.mClassName!!) }
        
        //输出文件
        var pw: PrintWriter? = null
        try {
            val fileOutputStream = FileOutputStream(methodMapFile, false)
            val w: Writer = OutputStreamWriter(fileOutputStream, "UTF-8")
            pw = PrintWriter(w)
            pw.println("ignore methods:")
            for (traceMethod in ignoreMethodList) {
                traceMethod?.revert(mappingCollector)
                pw.println(traceMethod?.toIgnoreString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "write method map Exception:%s", e.message)
            e.printStackTrace()
        } finally {
            if(pw != null) {
                pw.flush()
                pw.close()
            }
        }
    }
    
    /**
     * 保存混淆方法
     *
     * @param mappingCollector
     */
    private fun saveCollectedMethod(mappingCollector: MappingCollector) {
        val methodMapFile = File(configuration.methodMapFilePath)
        if(!methodMapFile.parentFile.exists()) {
            methodMapFile.parentFile.mkdirs()
        }
        val methodList: MutableList<TraceMethod?> = ArrayList()
        
        /**
         * handler的dispatchMessage也要加打点跟踪方法
         */
        val extra: TraceMethod = TraceMethod.Companion.create(
            TraceBuildConstants.METHOD_ID_DISPATCH, Opcodes.ACC_PUBLIC, "android.os.Handler", "dispatchMessage", "(Landroid.os.Message;)V"
        )
        collectedMethodMap[extra.getMethodName()] = extra
        methodList.addAll(collectedMethodMap.values)
        Log.i(TAG, "[saveCollectedMethod] size:%s incrementCount:%s path:%s", collectedMethodMap.size, incrementCount.get(), methodMapFile.absolutePath)
        Collections.sort(methodList) { o1, o2 -> o1?.mId!! - o2?.mId!! }
        var pw: PrintWriter? = null
        try {
            val fileOutputStream = FileOutputStream(methodMapFile, false)
            val w: Writer = OutputStreamWriter(fileOutputStream, "UTF-8")
            pw = PrintWriter(w)
            for (traceMethod in methodList) {
                traceMethod?.revert(mappingCollector)
                pw.println(traceMethod.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "write method map Exception:%s", e.message)
            e.printStackTrace()
        } finally {
            if(pw != null) {
                pw.flush()
                pw.close()
            }
        }
    }
    
    private inner class TraceClassAdapter internal constructor(i: Int, classVisitor: ClassVisitor?) : ClassVisitor(i, classVisitor) {
        private var className: String? = null
        private var isABSClass = false
        private var hasWindowFocusMethod = false
        override fun visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array<String>) {
            super.visit(version, access, name, signature, superName, interfaces)
            className = name
            if(access and Opcodes.ACC_ABSTRACT > 0 || access and Opcodes.ACC_INTERFACE > 0) {
                isABSClass = true
            } //收集类和父类映射关系
            collectedClassExtendMap[className!!] = superName
        }
        
        override fun visitMethod(
            access: Int, name: String, desc: String, signature: String, exceptions: Array<String>
        ): MethodVisitor {
            return if(isABSClass) {
                super.visitMethod(access, name, desc, signature, exceptions)
            } else {
                if(!hasWindowFocusMethod) {
                    hasWindowFocusMethod = isWindowFocusChangeMethod(name, desc)
                } //收集混淆和不混淆方法的映射关系
                CollectMethodNode(className, access, name, desc, signature, exceptions)
            }
        }
    }
    
    private inner class CollectMethodNode internal constructor(
        private val className: String?, access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<String>?
    ) : MethodNode(asmApi, access, name, desc, signature, exceptions) {
        private var isConstructor = false
        override fun visitEnd() {
            super.visitEnd()
            val traceMethod: TraceMethod = TraceMethod.Companion.create(0, access, className, name, desc)
            if("<init>" == name) {
                isConstructor = true
            }
            val isNeedTrace = isNeedTrace(configuration, traceMethod.mClassName, mappingCollector) // filter simple methods
            if((isEmptyMethod || isGetSetMethod || isSingleMethod) && isNeedTrace) {
                ignoreCount.incrementAndGet()
                collectedIgnoreMethodMap[traceMethod.getMethodName()] = traceMethod
                return
            }
            if(isNeedTrace && !collectedMethodMap.containsKey(traceMethod.getMethodName())) {
                traceMethod.mId = methodId.incrementAndGet()
                collectedMethodMap[traceMethod.getMethodName()] = traceMethod
                incrementCount.incrementAndGet()
            } else if(!isNeedTrace && !collectedIgnoreMethodMap.containsKey(traceMethod.mClassName)) {
                ignoreCount.incrementAndGet()
                collectedIgnoreMethodMap[traceMethod.getMethodName()] = traceMethod
            }
        }
        
        /**
         * get或者set方法
         *
         * @return
         */
        private val isGetSetMethod: Boolean
            private get() {
                var ignoreCount = 0
                val iterator = instructions.iterator()
                while (iterator.hasNext()) {
                    val insnNode = iterator.next()
                    val opcode = insnNode.opcode
                    if(-1 == opcode) {
                        continue
                    }
                    if(opcode != Opcodes.GETFIELD && opcode != Opcodes.GETSTATIC && opcode != Opcodes.H_GETFIELD && opcode != Opcodes.H_GETSTATIC && opcode != Opcodes.RETURN && opcode != Opcodes.ARETURN && opcode != Opcodes.DRETURN && opcode != Opcodes.FRETURN && opcode != Opcodes.LRETURN && opcode != Opcodes.IRETURN && opcode != Opcodes.PUTFIELD && opcode != Opcodes.PUTSTATIC && opcode != Opcodes.H_PUTFIELD && opcode != Opcodes.H_PUTSTATIC && opcode > Opcodes.SALOAD) {
                        if(isConstructor && opcode == Opcodes.INVOKESPECIAL) {
                            ignoreCount++
                            if(ignoreCount > 1) {
                                return false
                            }
                            continue
                        }
                        return false
                    }
                }
                return true
            }
        private val isSingleMethod: Boolean
            private get() {
                val iterator = instructions.iterator()
                while (iterator.hasNext()) {
                    val insnNode = iterator.next()
                    val opcode = insnNode.opcode
                    if(-1 == opcode) {
                        continue
                    } else if(Opcodes.INVOKEVIRTUAL <= opcode && opcode <= Opcodes.INVOKEDYNAMIC) {
                        return false
                    }
                }
                return true
            }
        
        /**
         * 空方法
         *
         * @return
         */
        private val isEmptyMethod: Boolean
            private get() {
                val iterator = instructions.iterator()
                while (iterator.hasNext()) {
                    val insnNode = iterator.next()
                    val opcode = insnNode.opcode
                    return if(-1 == opcode) {
                        continue
                    } else {
                        false
                    }
                }
                return true
            }
    }
    
    private fun listClassFiles(classFiles: ArrayList<File>, folder: File) {
        val files = folder.listFiles()
        if(null == files) {
            Log.e(TAG, "[listClassFiles] files is null! %s", folder.absolutePath)
            return
        }
        for (file in files) {
            if(file == null) {
                continue
            }
            if(file.isDirectory) {
                listClassFiles(classFiles, file)
            } else if(isNeedTraceFile(file.name)) {
                classFiles.add(file)
            }
        }
    }
    
    companion object {
        const val TAG = "MethodCollector"
        fun isWindowFocusChangeMethod(name: String?, desc: String?): Boolean {
            return null != name && null != desc && name == TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD && desc == TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS
        }
        
        /**
         * 是否需要跟踪
         *
         * @param configuration
         * @param clsName
         * @param mappingCollector
         * @return
         */
        fun isNeedTrace(configuration: Configuration, clsName: String?, mappingCollector: MappingCollector?): Boolean {
            var clsName = clsName
            var isNeed = true
            if(configuration.blockSet.contains(clsName)) {
                isNeed = false
            } else {
                if(null != mappingCollector) {
                    clsName = mappingCollector.originalClassName(clsName, clsName)
                }
                clsName = clsName!!.replace("/".toRegex(), ".")
                for (packageName in configuration.blockSet) {
                    if(clsName.startsWith(packageName!!.replace("/".toRegex(), "."))) {
                        isNeed = false
                        break
                    }
                }
            }
            return isNeed
        }
        
        /**
         * 过滤系统的一些文件:"R.class", "R$", "Manifest", "BuildConfig"
         *
         * @param fileName
         * @return
         */
        fun isNeedTraceFile(fileName: String): Boolean {
            if(fileName.endsWith(".class")) {
                for (unTraceCls in TraceBuildConstants.UN_TRACE_CLASS) {
                    if(fileName.contains(unTraceCls!!)) {
                        return false
                    }
                }
            } else {
                return false
            }
            return true
        }
    }
}