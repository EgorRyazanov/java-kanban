package kanban.manager;

import kanban.model.Epic;
import kanban.model.Subtask;
import kanban.model.Task;
import kanban.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private HistoryManager historyManager;
    private Task testTask1;
    private Task testTask2;
    private Epic testEpic;
    private Subtask testSubtask;

    @BeforeEach
    void beforeEach() {
        historyManager = new HistoryManager();
        testTask1 = new Task("Task 1", "Description", 1, TaskStatus.NEW);
        testTask2 = new Task("Task 2", "Description", 2, TaskStatus.IN_PROGRESS);
        testEpic = new Epic("Epic", "Description", 3);
        testSubtask = new Subtask("Subtask", "Description", 4, TaskStatus.NEW, 3);
    }

    @Test
    void shouldAddTaskToHistoryWhenTaskProvided() {
        historyManager.add(testTask1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(testTask1, history.get(0));
    }

    @Test
    void shouldAddDifferentTaskTypesToHistory() {
        historyManager.add(testTask1);
        historyManager.add(testEpic);
        historyManager.add(testSubtask);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertTrue(history.contains(testTask1));
        assertTrue(history.contains(testEpic));
        assertTrue(history.contains(testSubtask));
    }

    @Test
    void shouldMaintainOrderOfAddedTasks() {
        historyManager.add(testTask1);
        historyManager.add(testTask2);
        historyManager.add(testEpic);

        List<Task> history = historyManager.getHistory();
        assertEquals(testTask1, history.get(0));
        assertEquals(testTask2, history.get(1));
        assertEquals(testEpic, history.get(2));
    }

    @Test
    void shouldNotExceedMaxSizeLimit() {
        for (int i = 0; i < 11; i++) {
            Task task = new Task("Task " + i, "Desc", i, TaskStatus.NEW);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size());
        assertFalse(history.contains(new Task("Task 0", "Desc", 0, TaskStatus.NEW)));
        assertTrue(history.contains(new Task("Task 10", "Desc", 10, TaskStatus.NEW)));
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoTasksAdded() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
}