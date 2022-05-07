

package com.tencent.matrix.apk.model.task;


import com.google.common.collect.Ordering;
import com.google.gson.JsonArray;
import com.tencent.matrix.apk.model.exception.TaskExecuteException;
import com.tencent.matrix.apk.model.exception.TaskInitException;
import com.tencent.matrix.apk.model.job.JobConfig;
import com.tencent.matrix.apk.model.job.JobConstants;
import com.tencent.matrix.apk.model.result.TaskJsonResult;
import com.tencent.matrix.apk.model.result.TaskResult;
import com.tencent.matrix.apk.model.result.TaskResultFactory;
import com.tencent.matrix.apk.model.task.util.ApkConstants;
import com.tencent.matrix.apk.model.task.util.ApkUtil;
import com.tencent.matrix.javalib.util.Log;
import com.tencent.matrix.javalib.util.Util;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;




public class UnusedAssetsTask extends ApkTask {

    private static final String TAG = "Matrix.UnusedAssetsTask";

    private File inputFile;
    private final List<String> dexFileNameList;
    private final Set<String> ignoreSet;
    private final Set<String> assetsPathSet;
    private final Set<String> assetRefSet;

    public UnusedAssetsTask(JobConfig config, Map<String, String> params) {
        super(config, params);
        type = TaskFactory.TASK_TYPE_UNUSED_ASSETS;
        dexFileNameList = new ArrayList<>();
        ignoreSet = new HashSet<>();
        assetsPathSet = new HashSet<>();
        assetRefSet = new HashSet<>();
    }

    @Override
    public void init() throws TaskInitException {
        super.init();

        String inputPath = config.getUnzipPath();
        if (Util.isNullOrNil(inputPath)) {
            throw new TaskInitException(TAG + "---APK-UNZIP-PATH can not be null!");
        }
        inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new TaskInitException(TAG + "---APK-UNZIP-PATH '" + inputPath + "' is not exist!");
        } else if (!inputFile.isDirectory()) {
            throw new TaskInitException(TAG + "---APK-UNZIP-PATH '" + inputPath + "' is not directory!");
        }
        if (params.containsKey(JobConstants.PARAM_IGNORE_ASSETS_LIST) && !Util.isNullOrNil(params.get(JobConstants.PARAM_IGNORE_ASSETS_LIST))) {
            String[] ignoreAssets = params.get(JobConstants.PARAM_IGNORE_ASSETS_LIST).split(",");
            Log.i(TAG, "ignore assets %d", ignoreAssets.length);
            for (String ignore : ignoreAssets) {
                ignoreSet.add(Util.globToRegexp(ignore));
            }
        }

        File[] files = inputFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(ApkConstants.DEX_FILE_SUFFIX)) {
                    dexFileNameList.add(file.getName());
                }
            }
        }
    }

    private void findAssetsFile(File dir) throws IOException {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    findAssetsFile(file);
                } else {
                    Log.d(TAG, "find asset file %s", file.getAbsolutePath());
                    assetsPathSet.add(file.getAbsolutePath());
                }
            }
        }
    }

    private void decodeCode() throws IOException {
        for (String dexFileName : dexFileNameList) {
            DexBackedDexFile dexFile = DexFileFactory.loadDexFile(new File(inputFile, dexFileName), Opcodes.forApi(15));

            BaksmaliOptions options = new BaksmaliOptions();
            List<? extends ClassDef> classDefs = Ordering.natural().sortedCopy(dexFile.getClasses());

            for (ClassDef classDef : classDefs) {
                String[] lines = ApkUtil.disassembleClass(classDef, options);
                if (lines != null) {
                    readSmaliLines(lines);
                }
            }

        }
    }

    private void readSmaliLines(String[] lines) {
        if (lines == null) {
            return;
        }
        for (String line : lines) {
            line = line.trim();
            if (!Util.isNullOrNil(line) && line.startsWith("const-string")) {
                String[] columns = line.split(",");
                if (columns.length == 2) {
                    String assetFileName = columns[1].trim();
                    assetFileName = assetFileName.substring(1, assetFileName.length() - 1);
                    if (!Util.isNullOrNil(assetFileName)) {
                        for (String path : assetsPathSet) {
                            if (assetFileName.endsWith(path)) {
                                assetRefSet.add(path);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean ignoreAsset(String name) {
        for (String pattern : ignoreSet) {
            Log.d(TAG, "pattern %s", pattern);
            if (name.matches(pattern)) {
                return true;
            }
        }
        return false;
    }

    private void generateAssetsSet(String rootPath) {
        HashSet<String> relativeAssetsSet = new HashSet<String>();
        for (String path : assetsPathSet) {
            int index = path.indexOf(rootPath);
            if (index >= 0) {
                String relativePath = path.substring(index + rootPath.length() + 1);
                Log.d(TAG, "assets %s", relativePath);
                relativeAssetsSet.add(relativePath);
                if (ignoreAsset(relativePath)) {
                    Log.d(TAG, "ignore assets %s", relativePath);
                    assetRefSet.add(relativePath);
                }
            }
        }
        assetsPathSet.clear();
        assetsPathSet.addAll(relativeAssetsSet);
    }


    @Override
    public TaskResult call() throws TaskExecuteException {
        try {
            TaskResult taskResult = TaskResultFactory.factory(type, TaskResultFactory.TASK_RESULT_TYPE_JSON, config);
            long startTime = System.currentTimeMillis();
            File assetDir = new File(inputFile, ApkConstants.ASSETS_DIR_NAME);
            findAssetsFile(assetDir);
            generateAssetsSet(assetDir.getAbsolutePath());
            Log.i(TAG, "find all assets count: %d", assetsPathSet.size());
            decodeCode();
            Log.i(TAG, "find reference assets count: %d", assetRefSet.size());
            assetsPathSet.removeAll(assetRefSet);
            JsonArray jsonArray = new JsonArray();
            for (String name : assetsPathSet) {
                jsonArray.add(name);
            }
            ((TaskJsonResult) taskResult).add("unused-assets", jsonArray);
            taskResult.setStartTime(startTime);
            taskResult.setEndTime(System.currentTimeMillis());
            return taskResult;
        } catch (Exception e) {
            throw new TaskExecuteException(e.getMessage(), e);
        }
    }
}
