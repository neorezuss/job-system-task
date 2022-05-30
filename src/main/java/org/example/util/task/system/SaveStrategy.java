package org.example.util.task.system;

public interface SaveStrategy {
    <T> boolean save(long taskId, T result);
    <T> T getResult(long taskId, Class<T> type);
}
