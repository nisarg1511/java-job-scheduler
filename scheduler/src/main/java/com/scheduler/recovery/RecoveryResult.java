package com.scheduler.recovery;

import java.util.List;
import java.util.Set;

public final class RecoveryResult {

    private final List<String> pendingTaskIds;
    private final Set<String> completedTaskIds;

    public RecoveryResult(List<String> pendingTaskIds, Set<String> completedTaskIds) {
        this.pendingTaskIds = pendingTaskIds;
        this.completedTaskIds = completedTaskIds;
    }

    public Set<String> getCompletedTaskIds() {
        return completedTaskIds;
    }

    public List<String> getPendingTaskIds() {
        return this.pendingTaskIds;
    }

}
