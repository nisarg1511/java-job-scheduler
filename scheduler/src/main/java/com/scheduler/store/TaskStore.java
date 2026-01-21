package com.scheduler.store;

import java.io.IOException;
import java.util.List;

public interface TaskStore {

    void append(TaskRecord record) throws IOException;

    List<TaskRecord> loadAll() throws IOException;
}
