package org.example.util.task.system;

import org.example.util.task.Task;
import org.example.util.task.TaskState;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

public class TaskSystem {
    private final ScheduledExecutorService executorService;
    private SaveStrategy saveStrategy;

    public TaskSystem(int poolSize) {
        this.executorService = Executors.newScheduledThreadPool(poolSize);
        this.saveStrategy = SaveStrategies.newInMemorySaveStrategy();
    }

    public void setSaveStrategy(SaveStrategy saveStrategy) {
        this.saveStrategy = saveStrategy;
    }

    public <T> T getTaskResult(long taskId, Class<T> type) {
        return saveStrategy.getResult(taskId, type);
    }

    public <T> Future<T> executeImmediately(Task<T> task) {
        return CompletableFuture
                .supplyAsync(() -> {
                    setTaskState(task, TaskState.RUNNING);
                    return task.execute();
                }, executorService)
                .thenApply(res -> {
                    setTaskState(task, TaskState.COMPLETED);
                    saveStrategy.save(task.getId(), res);
                    return res;
                })
                .handle((res, ex) -> {
                    if (nonNull(ex)) {
                        setTaskState(task, TaskState.FAILED);
                        ex.printStackTrace();
                    }
                    return res;
                });
    }

    public ScheduledFuture<?> schedulePeriodicExecution(Task<?> task, long initDelay, long period, TimeUnit unit) {
        TaskRunnableAdapter taskAdapter = new TaskRunnableAdapter(task);
        setTaskState(task, TaskState.RUNNING);
        return executorService.scheduleAtFixedRate(taskAdapter, initDelay, period, unit);
    }

    private <T> Task<T> setTaskState(Task<T> task, TaskState state) {
        try {
            Field f1 = task.getClass().getSuperclass().getDeclaredField("state");
            f1.setAccessible(true);
            f1.set(task, state);
            return task;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Impossible to set task state.", e);
        }
    }
}
