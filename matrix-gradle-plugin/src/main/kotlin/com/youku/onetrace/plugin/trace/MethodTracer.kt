package com.youku.onetrace.plugin.trace

import com.youku.onetrace.javalib.util.FileUtil
import com.youku.onetrace.javalib.util.Log
import com.youku.onetrace.javalib.util.Util
import com.youku.onetrace.plugin.compat.AgpCompat.Companion.asmApi
import com.youku.onetrace.plugin.trace.item.TraceMethod
import com.youku.onetrace.plugin.trace.retrace.MappingCollector
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.util.CheckClassAdapter
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


/**
 *
 *
 * This class hooks all collected methods in oder to trace method in/out.
 *
 */
class MethodTracer(
    private val executor: ExecutorService,
    private val mappingCollector: MappingCollector,
    private val configuration: Configuration,
    private val collectedMethodMap: ConcurrentHashMap<String?, TraceMethod?>,
    private val collectedClassExtendMap: ConcurrentHashMap<String?, String?>
) {
    @Volatile
    private var traceError = false
    
    @kotlin.jvm.Throws(ExecutionException::class, InterruptedException::class)
    fun trace(srcFolderList: Map<File, File>?, dependencyJarList: Map<File, File>?, classLoader: ClassLoader, ignoreCheckClass: Boolean) {
        val futures: MutableList<Future<*>> = LinkedList()
        traceMethodFromSrc(srcFolderList, futures, classLoader, ignoreCheckClass)
        traceMethodFromJar(dependencyJarList, futures, classLoader, ignoreCheckClass)
        for (future in futures) {
            future.get()
        }
        require(!traceError) { "something wrong with trace, see detail log before" }
        futures.clear()
    }
    
    private fun traceMethodFromSrc(srcMap: Map<File, File>?, futures: MutableList<Future<*>>, classLoader: ClassLoader, skipCheckClass: Boolean) {
        if(null != srcMap) {
            for ((key, value) in srcMap) {
                futures.add(executor.submit { innerTraceMethodFromSrc(key, value, classLoader, skipCheckClass) })
            }
        }
    }
    
    private fun traceMethodFromJar(dependencyMap: Map<File, File>?, futures: MutableList<Future<*>>, classLoader: ClassLoader, skipCheckClass: Boolean) {
        if(null != dependencyMap) {
            for ((key, value) in dependencyMap) {
                futures.add(executor.submit { innerTraceMethodFromJar(key, value, classLoader, skipCheckClass) })
            }
        }
    }
    
    private fun innerTraceMethodFromSrc(input: File, output: File, classLoader: ClassLoader, ignoreCheckClass: Boolean) {
        val classFileList = ArrayList<File>()
        if(input.isDirectory) {
            listClassFiles(classFileList, input)
        } else {
            classFileList.add(input)
        }
        for (classFile in classFileList) {
            var `is`: InputStream? = null
            var os: FileOutputStream? = null
            try {
                val changedFileInputFullPath = classFile.absolutePath
                val changedFileOutput = File(changedFileInputFullPath.replace(input.absolutePath, output.absolutePath))
                if(changedFileOutput.canonicalPath == classFile.canonicalPath) {
                    throw RuntimeException("Input file(" + classFile.canonicalPath + ") should not be same with output!")
                }
                if(!changedFileOutput.exists()) {
                    changedFileOutput.parentFile.mkdirs()
                }
                changedFileOutput.createNewFile()
                if(MethodCollector.Companion.isNeedTraceFile(classFile.name)) {
                    `is` = FileInputStream(classFile)
                    val classReader = ClassReader(`is`)
                    val classWriter: ClassWriter = TraceClassWriter(ClassWriter.COMPUTE_FRAMES, classLoader)
                    val classVisitor: ClassVisitor = TraceClassAdapter(asmApi, classWriter)
                    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                    `is`.close()
                    val data = classWriter.toByteArray()
                    if(!ignoreCheckClass) {
                        try {
                            val cr = ClassReader(data)
                            val cw = ClassWriter(0)
                            val check: ClassVisitor = CheckClassAdapter(cw)
                            cr.accept(check, ClassReader.EXPAND_FRAMES)
                        } catch (e: Throwable) {
                            System.err.println("trace output ERROR : " + e.message + ", " + classFile)
                            traceError = true
                        }
                    }
                    os = if(output.isDirectory) {
                        FileOutputStream(changedFileOutput)
                    } else {
                        FileOutputStream(output)
                    }
                    os.write(data)
                    os.close()
                } else {
                    FileUtil.copyFileUsingStream(classFile, changedFileOutput)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[innerTraceMethodFromSrc] input:%s e:%s", input.name, e.message)
                try {
                    Files.copy(input.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }
            } finally {
                try {
                    `is`!!.close()
                    os!!.close()
                } catch (e: Exception) { // ignore
                }
            }
        }
    }
    
    private fun innerTraceMethodFromJar(input: File, output: File, classLoader: ClassLoader, skipCheckClass: Boolean) {
        var zipOutputStream: ZipOutputStream? = null
        var zipFile: ZipFile? = null
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(output))
            zipFile = ZipFile(input)
            val enumeration = zipFile.entries()
            while (enumeration.hasMoreElements()) {
                val zipEntry = enumeration.nextElement()
                val zipEntryName = zipEntry.name
                if(Util.preventZipSlip(output, zipEntryName)) {
                    Log.e(TAG, "Unzip entry %s failed!", zipEntryName)
                    continue
                }
                if(MethodCollector.Companion.isNeedTraceFile(zipEntryName)) {
                    val inputStream = zipFile.getInputStream(zipEntry)
                    val classReader = ClassReader(inputStream)
                    val classWriter: ClassWriter = TraceClassWriter(ClassWriter.COMPUTE_FRAMES, classLoader)
                    val classVisitor: ClassVisitor = TraceClassAdapter(asmApi, classWriter)
                    classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                    val data = classWriter.toByteArray() //
                    if(!skipCheckClass) {
                        try {
                            val r = ClassReader(data)
                            val w = ClassWriter(0)
                            val v: ClassVisitor = CheckClassAdapter(w)
                            r.accept(v, ClassReader.EXPAND_FRAMES)
                        } catch (e: Throwable) {
                            System.err.println("trace jar output ERROR: " + e.message + ", " + zipEntryName) //                        e.printStackTrace();
                            traceError = true
                        }
                    }
                    val byteArrayInputStream: InputStream = ByteArrayInputStream(data)
                    val newZipEntry = ZipEntry(zipEntryName)
                    FileUtil.addZipEntry(zipOutputStream, newZipEntry, byteArrayInputStream)
                } else {
                    val inputStream = zipFile.getInputStream(zipEntry)
                    val newZipEntry = ZipEntry(zipEntryName)
                    FileUtil.addZipEntry(zipOutputStream, newZipEntry, inputStream)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[innerTraceMethodFromJar] input:%s output:%s e:%s", input, output, e.message)
            (e as? ZipException)?.printStackTrace()
            try {
                if(input.length() > 0) {
                    Files.copy(input.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Log.e(TAG, "[innerTraceMethodFromJar] input:%s is empty", input)
                }
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
        } finally {
            try {
                if(zipOutputStream != null) {
                    zipOutputStream.finish()
                    zipOutputStream.flush()
                    zipOutputStream.close()
                }
                zipFile?.close()
            } catch (e: Exception) {
                Log.e(TAG, "close stream err!")
            }
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
            } else {
                if(null != file && file.isFile) {
                    classFiles.add(file)
                }
            }
        }
    }
    
    private inner class TraceClassAdapter internal constructor(i: Int, classVisitor: ClassVisitor?) : ClassVisitor(i, classVisitor) {
        private var className: String? = null
        private var superName: String? = null
        private var isABSClass = false
        private var hasWindowFocusMethod = false
        private var isActivityOrSubClass = false
        private var isNeedTrace = false
        override fun visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array<String>) {
            super.visit(version, access, name, signature, superName, interfaces)
            className = name
            this.superName = superName
            isActivityOrSubClass = isActivityOrSubClass(className, collectedClassExtendMap)
            isNeedTrace = MethodCollector.Companion.isNeedTrace(configuration, className, mappingCollector) //接口或者抽象类
            if(access and Opcodes.ACC_ABSTRACT > 0 || access and Opcodes.ACC_INTERFACE > 0) {
                isABSClass = true
            }
        }
        
        override fun visitMethod(
            access: Int, name: String, desc: String, signature: String, exceptions: Array<String>
        ): MethodVisitor {
            if(!hasWindowFocusMethod) {
                hasWindowFocusMethod = MethodCollector.Companion.isWindowFocusChangeMethod(name, desc)
            }
            return if(isABSClass) { //过滤接口或者抽象类
                super.visitMethod(access, name, desc, signature, exceptions)
            } else {
                val methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)
                TraceMethodAdapter(
                    api, methodVisitor, access, name, desc, className, hasWindowFocusMethod, isActivityOrSubClass, isNeedTrace
                )
            }
        }
        
        override fun visitEnd() {
            if(!hasWindowFocusMethod && isActivityOrSubClass && isNeedTrace) {
                insertWindowFocusChangeMethod(cv, className, superName)
            }
            super.visitEnd()
        }
    }
    
    private inner class TraceMethodAdapter(
        api: Int, mv: MethodVisitor?, access: Int, name: String?, desc: String?, className: String?, hasWindowFocusMethod: Boolean, isActivityOrSubClass: Boolean, isNeedTrace: Boolean
    ) : AdviceAdapter(api, mv, access, name, desc) {
        private val methodName: String?
        private val mName: String?
        private val className: String?
        private val hasWindowFocusMethod: Boolean
        private val isNeedTrace: Boolean
        private val isActivityOrSubClass: Boolean
        override fun onMethodEnter() {
            val traceMethod = collectedMethodMap[methodName]
            if(traceMethod != null) {
                traceMethodCount.incrementAndGet()
                mv.visitLdcInsn(traceMethod.mId)
                mv.visitMethodInsn(INVOKESTATIC, TraceBuildConstants.APM_TRACE_CLASS, "i", "(I)V", false)
                if(checkNeedTraceWindowFocusChangeMethod(traceMethod)) {
                    traceWindowFocusChangeMethod(mv, className)
                }
            }
        }
        
        override fun onMethodExit(opcode: Int) {
            val traceMethod = collectedMethodMap[methodName]
            if(traceMethod != null) {
                traceMethodCount.incrementAndGet()
                mv.visitLdcInsn(traceMethod.mId)
                mv.visitMethodInsn(INVOKESTATIC, TraceBuildConstants.APM_TRACE_CLASS, "o", "(I)V", false)
            }
        }
        
        private fun checkNeedTraceWindowFocusChangeMethod(traceMethod: TraceMethod): Boolean {
            if(hasWindowFocusMethod && isActivityOrSubClass && isNeedTrace) {
                val windowFocusChangeMethod: TraceMethod = TraceMethod.Companion.create(
                    -1, ACC_PUBLIC, className, TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD, TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS
                )
                if(windowFocusChangeMethod == traceMethod) {
                    return true
                }
            }
            return false
        }
        
        init {
            val traceMethod: TraceMethod = TraceMethod.Companion.create(0, access, className, name, desc)
            methodName = traceMethod.getMethodName()
            this.hasWindowFocusMethod = hasWindowFocusMethod
            this.className = className
            this.mName = name
            this.isActivityOrSubClass = isActivityOrSubClass
            this.isNeedTrace = isNeedTrace
        }
    }
    
    /**
     * 判断activiy的子类
     *
     * @param className
     * @param mCollectedClassExtendMap
     * @return
     */
    private fun isActivityOrSubClass(className: String?, mCollectedClassExtendMap: ConcurrentHashMap<String?, String?>): Boolean {
        var className = className
        className = className!!.replace(".", "/")
        val isActivity =
            className == TraceBuildConstants.APM_TRACE_ACTIVITY_CLASS || className == TraceBuildConstants.APM_TRACE_V4_ACTIVITY_CLASS || className == TraceBuildConstants.APM_TRACE_V7_ACTIVITY_CLASS || className == TraceBuildConstants.APM_TRACE_ANDROIDX_ACTIVITY_CLASS
        return if(isActivity) {
            true
        } else {
            if(!mCollectedClassExtendMap.containsKey(className)) {
                false
            } else {
                isActivityOrSubClass(mCollectedClassExtendMap[className], mCollectedClassExtendMap)
            }
        }
    }
    
    private fun traceWindowFocusChangeMethod(mv: MethodVisitor, classname: String?) {
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitVarInsn(Opcodes.ILOAD, 1)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, TraceBuildConstants.APM_TRACE_CLASS, "at", "(Landroid/app/Activity;Z)V", false)
    }
    
    /**
     * 给activity加入  WindowFocusChangeMethod 方法
     *
     * @param cv
     * @param classname
     * @param superClassName
     */
    private fun insertWindowFocusChangeMethod(cv: ClassVisitor, classname: String?, superClassName: String?) {
        val methodVisitor = cv.visitMethod(
            Opcodes.ACC_PUBLIC, TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD, TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS, null, null
        )
        methodVisitor.visitCode()
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        methodVisitor.visitVarInsn(Opcodes.ILOAD, 1)
        methodVisitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL, superClassName, TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD, TraceBuildConstants.APM_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS, false
        )
        traceWindowFocusChangeMethod(methodVisitor, classname)
        methodVisitor.visitInsn(Opcodes.RETURN)
        methodVisitor.visitMaxs(2, 2)
        methodVisitor.visitEnd()
    }
    
    companion object {
        private const val TAG = "Apm.MethodTracer"
        private val traceMethodCount = AtomicInteger()
    }
}