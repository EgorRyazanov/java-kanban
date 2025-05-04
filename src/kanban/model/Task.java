package kanban.model;

import java.util.Objects;

public class Task {
    protected String title;
    protected String description;
    protected int id;
    protected TaskStatus status;

    public Task(String title, String description, int id, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.id = id;
        this.status = status;
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

    @Override
    public String toString() {
        return id + ": " + title + " (" + status + ")";
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
        return  17 + Objects.hashCode(id) + title.hashCode();
    }
}