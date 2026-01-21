package com.scheduler.scheduler;

import com.scheduler.core.TaskPayload;

public interface TaskRegistry {

    void register(String taskId, TaskPayload payload);

    TaskPayload getTask(String taskId);

    boolean contains(String taskId);
}
