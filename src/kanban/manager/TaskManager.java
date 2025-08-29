package kanban.manager;

import kanban.model.Epic;
import kanban.model.Subtask;
import kanban.model.Task;
import kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeSet;

public interface TaskManager {
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTask(int id);

    Subtask getSubtask(int id);

    Task addTask(String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime);

    void updateTask(Task task);

    void removeTask(int id);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpic(int id);

    Epic addEpic(String title, String desc);

    void removeEpic(int id);

    List<Subtask> getAllSubtasks();

    List<Subtask> getSubtasksByEpic(int epicId);

    Subtask addSubtask(String title, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime);

    void updateSubtask(Subtask subtask);

    void removeSubtask(int id);

    TreeSet<Task> getPrioritizedTasks();

    boolean checkTimeTaskAvailable(LocalDateTime startTime, Duration duration);

    List<Task> getHistory();
}
