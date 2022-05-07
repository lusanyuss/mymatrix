

package com.tencent.matrix.apk.model.task;

import com.google.gson.JsonArray;
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

import static com.tencent.matrix.apk.model.result.TaskResultFactory.TASK_RESULT_TYPE_JSON;
import static com.tencent.matrix.apk.model.task.TaskFactory.TASK_TYPE_CHECK_MULTILIB;



public class MultiLibCheckTask extends ApkTask {

    private static final String TAG = "Matrix.MultiLibCheckTask";

    private File libDir;

    public MultiLibCheckTask(JobConfig jobConfig, Map<String, String> params) {
        super(jobConfig, params);
        type = TASK_TYPE_CHECK_MULTILIB;
    }

    @Override
    public void init() throws TaskInitException {
        super.init();
        String inputPath = config.getUnzipPath();
        if (!Util.isNullOrNil(inputPath)) {
            Log.i(TAG, "inputPath:%s", inputPath);
            libDir = new File(inputPath, "lib");
        } else {
            throw new TaskInitException(TAG + "---APK-UNZIP-PATH can not be null!");
        }
    }

    @Override
    public TaskResult call() throws TaskExecuteException {
        try {
            TaskResult taskResult = TaskResultFactory.factory(getType(), TASK_RESULT_TYPE_JSON, config);
            if (taskResult == null) {
                return null;
            }
            long startTime = System.currentTimeMillis();
            JsonArray jsonArray = new JsonArray();
            if (libDir.exists() && libDir.isDirectory()) {
                File[] dirs = libDir.listFiles();
                for (File dir : dirs) {
                    if (dir.isDirectory()) {
                        jsonArray.add(dir.getName());
                    }
                }
            }
            ((TaskJsonResult) taskResult).add("lib-dirs", jsonArray);
            if (jsonArray.size() > 1) {
                ((TaskJsonResult) taskResult).add("multi-lib", true);
            } else {
                ((TaskJsonResult) taskResult).add("multi-lib", false);
            }
            taskResult.setStartTime(startTime);
            taskResult.setEndTime(System.currentTimeMillis());
            return taskResult;
        } catch (Exception e) {
            throw new TaskExecuteException(e.getMessage(), e);
        }
    }


}
