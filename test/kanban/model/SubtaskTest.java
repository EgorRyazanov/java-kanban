package kanban.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    @Test
    void shouldReturnEpicId() {
        Subtask subtask = new Subtask("Title", "Desc", 1, TaskStatus.NEW, 42);
        assertEquals(42, subtask.getEpicId());
    }
}