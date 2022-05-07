

package com.tencent.matrix.apk.model.result;


import java.util.List;


@SuppressWarnings("PMD")
public abstract class JobResult {

    protected String format = TaskResultFactory.TASK_RESULT_TYPE_HTML;

    protected List<TaskResult> resultList;

    public String getFormat() {
        return format;
    }

    public void addTaskResult(TaskResult result) {
        if (resultList != null) {
            resultList.add(result);
        }
    }

    public abstract void output();

}
