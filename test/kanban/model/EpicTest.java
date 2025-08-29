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
    private final LocalDateTime baseTime = LocalDateTime.of(2023, 12, 25, 10, 0);

    private final Subtask subtask1 = new Subtask("Title", "Desc", 1, TaskStatus.NEW, epicId,
            Duration.ofHours(1), baseTime.plusHours(1));
    private final Subtask subtask2 = new Subtask("Title2", "Desc2", 2, TaskStatus.NEW, epicId,
            Duration.ofHours(3), baseTime.plusHours(2));

    @BeforeEach
    void beforeEach() {
        epic = new Epic("Test Epic", "Epic description", epicId, Duration.ofMinutes(0), baseTime);
    }

    @Test
    void shouldCreateEpic() {
        assertEquals("Test Epic", epic.getTitle());
        assertEquals("Epic description", epic.getDescription());
        assertEquals(epicId, epic.getId());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertTrue(epic.getSubtasks().isEmpty());
    }

    @Test
    void shouldAddSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertEquals(2, epic.getSubtasks().size());
        assertTrue(epic.getSubtasks().contains(subtask1));
        assertTrue(epic.getSubtasks().contains(subtask2));
    }

    @Test
    void shouldRemoveSubtask() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        epic.removeSubtask(2);

        assertEquals(1, epic.getSubtasks().size());
        assertNotEquals(2, epic.getSubtasks().size());
        assertTrue(epic.getSubtasks().contains(subtask1));
    }

    @Test
    void shouldReturnSubtaskIds() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        ArrayList<Subtask> result = new ArrayList<>();
        result.add(subtask1);
        result.add(subtask2);

        assertArrayEquals(result.toArray(), epic.getSubtasks().toArray());
    }

    @Test
    void getEndTimeShouldReturnNullWhenNoSubtasks() {
        assertNull(epic.getEndTime());
    }

    @Test
    void getEndTimeShouldReturnSubtaskEndTimeWhenSingleSubtask() {
        epic.addSubtask(subtask1);

        LocalDateTime expectedEndTime = baseTime.plusHours(1).plusHours(1);
        assertEquals(expectedEndTime, epic.getEndTime());
    }

    @Test
    void getEndTimeShouldReturnLatestEndTimeWhenMultipleSubtasks() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        LocalDateTime expectedEndTime = baseTime.plusHours(2).plusHours(3);
        assertEquals(expectedEndTime, epic.getEndTime());
    }

    @Test
    void getEndTimeShouldUpdateWhenSubtasksAreModified() {
        epic.addSubtask(subtask1);
        LocalDateTime initialEndTime = epic.getEndTime();

        epic.addSubtask(subtask2);
        LocalDateTime updatedEndTime = epic.getEndTime();

        assertNotEquals(initialEndTime, updatedEndTime);
        assertTrue(updatedEndTime.isAfter(initialEndTime));
    }
}