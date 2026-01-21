package com.scheduler.scheduler;

import java.time.Instant;
import java.util.List;

import com.scheduler.core.TaskPayload;
import com.scheduler.core.TaskState;
import com.scheduler.recovery.RecoveryManager;
import com.scheduler.recovery.RecoveryResult;
import com.scheduler.store.TaskRecord;
import com.scheduler.store.TaskStore;

public class Scheduler {

    private final TaskStore taskStore;
    private final TaskRegistry taskRegistry;
    private final RecoveryManager recoveryManager;

    public Scheduler(TaskStore taskStore, TaskRegistry taskRegistry, RecoveryManager recoveryManager) {
        this.taskStore = taskStore;
        this.taskRegistry = taskRegistry;
        this.recoveryManager = recoveryManager;
    }

    public void runOnce() {
        RecoveryResult result = recoveryManager.recover();
        List<String> pendingTaskIds = result.getPendingTaskIds();
        System.out.println("Executing.....");
        for (String task : pendingTaskIds) {
            System.out.println("Executing task: " + task);
            TaskPayload payload = taskRegistry.getTask(task);
            if (payload == null) {
                continue;
            }
            try {
                payload.execute();
                System.out.println("Executed task: " + task);
                TaskRecord record = new TaskRecord(task, TaskState.COMPLETED, Instant.now());
                taskStore.append(record);
            } catch (Exception e) { //Research about catching Throwable instead of Execption
                System.out.println("Failed");
            }
        }
    }
}
