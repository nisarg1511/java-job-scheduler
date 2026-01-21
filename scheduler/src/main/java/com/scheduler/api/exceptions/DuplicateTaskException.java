package com.scheduler.api.exceptions;

public class DuplicateTaskException extends Exception {

    public DuplicateTaskException(String message) {
        super(message);
    }

    public DuplicateTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}
