package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtasksIds;

    private LocalDateTime endDate;

    public Epic(String title, String description, int id, Duration duration, LocalDateTime startTime) {
        super(title, description, id, TaskStatus.NEW, duration, startTime);
        this.type = TaskType.EPIC;
        this.subtasksIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtasksIds;
    }

    public void addSubtask(Integer subtaskId) {
        subtasksIds.add(subtaskId);
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void removeSubtask(int subtaskId) {
        subtasksIds.remove(subtasksIds.stream()
                .filter(id ->  id == subtaskId)
                .findFirst()
                .orElseThrow(() -> new Error("Не нашлась подзадача")));
    }

    @Override
    public LocalDateTime getEndTime() {
        return endDate;
    }
}