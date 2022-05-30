package org.example.util.task.system;

import org.example.util.task.Task;
import org.example.util.task.TaskState;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TaskSystemTest {
    private TaskSystem taskSystem;

    @Before
    public void setUp() {
        taskSystem = new TaskSystem(4);
    }

    @Test
    public void executeImmediatelyValidTask() throws ExecutionException, InterruptedException {
        Task<Integer> task = new Task<Integer>(1L) {
            @Override
            public Integer execute() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 1000 + 3000;
            }
        };
        assertEquals(task.getState(), TaskState.NEW);

        Future<Integer> futureResult = taskSystem.executeImmediately(task);

        assertEquals(task.getState(), TaskState.RUNNING);

        Integer result = futureResult.get();

        assertEquals(task.getState(), TaskState.COMPLETED);
        assertEquals(result, taskSystem.getTaskResult(1, Integer.class));
        assertEquals(result, Integer.valueOf(4000));
    }

    @Test
    public void executeImmediatelyFailedTask() throws ExecutionException, InterruptedException {
        Task<Integer> task = new Task<Integer>(2L) {
            @Override
            public Integer execute() {
                throw new RuntimeException("Something happened...");
            }
        };
        assertEquals(task.getState(), TaskState.NEW);

        Integer result = taskSystem.executeImmediately(task).get();

        assertEquals(task.getState(), TaskState.FAILED);
        assertNull(taskSystem.getTaskResult(2L, Integer.class));
        assertNull(result);
    }

    @Test
    public void schedulePeriodicExecutionValidTask() {
        Task<Void> task = new Task<Void>(3L) {
            @Override
            public Void execute() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long result = new Date().getTime();
                System.out.println("Result is " + result);
                return null;
            }
        };
        assertEquals(task.getState(), TaskState.NEW);

        ScheduledFuture<?> scheduledFuture =
                taskSystem.schedulePeriodicExecution(task, 1000, 5000, TimeUnit.MILLISECONDS);

        assertEquals(task.getState(), TaskState.RUNNING);

    }

    @Test
    public void schedulePeriodicExecutionInvalidTask() throws InterruptedException {
        Task<Void> task = new Task<Void>(4L) {
            @Override
            public Void execute() {
                throw new RuntimeException("Something happened...");
            }
        };
        assertEquals(task.getState(), TaskState.NEW);

        ScheduledFuture<?> scheduledFuture =
                taskSystem.schedulePeriodicExecution(task, 1000, 5000, TimeUnit.MILLISECONDS);

        assertEquals(task.getState(), TaskState.RUNNING);
        Thread.sleep(1100);
        assertEquals(task.getState(), TaskState.FAILED);
    }
}