package edu.uob.executor;


import java.io.Serial;

public class ExecutorException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ExecutorException(String message) {
        super(message);
    }

    public ExecutorException(String message, Throwable cause) {
        super(message, cause);
    }

}