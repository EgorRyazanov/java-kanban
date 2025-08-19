package kanban.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int id, TaskStatus status, int epicId) {
        super(title, description, id, status);
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