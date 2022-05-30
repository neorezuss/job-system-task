package org.example.util.task.system;

import java.util.HashMap;
import java.util.Map;

public class InMemorySaveStrategy implements SaveStrategy {
    private final Map<Long, Object> results;

    public InMemorySaveStrategy() {
        this.results = new HashMap<>();
    }

    @Override
    public <T> boolean save(long taskId, T result) {
        results.put(taskId, result);
        return true;
    }

    @Override
    public <T> T getResult(long taskId, Class<T> type) {
        return type.cast(results.get(taskId));
    }
}
