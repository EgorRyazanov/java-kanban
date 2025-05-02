package kanban;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private final ArrayList<Task> history = new ArrayList<>(10);

    public void add(Task task) {
        System.out.println(history.size());
        if (history.size() == 10) {
            history.remove(0);
        }
        history.add(task);
    }

    public List<Task> getHistory() {
        return history;
    }
}
