

package com.tencent.matrix.apk.model.exception;



public class TaskInitException extends RuntimeException {

    public TaskInitException(String message) {
        super(message);
    }

    public TaskInitException(String message, Throwable cause) {
        super(message, cause);
    }

}
