package com.scheduler.api;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.scheduler.api.exceptions.DuplicateTaskException;
import com.scheduler.api.exceptions.SubmissionFailedException;
import com.scheduler.core.TaskPayload;
import com.scheduler.core.TaskState;
import com.scheduler.scheduler.TaskRegistry;
import com.scheduler.store.TaskRecord;
import com.scheduler.store.TaskStore;

public class TaskSubmissionService implements TaskSubmissionHandler {

    private final TaskStore store;
    private final TaskRegistry registry;

    public TaskSubmissionService(TaskStore store, TaskRegistry registry) {
        this.store = store;
        this.registry = registry;
    }

    @Override
    public void submit(String taskId, TaskPayload payload) throws DuplicateTaskException, SubmissionFailedException {
        try {
            List<TaskRecord> existingTasks = this.store.loadAll();
            for (TaskRecord record : existingTasks) {
                if (record.getTaskId().equals(taskId)) {
                    throw new DuplicateTaskException("Task with this ID already exist.");
                }
            }
            System.out.println("No duplicates found, submitting task: " + taskId);
            submitIfAbsent(taskId, payload);
            System.out.println("Task submitted successfully: " + taskId);
        } catch (IOException e) {
            throw new SubmissionFailedException("Failed to submit task.");
        }
    }

    @Override
    public void submitIfAbsent(String taskId, TaskPayload payload) throws SubmissionFailedException {
        try {
            System.out.println("Registering task in registry: " + taskId);
            this.registry.register(taskId, payload);
            System.out.println("Appending task to store: " + taskId);
            this.store.append(new TaskRecord(taskId, TaskState.PENDING, Instant.now()));
            System.out.println("Appended task to store: " + taskId);
        } catch (IOException e) {
            throw new SubmissionFailedException("Failed to submit task.");
        }
    }

}
