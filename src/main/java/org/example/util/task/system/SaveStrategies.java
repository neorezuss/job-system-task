package org.example.util.task.system;

public class SaveStrategies {
    public static SaveStrategy newInMemorySaveStrategy() {
        return new InMemorySaveStrategy();
    }
}
