

package com.tencent.matrix.apk.model.task;


import com.tencent.matrix.apk.model.exception.TaskExecuteException;
import com.tencent.matrix.apk.model.exception.TaskInitException;
import com.tencent.matrix.apk.model.job.JobConfig;
import com.tencent.matrix.apk.model.result.TaskResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 
 */

public abstract class ApkTask implements Callable<TaskResult> {

    private static final String TAG = "Matrix.ApkTask";

    protected int type;
    protected JobConfig config;
    protected Map<String, String> params;
    protected List<ApkTaskProgressListener> progressListeners;

    public interface ApkTaskProgressListener {
        void getProgress(int progress, String message);
    }


    public ApkTask(JobConfig config, Map<String, String> params) {
        this.params = params;
        this.config = config;
        progressListeners = new LinkedList<>();
    }

    public int getType() {
        return type;
    }

    public void init() throws TaskInitException {
        if (config == null) {
            throw new TaskInitException(TAG + "---jobConfig can not be null!");
        }

        if (params == null) {
            throw new TaskInitException(TAG + "---params can not be null!");
        }
    }

    public void addProgressListener(ApkTaskProgressListener listener) {
        if (listener != null) {
            progressListeners.add(listener);
        }
    }

    public void removeProgressListener(ApkTaskProgressListener listener) {
        if (listener != null) {
            progressListeners.remove(listener);
        }
    }

    protected void notifyProgress(int progress, String message) {
        for (ApkTaskProgressListener listener : progressListeners) {
            listener.getProgress(progress, message);
        }
    }

    @Override
    public abstract TaskResult call() throws TaskExecuteException;
}
