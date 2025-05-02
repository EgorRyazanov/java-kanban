package kanban;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EpicTest {
    private Epic epic;
    private final int epicId = 1;

    @BeforeEach
    void beforeEach() {
        epic = new Epic("Test Epic", "Epic description", epicId);
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
        epic.addSubtask(2);
        epic.addSubtask(3);

        assertEquals(2, epic.getSubtaskIds().size());
        assertTrue(epic.getSubtaskIds().contains(2));
        assertTrue(epic.getSubtaskIds().contains(3));
    }

    @Test
    void shouldRemoveSubtask() {
        epic.addSubtask(2);
        epic.addSubtask(3);

        epic.removeSubtask(2);

        assertEquals(1, epic.getSubtaskIds().size());
        assertFalse(epic.getSubtaskIds().contains(2));
        assertTrue(epic.getSubtaskIds().contains(3));
    }

    @Test
    void shouldReturnSubtaskIds() {
        epic.addSubtask(2);
        epic.addSubtask(3);

        ArrayList<Integer> result = new ArrayList<>();
        result.add(2);
        result.add(3);

        assertArrayEquals(result.toArray(), epic.getSubtaskIds().toArray());
    }
}