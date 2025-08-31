package kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Objects;

public class Task {

    protected Duration duration;
    protected LocalDateTime startTime;
    protected String title;
    protected String description;
    protected int id;
    protected TaskStatus status;
    protected TaskType type = TaskType.TASK;

    public Task(String title, String description, int id, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public int getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskType getType() {
        return type;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration.toMinutes());
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s", id, type, title, status, description, duration.toMinutes(), startTime != null ? DateTimeFormatter.ISO_DATE_TIME.format(startTime) : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return task.id == this.id && Objects.equals(task.title, this.title);
    }

    @Override
    public int hashCode() {
        return 17 + Objects.hashCode(id);
    }

    public static final Comparator<Task> START_TIME_COMPARATOR = Comparator.comparing(Task::getStartTime);
}

