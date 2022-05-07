

package com.tencent.matrix.apk.model.task;

import com.tencent.matrix.apk.model.task.util.ApkConstants;
import com.tencent.matrix.apk.model.exception.TaskExecuteException;
import com.tencent.matrix.apk.model.exception.TaskInitException;
import com.tencent.matrix.apk.model.job.JobConfig;
import com.tencent.matrix.apk.model.result.TaskJsonResult;
import com.tencent.matrix.apk.model.result.TaskResult;
import com.tencent.matrix.apk.model.result.TaskResultFactory;
import com.tencent.matrix.javalib.util.Log;
import com.tencent.matrix.javalib.util.Util;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import static com.tencent.matrix.apk.model.result.TaskResultFactory.TASK_RESULT_TYPE_JSON;
import static com.tencent.matrix.apk.model.task.TaskFactory.TASK_TYPE_CHECK_RESGUARD;



public class ResProguardCheckTask extends ApkTask {

    private static final String TAG = "Matrix.ResProguardCheckTask";

    private File inputFile;
    private Pattern fileNamePattern;

    public ResProguardCheckTask(JobConfig config, Map<String, String> params) {
        super(config, params);
        type = TASK_TYPE_CHECK_RESGUARD;
    }

    @Override
    public void init() throws TaskInitException {
        super.init();
        String inputPath = config.getUnzipPath();
        if (Util.isNullOrNil(inputPath)) {
            throw new TaskInitException(TAG + "---APK-UNZIP-PATH can not be null!");
        }
        Log.i(TAG, "inputPath:%s", inputPath);
        inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            throw new TaskInitException(TAG + "---APK-UNZIP-PATH '" + inputPath + "' is not exist!");
        } else if (!inputFile.isDirectory()) {
            throw new TaskInitException(TAG + "---APK-UNZIP-PATH '" + inputPath + "' is not directory!");
        }
        fileNamePattern = Pattern.compile("[a-z_0-9]{1,3}");
    }



    @Override
    public TaskResult call() throws TaskExecuteException {
        File resDir = new File(inputFile, ApkConstants.RESOURCE_DIR_PROGUARD_NAME);
        try {
            TaskResult taskResult = TaskResultFactory.factory(getType(), TASK_RESULT_TYPE_JSON, config);
            if (taskResult == null) {
                return null;
            }
            long startTime = System.currentTimeMillis();
            if (resDir.exists() && resDir.isDirectory()) {
                Log.i(TAG, "find resource directory " + resDir.getAbsolutePath());
                ((TaskJsonResult) taskResult).add("hasResProguard", true);
            } else {
                resDir = new File(inputFile, ApkConstants.RESOURCE_DIR_NAME);
                if (resDir.exists() && resDir.isDirectory()) {
                    File[] dirs = resDir.listFiles();
                    boolean hasProguard = true;
                    for (File dir : dirs) {
                        if (dir.isDirectory() && !fileNamePattern.matcher(dir.getName()).matches()) {
                            hasProguard = false;
                            Log.i(TAG, "directory " + dir.getName() + " has a non-proguard name!");
                            break;
                        }
                    }
                    ((TaskJsonResult) taskResult).add("hasResProguard", hasProguard);
                } else {
                    throw new TaskExecuteException(TAG + "---No resource directory found!");
                }
            }
            taskResult.setStartTime(startTime);
            taskResult.setEndTime(System.currentTimeMillis());
            return taskResult;
        } catch (Exception e) {
            throw new TaskExecuteException(e.getMessage(), e);
        }
    }
}
