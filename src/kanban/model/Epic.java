package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Epic extends Task {
    private final List<Subtask> subtasks;

    public Epic(String title, String description, int id, Duration duration, LocalDateTime startTime) {
        super(title, description, id, TaskStatus.NEW, duration, startTime);
        this.type = TaskType.EPIC;
        this.subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtaskId) {
        subtasks.add(subtaskId);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.remove(subtasks.stream().map(subtask1 -> subtask.getId()).collect(Collectors.toList()).indexOf(subtask.getId()));
        addSubtask(subtask);
    }

    public void removeSubtask(int subtaskId) {
        subtasks.remove(subtasks.stream()
                .filter(subtask -> subtask.getId() == subtaskId)
                .findFirst()
                .orElseThrow(() -> new Error("Не нашлась подзадача")));
    }

    public TaskStatus getStatus() {
        if (subtasks.isEmpty()) {
            return TaskStatus.NEW;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask  : subtasks) {
            TaskStatus status = subtask.getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }
        System.out.println(allDone);
        System.out.println(allNew);
        if (allDone) {
            return TaskStatus.DONE;
        } else if (allNew) {
            return TaskStatus.NEW;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public LocalDateTime getEndTime() {
        if (subtasks.isEmpty()) {
            return null;
        }

        Optional<LocalDateTime> endTime = subtasks.stream()
                .map(Task::getEndTime)
                .max(LocalDateTime::compareTo);

       return endTime.get();
    }
}