package kanban.manager;

import kanban.model.Epic;
import kanban.model.Subtask;
import kanban.model.Task;
import kanban.model.TaskStatus;

import java.util.List;

public interface TaskManager {
    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTask(int id);

    Subtask getSubtask(int id);

    void addTask(String title, String description, TaskStatus status);

    void updateTask(Task task);

    void removeTask(int id);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpic(int id);

    void addEpic(String title, String desc);

    void updateEpic(Epic epic);

    void removeEpic(int id);

    List<Subtask> getAllSubtasks();

    List<Subtask> getSubtasksByEpic(int epicId);

    void addSubtask(String title, String description, TaskStatus status, int epicId);

    void updateSubtask(Subtask subtask);

    void removeSubtask(int id);

    List<Task> getHistory();
}
