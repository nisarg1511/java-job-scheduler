package com.scheduler;

import java.nio.file.Path;

import com.scheduler.api.TaskSubmissionService;
import com.scheduler.payload.PrintTaskPayload;
import com.scheduler.recovery.RecoveryManager;
import com.scheduler.scheduler.InMemoryTaskRegistry;
import com.scheduler.scheduler.Scheduler;
import com.scheduler.scheduler.TaskRegistry;
import com.scheduler.store.FileTaskStore;
import com.scheduler.store.TaskStore;

public final class App {

    public static void main(String[] args) throws Exception {

        // 1. Log file location
        Path logPath = Path.of("data", "task-log.txt");

        // 2. Store
        TaskStore store = new FileTaskStore(logPath);

        // 3. Registry
        TaskRegistry registry = new InMemoryTaskRegistry();

        // 4. Recovery
        RecoveryManager recoveryManager = new RecoveryManager((FileTaskStore) store);

        Scheduler scheduler = new Scheduler(store, registry, recoveryManager);

        // 7. Submission API
        TaskSubmissionService submissionService
                = new TaskSubmissionService(store, registry);

        // 8. Submit demo tasks
        submissionService.submit(
                "task-1",
                new PrintTaskPayload("Hello from task-1")
        );

        submissionService.submit(
                "task-2",
                new PrintTaskPayload("Hello from task-2")
        );

        // 9. Run scheduler once
        scheduler.runOnce();
    }
}
