package kanban.manager;

import kanban.model.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    void beforeEach() throws IOException {
        historyManager = new InMemoryHistoryManager();
        tempFile = File.createTempFile("tasks", ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, true))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        manager = FileBackedTaskManager.createManager(historyManager, tempFile);
    }

    @AfterEach
    void afterEach() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void shouldSaveAndLoadEmptyTasks() throws IOException {
        List<Task> loadedTasks = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedTasks.isEmpty(), "Загруженный список задач должен быть пустым");
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        Task task = manager.addTask("Task 1", "Description", TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2025, 3, 1, 0, 0, 0, 0));
        Epic epic = manager.addEpic("Epic 1", "Epic description");
        Subtask subtask = manager.addSubtask("Subtask 1", "Sub desc", TaskStatus.IN_PROGRESS, epic.getId(), Duration.ofMinutes(0), LocalDateTime.of(2011, 1, 1, 0, 0, 0, 0));
        List<Task> loadedTasks = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(3, loadedTasks.size(), "Неверное количество загруженных задач");
        assertTrue(loadedTasks.contains(task), "Задача не найдена после загрузки");
        assertTrue(loadedTasks.contains(epic), "Эпик не найден после загрузки");
        assertTrue(loadedTasks.contains(subtask), "Подзадача не найдена после загрузки");
    }

    @Test
    void shouldSaveAndLoadTaskFieldsCorrectly() throws IOException {
        Task original = manager.addTask("Test Task", "Test Desc", TaskStatus.DONE, Duration.ofMinutes(15), LocalDateTime.of(2000, 2, 3, 0, 0, 0, 0));
        List<Task> loadedTasks = FileBackedTaskManager.loadFromFile(tempFile);
        Task loaded = loadedTasks.getFirst();

        assertEquals(original.getId(), loaded.getId(), "ID не совпадает");
        assertEquals(original.getTitle(), loaded.getTitle(), "Название не совпадает");
        assertEquals(original.getDescription(), loaded.getDescription(), "Описание не совпадает");
        assertEquals(original.getStatus(), loaded.getStatus(), "Статус не совпадает");
        assertEquals(original.getEndTime(), loaded.getEndTime(), "Время не совпадает");
    }

    @Test
    void shouldSaveAndLoadEpicWithoutSubtasks() throws IOException {
        Epic epic = manager.addEpic("Epic", "Epic desc");
        List<Task> loadedTasks = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = (Epic) loadedTasks.getFirst();

        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(TaskStatus.NEW, loadedEpic.getStatus(), "Статус эпика без подзадач должен быть NEW");
    }

    @Test
    void shouldLoadFromFileWithInitialValues() throws IOException {
        Task task = new Task("Task 1", "Desc 1", 1, TaskStatus.NEW, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        Epic epic = new Epic("Epic 1", "Epic desc", 2, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));
        Subtask subtask = new Subtask("Sub 1", "Sub desc", 3, TaskStatus.DONE, 2, Duration.ofMinutes(0), LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, true))) {
            writer.write(task.toString()); writer.newLine();
            writer.write(epic.toString()); writer.newLine();
            writer.write(subtask.toString()); writer.newLine();
        }

        FileBackedTaskManager loadedManager = FileBackedTaskManager.createManager(historyManager, tempFile);
        assert loadedManager != null;
        assertNotNull(loadedManager.getTask(1), "Задача не загрузилась");
        assertNotNull(loadedManager.getEpic(2), "Эпик не загрузился");
        assertNotNull(loadedManager.getSubtask(3), "Подзадача не загрузилась");
    }
}