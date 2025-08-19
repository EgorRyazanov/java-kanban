package kanban.util;

import kanban.manager.FileBackedTaskManager;
import kanban.manager.InMemoryHistoryManager;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import kanban.model.HistoryManager;
import java.io.File;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static TaskManager getFileBackedTaskManager(File file) {
        return FileBackedTaskManager.createManager(getDefaultHistory(), file);
    }

    private static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}