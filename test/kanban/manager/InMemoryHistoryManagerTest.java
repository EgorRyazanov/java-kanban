package kanban.manager;

import kanban.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private Epic testEpic;
    private Subtask testSubtask;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        testTask1 = new Task("Task 1", "Description", 1, TaskStatus.NEW);
        testTask2 = new Task("Task 2", "Description", 2, TaskStatus.IN_PROGRESS);
        testTask3 = new Task("Task 3", "Description", 3, TaskStatus.IN_PROGRESS);
        testEpic = new Epic("Epic", "Description", 3);
        testSubtask = new Subtask("Subtask", "Description", 4, TaskStatus.NEW, 3);
    }

    @Test
    void shouldRemoveTaskWhenSameTaskWasProvidedAgain() {
        historyManager.add(testTask1);
        historyManager.add(testTask2);
        historyManager.add(testTask3);
        historyManager.add(testTask2);
        historyManager.add(testTask1);
        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(testTask3, history.get(0));
        assertEquals(testTask2, history.get(1));
        assertEquals(testTask1, history.get(2));
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
    void shouldReturnEmptyHistoryWhenNoTasksAdded() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
}