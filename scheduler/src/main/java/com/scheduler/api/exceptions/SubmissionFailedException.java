package com.scheduler.api.exceptions;

public class SubmissionFailedException extends Exception {

    public SubmissionFailedException(String message) {
        super(message);
    }

    public SubmissionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
