package kanban;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldReturnTaskManagerInstance() {
        Managers managers = new Managers();
        TaskManager taskManager = managers.getDefault();

        assertNotNull(taskManager, "Должен возвращаться экземпляр TaskManager");
        assertTrue(taskManager instanceof InMemoryTaskManager,
                "Должна возвращаться реализация InMemoryTaskManager");
    }

    @Test
    void shouldCreateIndependentInstancesForDifferentManagers() {
        Managers managers1 = new Managers();
        Managers managers2 = new Managers();

        assertNotSame(managers1.getDefault(), managers2.getDefault(),
                "Разные экземпляры Managers должны создавать разные TaskManager");

        assertNotSame(managers1.getDefaultHistory(), managers2.getDefaultHistory(),
                "Разные экземпляры Managers должны создавать разные HistoryManager");
    }
}