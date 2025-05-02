package kanban;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getAllTasks();

    void deleteAllTasks();

    Task getTask(int id);

    Subtask getSubtask(int id);

    void addTask(String title, String description, TaskStatus status);

    void updateTask(Task task);

    void removeTask(int id);

    ArrayList<Epic> getAllEpics();

    void deleteAllEpics();

    Epic getEpic(int id);

    void addEpic(String title, String desc);

    void updateEpic(Epic epic);

    void removeEpic(int id);

    ArrayList<Subtask> getAllSubtasks();

    ArrayList<Subtask> getSubtasksByEpic(int epicId);

    void addSubtask(String title, String description, TaskStatus status, int epicId);

    void updateSubtask(Subtask subtask);

    void removeSubtask(int id);
}
