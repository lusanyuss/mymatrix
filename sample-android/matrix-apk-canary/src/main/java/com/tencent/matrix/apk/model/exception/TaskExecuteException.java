

package com.tencent.matrix.apk.model.exception;



public class TaskExecuteException extends RuntimeException {

    public TaskExecuteException(String message) {
        super(message);
    }

    public TaskExecuteException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
