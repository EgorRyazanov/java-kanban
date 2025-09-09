package kanban.manager;

import kanban.exception.NotFoundException;
import kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected int idCounter = 1;
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
    public Task getTask(int id) throws NotFoundException {
        Task task = tasks.get(id);

        if (task == null) {
            throw new NotFoundException();
        }

        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) throws NotFoundException {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException();
        }
        historyManager.add(subtask);
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
    public void removeTask(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException();
        }

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
            for (Integer subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
        }
        epics.clear();
    }

    @Override
    public Epic getEpic(int id) throws NotFoundException {
        Epic epic = epics.get(id);

        if (epic == null) {
            throw new NotFoundException();
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic addEpic(String title, String desc) {
        int id = generateId();
        Epic epic = new Epic(title, desc, id, Duration.ofMinutes(0), null);
        System.out.println("Эпик создан с ID: " + id);
        updateEpic(epic);

        return epic;
    }



    @Override
    public void removeEpic(int id) throws NotFoundException {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException();
        }

        for (Integer subtaskId : epic.getSubtasksIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) throws NotFoundException {
        Epic epic = epics.get(epicId);
        List<Subtask> result = new ArrayList<>();
        if (epic == null) {
            throw new NotFoundException();
        }

        for (int id : epic.getSubtasksIds()) {
            result.add(subtasks.get(id));
        }
        return result;
    }

    @Override
    public Subtask addSubtask(String title, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        int id = generateId();
        Subtask subtask = new Subtask(title, description, id, status, epicId, duration, startTime);
        System.out.println("Подзадача создана с ID: " + id);
        subtasks.put(id, subtask);
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpic(epic);
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
        updateEpic(epic);
    }

    @Override
    public void removeSubtask(int id) throws NotFoundException {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException();
        }

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtask(id);
            updateEpic(epic);
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

    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtasksIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subId : subtaskIds) {
            TaskStatus status = subtasks.get(subId).getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private int generateId() {
        return idCounter++;
    }

    private void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
        updateEpicTime(epic);
    }

    private void updateEpicTime(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtasksIds();
        if (subtaskIds.isEmpty()) {
            epic.setEndDate(null);
            return;
        }

        Optional<LocalDateTime> endTime = subtaskIds.stream()
                .map(subtasks::get)
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo);

        epic.setEndDate(endTime.get());
    }
}