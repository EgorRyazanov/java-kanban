package kanban.util;

import kanban.manager.InMemoryHistoryManager;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import kanban.model.HistoryManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    private static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}