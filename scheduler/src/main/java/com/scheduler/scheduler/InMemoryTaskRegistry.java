package com.scheduler.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.scheduler.core.TaskPayload;

public class InMemoryTaskRegistry implements TaskRegistry {

    private final Map<String, TaskPayload> registry = new ConcurrentHashMap<>();

    @Override
    public void register(String taskId, TaskPayload payload) {
        if (registry.containsKey(taskId)) {
            throw new IllegalArgumentException("Task already registered: " + taskId);
        }
        registry.put(taskId, payload);
    }

    @Override
    public TaskPayload getTask(String taskId) {
        return registry.get(taskId);
    }

    @Override
    public boolean contains(String taskId) {
        return registry.containsKey(taskId);
    }
}
