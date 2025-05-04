package kanban.util;

import kanban.manager.HistoryManager;
import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    private static HistoryManager getDefaultHistory() {
        return  new HistoryManager();

    }
}