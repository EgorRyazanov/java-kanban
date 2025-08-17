package kanban.util;

import kanban.manager.FileBackedTaskManager;
import kanban.manager.InMemoryHistoryManager;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import kanban.model.HistoryManager;
import kanban.model.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static TaskManager getFileBackedTaskManager(File file) {
        try {
            List<Task> initialTasks =  FileBackedTaskManager.loadFromFile(file);
            return new FileBackedTaskManager(getDefaultHistory(), file, initialTasks);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }

        return null;
    }

    private static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}