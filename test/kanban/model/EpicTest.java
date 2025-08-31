package kanban.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;
    private final int epicId = 1;

    private final Integer subtask1 = 1;
    private final Integer subtask2 = 2;

    @BeforeEach
    void beforeEach() {
        epic = new Epic("Test Epic", "Epic description", epicId, Duration.ofMinutes(0), LocalDateTime.of(2001, 1, 1, 1, 1, 1));
    }

    @Test
    void shouldCreateEpic() {
        assertEquals("Test Epic", epic.getTitle());
        assertEquals("Epic description", epic.getDescription());
        assertEquals(epicId, epic.getId());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldAddSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertEquals(2, epic.getSubtaskIds().size());
        assertTrue(epic.getSubtaskIds().contains(subtask1));
        assertTrue(epic.getSubtaskIds().contains(subtask2));
    }

    @Test
    void shouldRemoveSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        epic.removeSubtask(2);

        assertEquals(1, epic.getSubtaskIds().size());
        assertNotEquals(2, epic.getSubtaskIds().size());
        assertTrue(epic.getSubtaskIds().contains(subtask1));
    }

    @Test
    void shouldReturnSubtaskIds() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        ArrayList<Integer> result = new ArrayList<>();
        result.add(subtask1);
        result.add(subtask2);

        assertArrayEquals(result.toArray(), epic.getSubtaskIds().toArray());
    }

    @Test
    void getEndTimeShouldReturnNullWhenNoSubtasks() {
        assertNull(epic.getEndTime());
    }
}