package com.scheduler.recovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.scheduler.core.TaskState;
import com.scheduler.store.FileTaskStore;
import com.scheduler.store.TaskRecord;
import com.scheduler.store.TaskStore;

public class RecoveryManager {

    TaskStore store;

    public RecoveryManager(FileTaskStore store) {
        this.store = store;
    }

    public RecoveryResult recover() {
        try {
            // 1. load all task records from store
            Map<String, TaskState> taskStateMap = new HashMap<>();
            List<TaskRecord> records = this.store.loadAll();
            List<String> pendingTaskIds = new ArrayList<>();
            Set<String> completedTaskIds = new HashSet<>();
            // 2. replay records to rebuild task state
            for (TaskRecord record : records) {
                if (!(taskStateMap.containsKey(record.getTaskId())) && (record.getState() == TaskState.PENDING)) {
                    taskStateMap.put(record.getTaskId(), TaskState.PENDING);
                    pendingTaskIds.add(record.getTaskId());
                } else {
                    TaskState currentState = taskStateMap.get(record.getTaskId());
                    TaskState incomingState = record.getState();
                    if (incomingState == TaskState.COMPLETED && currentState == TaskState.PENDING) {
                        taskStateMap.put(record.getTaskId(), incomingState);
                        pendingTaskIds.remove(record.getTaskId());
                        completedTaskIds.add(record.getTaskId());
                    }
                }
            }
            // 4. return recovery result
            return new RecoveryResult(pendingTaskIds, completedTaskIds);
        } catch (IOException e) {
            throw new IllegalStateException("Recovery failed", e);
        }
    }
}
