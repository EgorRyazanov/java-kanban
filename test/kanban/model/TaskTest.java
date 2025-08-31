package kanban.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {
    @Test
    void shouldReturnTitle() {
        Task task = new Task("Title", "Description", 1, TaskStatus.NEW , Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        assertEquals("Title", task.getTitle());
    }

    @Test
    void shouldReturnId() {
        Task task = new Task("Title", "Description", 42, TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        assertEquals(42, task.getId());
    }

    @Test
    void shouldReturnStatus() {
        Task task = new Task("Title", "Description", 1, TaskStatus.IN_PROGRESS, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    }

    @Test
    void shouldChangeStatus() {
        Task task = new Task("Title", "Description", 1, TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        task.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getStatus());
    }

    @Test
    void taskToString() {
        Task task = new Task("Test Task", "Test Description", 123, TaskStatus.DONE, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        String expected = "123,TASK,Test Task,DONE,Test Description,0,2000-01-01T00:00:00";
        assertEquals(expected, task.toString());
    }

    @Test
    void testEquals() {
        Task task1 = new Task("Task", "Desc", 1, TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        Task task2 = new Task("Task", "Desc", 1, TaskStatus.DONE, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        Task task3 = new Task("Different", "Desc", 1, TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        Task task4 = new Task("Task", "Desc", 2, TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));

        assertEquals(task1, task1);
        assertEquals(task1, task2);
        assertEquals(task2, task1);
        assertNotEquals(task1, task3);
        assertNotEquals(task1, task4);
        assertNotEquals(null, task1);
    }

    @Test
    void testHashCode() {
        Task task1 = new Task("Task", "Desc", 1, TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        Task task2 = new Task("Task", "Desc", 1, TaskStatus.DONE, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));

        assertEquals(task1.hashCode(), task2.hashCode());
    }
}