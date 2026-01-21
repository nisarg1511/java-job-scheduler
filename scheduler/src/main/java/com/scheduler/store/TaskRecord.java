package com.scheduler.store;

import java.time.Instant;

import com.scheduler.core.TaskState;

public final class TaskRecord {

    private String taskId;
    private TaskState state;
    private Instant timestamp;

    public TaskRecord() {
    }

    public TaskRecord(String taskId, TaskState state, Instant timestamp) {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("taskId must not be null or blank");
        }
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }

        this.taskId = taskId;
        this.state = state;
        this.timestamp = timestamp;
    }

    public String getTaskId() {
        return taskId;
    }

    public TaskState getState() {
        return state;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
