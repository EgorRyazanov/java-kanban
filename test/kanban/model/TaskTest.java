package kanban.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {

    @Test
    void shouldReturnTitle() {
        Task task = new Task("Title", "Description", 1, TaskStatus.NEW);
        assertEquals("Title", task.getTitle());
    }

    @Test
    void shouldReturnId() {
        Task task = new Task("Title", "Description", 42, TaskStatus.NEW);
        assertEquals(42, task.getId());
    }

    @Test
    void shouldReturnStatus() {
        Task task = new Task("Title", "Description", 1, TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void shouldChangeStatus() {
        Task task = new Task("Title", "Description", 1, TaskStatus.NEW);
        task.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void tastToString() {
        Task task = new Task("Test Task", "Test Description", 123, TaskStatus.DONE);
        String expected = "123: Test Task (DONE)";
        assertEquals(expected, task.toString());
    }

    @Test
    void testEquals() {
        Task task1 = new Task("Task", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task", "Desc", 1, TaskStatus.DONE);
        Task task3 = new Task("Different", "Desc", 1, TaskStatus.NEW);
        Task task4 = new Task("Task", "Desc", 2, TaskStatus.NEW);

        assertEquals(task1, task1);
        assertEquals(task1, task2);
        assertEquals(task2, task1);
        assertNotEquals(task1, task3);
        assertNotEquals(task1, task4);
        assertNotEquals(null, task1);
    }

    @Test
    void testHashCode() {
        Task task1 = new Task("Task", "Desc", 1, TaskStatus.NEW);
        Task task2 = new Task("Task", "Desc", 1, TaskStatus.DONE);
        Task task3 = new Task("Different", "Desc", 1, TaskStatus.NEW);

        assertEquals(task1.hashCode(), task2.hashCode());
        assertNotEquals(task1.hashCode(), task3.hashCode());
    }
}