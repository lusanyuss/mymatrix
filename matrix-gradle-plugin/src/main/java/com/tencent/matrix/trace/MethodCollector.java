package com.tencent.matrix.trace;

import com.tencent.matrix.javalib.util.Log;
import com.tencent.matrix.plugin.compat.AgpCompat;
import com.tencent.matrix.trace.item.TraceMethod;
import com.tencent.matrix.trace.retrace.MappingCollector;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MethodCollector {

    private static final String TAG = "MethodCollector";

    private final ExecutorService executor;
    private final MappingCollector mappingCollector;


    //className->superName
    private final ConcurrentHashMap<String, String> collectedClassExtendMap = new ConcurrentHashMap<>();
    //被忽略的方法搜集:名称->方法信息bean
    private final ConcurrentHashMap<String, TraceMethod> collectedIgnoreMethodMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TraceMethod> collectedMethodMap;
    private final Configuration configuration;
    private final AtomicInteger methodId;
    private final AtomicInteger ignoreCount = new AtomicInteger();
    private final AtomicInteger incrementCount = new AtomicInteger();

    public MethodCollector(ExecutorService executor, MappingCollector mappingCollector, AtomicInteger methodId,
                           Configuration configuration, ConcurrentHashMap<String, TraceMethod> collectedMethodMap) {
        this.executor = executor;
        this.mappingCollector = mappingCollector;
        this.configuration = configuration;
        this.methodId = methodId;
        this.collectedMethodMap = collectedMethodMap;
    }

    public ConcurrentHashMap<String, String> getCollectedClassExtendMap() {
        return collectedClassExtendMap;
    }

    public ConcurrentHashMap<String, TraceMethod> getCollectedMethodMap() {
        return collectedMethodMap;
    }

    /**
     * 对所有的类进行插桩,并保存,收集的方法和忽略方法
     *
     * @param srcFolderList
     * @param dependencyJarList
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void collect(Set<File> srcFolderList, Set<File> dependencyJarList) throws ExecutionException, InterruptedException {
        List<Future> futures = new LinkedList<>();

        for (File srcFile : srcFolderList) {
            //文件全部装到classFileList
            ArrayList<File> classFileList = new ArrayList<>();
            if (srcFile.isDirectory()) {
                listClassFiles(classFileList, srcFile);
            } else {
                classFileList.add(srcFile);
            }

            //对源码的所有的文件进行插桩
            for (File classFile : classFileList) {
                futures.add(executor.submit(new CollectSrcTask(classFile)));
            }
        }

        //对jar文件进行插桩
        for (File jarFile : dependencyJarList) {
            futures.add(executor.submit(new CollectJarTask(jarFile)));
        }

        for (Future future : futures) {
            future.get();
        }
        futures.clear();

        //保存忽略收集的方法
        futures.add(executor.submit(new Runnable() {
            @Override
            public void run() {
                saveIgnoreCollectedMethod(mappingCollector);
            }
        }));

        //保存收集的方法
        futures.add(executor.submit(new Runnable() {
            @Override
            public void run() {
                saveCollectedMethod(mappingCollector);
            }
        }));

        /**
         * 链表填满后就可以获取顺序结果
         */
        for (Future future : futures) {
            future.get();
        }
        futures.clear();

    }


    /**
     * 源码插桩线程
     */
    class CollectSrcTask implements Runnable {

        File classFile;

        CollectSrcTask(File classFile) {
            this.classFile = classFile;
        }

        @Override
        public void run() {
            InputStream is = null;
            try {
                is = new FileInputStream(classFile);
                ClassReader classReader = new ClassReader(is);
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                ClassVisitor visitor = new TraceClassAdapter(AgpCompat.getAsmApi(), classWriter);
                classReader.accept(visitor, 0);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * jar包插桩线程
     */
    class CollectJarTask implements Runnable {

        File fromJar;

        CollectJarTask(File jarFile) {
            this.fromJar = jarFile;
        }

        @Override
        public void run() {
            ZipFile zipFile = null;

            try {
                zipFile = new ZipFile(fromJar);
                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                while (enumeration.hasMoreElements()) {
                    ZipEntry zipEntry = enumeration.nextElement();
                    String zipEntryName = zipEntry.getName();
                    if (isNeedTraceFile(zipEntryName)) {
                        InputStream inputStream = zipFile.getInputStream(zipEntry);
                        ClassReader classReader = new ClassReader(inputStream);
                        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                        ClassVisitor visitor = new TraceClassAdapter(AgpCompat.getAsmApi(), classWriter);
                        classReader.accept(visitor, 0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    zipFile.close();
                } catch (Exception e) {
                    Log.e(TAG, "close stream err! fromJar:%s", fromJar.getAbsolutePath());
                }
            }
        }
    }


    /**
     * 保存收集到的忽略的收集到的方法
     *
     * @param mappingCollector
     */
    private void saveIgnoreCollectedMethod(MappingCollector mappingCollector) {

        File methodMapFile = new File(configuration.ignoreMethodMapFilePath);
        if (!methodMapFile.getParentFile().exists()) {
            methodMapFile.getParentFile().mkdirs();
        }
        List<TraceMethod> ignoreMethodList = new ArrayList<>();
        ignoreMethodList.addAll(collectedIgnoreMethodMap.values());
        Log.i(TAG, "[saveIgnoreCollectedMethod] size:%s path:%s", collectedIgnoreMethodMap.size(), methodMapFile.getAbsolutePath());

        //排序
        Collections.sort(ignoreMethodList, new Comparator<TraceMethod>() {
            @Override
            public int compare(TraceMethod o1, TraceMethod o2) {
                return o1.className.compareTo(o2.className);
            }
        });

        //输出文件
        PrintWriter pw = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(methodMapFile, false);
            Writer w = new OutputStreamWriter(fileOutputStream, "UTF-8");
            pw = new PrintWriter(w);
            pw.println("ignore methods:");
            for (TraceMethod traceMethod : ignoreMethodList) {
                traceMethod.revert(mappingCollector);
                pw.println(traceMethod.toIgnoreString());
            }
        } catch (Exception e) {
            Log.e(TAG, "write method map Exception:%s", e.getMessage());
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }

    /**
     * 保存混淆方法
     *
     * @param mappingCollector
     */
    private void saveCollectedMethod(MappingCollector mappingCollector) {
        File methodMapFile = new File(configuration.methodMapFilePath);
        if (!methodMapFile.getParentFile().exists()) {
            methodMapFile.getParentFile().mkdirs();
        }
        List<TraceMethod> methodList = new ArrayList<>();

        /**
         * handler的dispatchMessage也要加打点跟踪方法
         */
        TraceMethod extra = TraceMethod.create(TraceBuildConstants.METHOD_ID_DISPATCH, Opcodes.ACC_PUBLIC, "android.os.Handler",
                "dispatchMessage", "(Landroid.os.Message;)V");
        collectedMethodMap.put(extra.getMethodName(), extra);

        methodList.addAll(collectedMethodMap.values());

        Log.i(TAG, "[saveCollectedMethod] size:%s incrementCount:%s path:%s", collectedMethodMap.size(), incrementCount.get(), methodMapFile.getAbsolutePath());

        Collections.sort(methodList, new Comparator<TraceMethod>() {
            @Override
            public int compare(TraceMethod o1, TraceMethod o2) {
                return o1.id - o2.id;
            }
        });

        PrintWriter pw = null;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(methodMapFile, false);
            Writer w = new OutputStreamWriter(fileOutputStream, "UTF-8");
            pw = new PrintWriter(w);
            for (TraceMethod traceMethod : methodList) {
                traceMethod.revert(mappingCollector);
                pw.println(traceMethod.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "write method map Exception:%s", e.getMessage());
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }

    private class TraceClassAdapter extends ClassVisitor {
        private String className;
        private boolean isABSClass = false;
        private boolean hasWindowFocusMethod = false;

        TraceClassAdapter(int i, ClassVisitor classVisitor) {
            super(i, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.className = name;
            if ((access & Opcodes.ACC_ABSTRACT) > 0 || (access & Opcodes.ACC_INTERFACE) > 0) {
                this.isABSClass = true;
            }
            //收集类和父类映射关系
            collectedClassExtendMap.put(className, superName);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            if (isABSClass) {
                return super.visitMethod(access, name, desc, signature, exceptions);
            } else {
                if (!hasWindowFocusMethod) {
                    hasWindowFocusMethod = isWindowFocusChangeMethod(name, desc);
                }
                //收集混淆和不混淆方法的映射关系
                return new CollectMethodNode(className, access, name, desc, signature, exceptions);
            }
        }
    }

    private class CollectMethodNode extends MethodNode {
        private String className;
        private boolean isConstructor;


        CollectMethodNode(String className, int access, String name, String desc,
                          String signature, String[] exceptions) {
            super(AgpCompat.getAsmApi(), access, name, desc, signature, exceptions);
            this.className = className;
        }

        @Override
        public void visitEnd() {
            super.visitEnd();
            TraceMethod traceMethod = TraceMethod.create(0, access, className, name, desc);

            if ("<init>".equals(name)) {
                isConstructor = true;
            }

            boolean isNeedTrace = isNeedTrace(configuration, traceMethod.className, mappingCollector);
            // filter simple methods
            if ((isEmptyMethod() || isGetSetMethod() || isSingleMethod())
                    && isNeedTrace) {
                ignoreCount.incrementAndGet();
                collectedIgnoreMethodMap.put(traceMethod.getMethodName(), traceMethod);
                return;
            }

            if (isNeedTrace && !collectedMethodMap.containsKey(traceMethod.getMethodName())) {
                traceMethod.id = methodId.incrementAndGet();
                collectedMethodMap.put(traceMethod.getMethodName(), traceMethod);
                incrementCount.incrementAndGet();
            } else if (!isNeedTrace && !collectedIgnoreMethodMap.containsKey(traceMethod.className)) {
                ignoreCount.incrementAndGet();
                collectedIgnoreMethodMap.put(traceMethod.getMethodName(), traceMethod);
            }

        }

        /**
         * get或者set方法
         *
         * @return
         */
        private boolean isGetSetMethod() {
            int ignoreCount = 0;
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insnNode = iterator.next();
                int opcode = insnNode.getOpcode();
                if (-1 == opcode) {
                    continue;
                }
                if (opcode != Opcodes.GETFIELD
                        && opcode != Opcodes.GETSTATIC
                        && opcode != Opcodes.H_GETFIELD
                        && opcode != Opcodes.H_GETSTATIC

                        && opcode != Opcodes.RETURN
                        && opcode != Opcodes.ARETURN
                        && opcode != Opcodes.DRETURN
                        && opcode != Opcodes.FRETURN
                        && opcode != Opcodes.LRETURN
                        && opcode != Opcodes.IRETURN

                        && opcode != Opcodes.PUTFIELD
                        && opcode != Opcodes.PUTSTATIC
                        && opcode != Opcodes.H_PUTFIELD
                        && opcode != Opcodes.H_PUTSTATIC
                        && opcode > Opcodes.SALOAD) {
                    if (isConstructor && opcode == Opcodes.INVOKESPECIAL) {
                        ignoreCount++;
                        if (ignoreCount > 1) {
                            return false;
                        }
                        continue;
                    }
                    return false;
                }
            }
            return true;
        }

        private boolean isSingleMethod() {
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insnNode = iterator.next();
                int opcode = insnNode.getOpcode();
                if (-1 == opcode) {
                    continue;
                } else if (Opcodes.INVOKEVIRTUAL <= opcode && opcode <= Opcodes.INVOKEDYNAMIC) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 空方法
         *
         * @return
         */
        private boolean isEmptyMethod() {
            ListIterator<AbstractInsnNode> iterator = instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insnNode = iterator.next();
                int opcode = insnNode.getOpcode();
                if (-1 == opcode) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }

    }

    public static boolean isWindowFocusChangeMethod(String name, String desc) {
        return null != name && null != desc && name.equals(TraceBuildConstants.MATRIX_TRACE_ON_WINDOW_FOCUS_METHOD) && desc.equals(TraceBuildConstants.MATRIX_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS);
    }

    /**
     * 是否需要跟踪
     *
     * @param configuration
     * @param clsName
     * @param mappingCollector
     * @return
     */
    public static boolean isNeedTrace(Configuration configuration, String clsName, MappingCollector mappingCollector) {
        boolean isNeed = true;
        if (configuration.blockSet.contains(clsName)) {
            isNeed = false;
        } else {
            if (null != mappingCollector) {
                clsName = mappingCollector.originalClassName(clsName, clsName);
            }
            clsName = clsName.replaceAll("/", ".");
            for (String packageName : configuration.blockSet) {
                if (clsName.startsWith(packageName.replaceAll("/", "."))) {
                    isNeed = false;
                    break;
                }
            }
        }
        return isNeed;
    }


    private void listClassFiles(ArrayList<File> classFiles, File folder) {
        File[] files = folder.listFiles();
        if (null == files) {
            Log.e(TAG, "[listClassFiles] files is null! %s", folder.getAbsolutePath());
            return;
        }
        for (File file : files) {
            if (file == null) {
                continue;
            }
            if (file.isDirectory()) {
                listClassFiles(classFiles, file);
            } else if (isNeedTraceFile(file.getName())) {
                classFiles.add(file);
            }
        }
    }

    /**
     * 过滤系统的一些文件:"R.class", "R$", "Manifest", "BuildConfig"
     *
     * @param fileName
     * @return
     */
    public static boolean isNeedTraceFile(String fileName) {
        if (fileName.endsWith(".class")) {
            for (String unTraceCls : TraceBuildConstants.UN_TRACE_CLASS) {
                if (fileName.contains(unTraceCls)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

}
