package kanban.model;

import java.util.Comparator;

public class TaskComparator implements Comparator<Task> {
    @Override
    public int compare(Task item1, Task item2) {
        return Integer.compare(item1.id, item2.id);
    }
}
