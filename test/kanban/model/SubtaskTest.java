package kanban.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    @Test
    void shouldReturnEpicId() {
        Subtask subtask = new Subtask("Title", "Desc", 1, TaskStatus.NEW, 42, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        assertEquals(42, subtask.getEpicId());
    }
}