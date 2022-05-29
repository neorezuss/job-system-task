package org.example.util.task.system;

import org.example.util.task.Task;
import org.example.util.task.TaskState;

import java.lang.reflect.Field;

class TaskRunnableAdapter implements Runnable {
    private final Task task;

    public TaskRunnableAdapter(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        try {
            task.execute();
        } catch (Exception e) {
            setFailedState(task);
        }
    }

    private void setFailedState(Task<?> task) {
        try {
            Field f1 = task.getClass().getSuperclass().getDeclaredField("state");
            f1.setAccessible(true);
            f1.set(task, TaskState.FAILED);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Impossible to set task state.", e);
        }
    }
}
