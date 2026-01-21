package com.scheduler.payload;

import com.scheduler.core.TaskPayload;

public class PrintTaskPayload implements TaskPayload {

    private final String message;

    public PrintTaskPayload(String message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        this.message = message;
    }

    @Override
    public void execute() {
        System.out.println(this.message);
    }
}
