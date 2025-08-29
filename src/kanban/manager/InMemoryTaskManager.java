package kanban.manager;

import kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idCounter = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();

    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(Task.START_TIME_COMPARATOR);

    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        Optional<Task> task = tasks.values().stream().peek(
                prioritizedTasks::remove
        ).findFirst();
        tasks.clear();

    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Task addTask(String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        int id = generateId();
        Task task = new Task(title, description, id, status, duration, startTime);
        if (duration != null && startTime != null) {
            prioritizedTasks.add(task);
        }
        tasks.put(id, task);
        System.out.println("Задача создана с ID: " + id);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        Task oldTask = tasks.get(task.getId());
        prioritizedTasks.remove(oldTask);
        prioritizedTasks.add(task);
        tasks.put(task.getId(), task);
    }

    @Override
    public void removeTask(int id) {
        prioritizedTasks.remove(tasks.get(id));
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (Subtask subtask : epic.getSubtasks()) {
                int id = subtask.getId();
                subtasks.remove(id);
                historyManager.remove(id);
            }
        }
        epics.clear();
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic addEpic(String title, String desc) {
        int id = generateId();
        Epic epic = new Epic(title, desc, id, Duration.ofMinutes(0), null);
        epics.put(epic.getId(), epic);
        System.out.println("Эпик создан с ID: " + id);

        return epic;
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                int subId = subtask.getId();
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        return epic.getSubtasks();
    }

    @Override
    public Subtask addSubtask(String title, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        int id = generateId();
        Subtask subtask = new Subtask(title, description, id, status, epicId, duration, startTime);
        System.out.println("Подзадача создана с ID: " + id);
        subtasks.put(id, subtask);
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.addSubtask(subtask);
        }

        if (duration != null && startTime != null) {
            prioritizedTasks.add(subtask);
        }

        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        Subtask oldTask = subtasks.get(subtask.getId());
        prioritizedTasks.remove(oldTask);
        prioritizedTasks.add(subtask);
        subtasks.put(subtask.getId(), subtask);
        epic.updateSubtask(subtask);
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
            }
        }
        subtasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public boolean checkTimeTaskAvailable(LocalDateTime startTime, Duration duration) {
        LocalDateTime endTime = startTime.plusMinutes(duration.toMinutes());
        for (Task task : prioritizedTasks) {
            if (startTime.isBefore(task.getEndTime()) && endTime.isAfter(task.getStartTime())) {
                return false;
            }

        }

        return true;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private int generateId() {
        return idCounter++;
    }
}