package kanban.util;

import kanban.manager.InMemoryTaskManager;
import kanban.manager.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldReturnTaskManagerInstance() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager, "Должен возвращаться экземпляр TaskManager");
        assertTrue(taskManager instanceof InMemoryTaskManager,
                "Должна возвращаться реализация InMemoryTaskManager");
    }

    @Test
    void shouldCreateIndependentInstancesForDifferentManagers() {
        TaskManager manager1 = Managers.getDefault();
        TaskManager manager2 = Managers.getDefault();

        assertNotSame(manager1, manager2,
                "getDefault каждый раз должен создавать новые экземпляры TaskManager");
    }
}