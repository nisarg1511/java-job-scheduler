package com.scheduler.scheduler;

import com.scheduler.core.Task;

public interface TaskExecutor {

    void execute(Task task) throws Exception;
}
