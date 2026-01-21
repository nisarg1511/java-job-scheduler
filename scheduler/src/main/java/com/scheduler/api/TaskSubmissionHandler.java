package com.scheduler.api;

import com.scheduler.api.exceptions.DuplicateTaskException;
import com.scheduler.api.exceptions.SubmissionFailedException;
import com.scheduler.core.TaskPayload;

/**
 * TaskSubmissionService defines the contract for submitting new tasks to the
 * scheduler.
 */
public interface TaskSubmissionHandler {

    /**
     * Submits a new task for execution.
     *
     * @param taskId unique identifier for the task (client-generated)
     * @param payload the task payload, already registered in TaskRegistry
     * @throws DuplicateTaskException if a task with the same ID already exists
     * @throws SubmissionFailedException if durable submission fails (e.g. I/O
     * error)
     */
    void submit(String taskId, TaskPayload payload)
            throws DuplicateTaskException, SubmissionFailedException;

    /**
     * Submits a new task only if no task with the same ID exists.
     *
     * This is a no-op if the task already exists.
     *
     * @param taskId unique identifier for the task
     * @param payload the task payload
     * @throws SubmissionFailedException if durable submission fails
     */
    void submitIfAbsent(String taskId, TaskPayload payload)
            throws SubmissionFailedException;
}
