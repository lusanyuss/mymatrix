

package com.tencent.matrix.apk.model.result;


import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 
 */
@SuppressWarnings("PMD")
public abstract class TaskResult {
    private final SimpleDateFormat dateFormat;
    protected String startTime;
    protected String endTime;
    public final int taskType;

    public TaskResult(int taskType) {
        this.taskType = taskType;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    }

    public void setStartTime(long startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        this.startTime = dateFormat.format(calendar.getTime());
    }

    public void setEndTime(long endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(endTime);
        this.endTime = dateFormat.format(calendar.getTime());
    }

    public abstract Object getResult();

}
