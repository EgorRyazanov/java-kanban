package kanban;

public class Managers {

    private final HistoryManager historyManager = new HistoryManager();

    public TaskManager getDefault() {
        return new InMemoryTaskManager(historyManager);
    }

    public HistoryManager getDefaultHistory() {
        return historyManager;
    }
}
