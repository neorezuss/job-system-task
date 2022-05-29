package org.example.util.task;

public abstract class Task<T> {
    private long id;
    private TaskState state;

    public Task(long id) {
        this.id = id;
        this.state = TaskState.NEW;
    }

    public long getId() {
        return id;
    }

    public TaskState getState() {
        return state;
    }

    public abstract T execute();
}
