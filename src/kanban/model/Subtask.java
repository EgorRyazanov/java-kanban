package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int id, TaskStatus status, int epicId,Duration duration, LocalDateTime startTime) {
        super(title, description, id, status, duration, startTime);
        this.type = TaskType.SUBTASK;
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s", id, type, title, status, description, epicId);
    }
}