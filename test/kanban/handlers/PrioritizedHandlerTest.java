package kanban.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kanban.HttpTaskServer;
import kanban.adapters.DurationAdapter;
import kanban.adapters.EpicAdapter;
import kanban.adapters.LocalDateTimeAdapter;
import kanban.manager.TaskManager;
import kanban.model.Epic;
import kanban.model.Task;
import kanban.model.TaskStatus;
import kanban.util.Managers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest {
    private HttpTaskServer taskServer;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;
    private final int PORT = HttpTaskServer.PORT;

    @BeforeEach
    void beforeEach() throws IOException {
        File tempFile = File.createTempFile("history_test", ".csv");
        tempFile.deleteOnExit();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, true))) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        manager = Managers.getFileBackedTaskManager(tempFile);
        taskServer = new HttpTaskServer();
        taskServer.start(manager);

        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .serializeNulls()
                .create();
    }

    @AfterEach
    void afterEach() {
        if (taskServer != null) {
            taskServer.stop();
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoPrioritizedTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnPrioritizedTasksInCorrectOrder() throws IOException, InterruptedException {
        Task task1 = manager.addTask("Task 1 - Early", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 0)); // Раннее время

        Task task2 = manager.addTask("Task 2 - Late", "Description", TaskStatus.NEW,
                Duration.ofMinutes(45), LocalDateTime.of(2024, 1, 1, 12, 0)); // Позднее время

        Task task3 = manager.addTask("Task 3 - Middle", "Description", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 11, 0)); // Среднее время

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);

        assertEquals(200, response.statusCode());
        assertEquals(3, prioritizedTasks.length);

        assertEquals(task1, prioritizedTasks[0]);
        assertEquals(task3, prioritizedTasks[1]);
        assertEquals(task2, prioritizedTasks[2]);
    }

    @Test
    void shouldReturnNotFoundForInvalidPrioritizedEndpoint() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/prioritized/invalid"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnMethodNotAllowedForPost() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/prioritized"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode()); // POST не обрабатывается
    }

    @Test
    void shouldHandleEmptyPrioritizedListAfterTaskDeletion() throws IOException, InterruptedException {
        Task task = manager.addTask("Task to delete", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        manager.removeTask(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnCorrectPrioritizedOrderWithMultipleTasks() throws IOException, InterruptedException {
        Task task3 = manager.addTask("Task 3 - 11:00", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 11, 0));

        Task task1 = manager.addTask("Task 1 - 09:00", "Description", TaskStatus.NEW,
                Duration.ofMinutes(45), LocalDateTime.of(2024, 1, 1, 9, 0));

        Task task2 = manager.addTask("Task 2 - 10:30", "Description", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2024, 1, 1, 10, 30));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);

        assertEquals(200, response.statusCode());
        assertEquals(3, prioritizedTasks.length);

        assertEquals(task1, prioritizedTasks[0]);
        assertEquals(task2, prioritizedTasks[1]);
        assertEquals(task3, prioritizedTasks[2]);
    }

    @Test
    void shouldReturnOnlyTasksWithTimeInPrioritizedList() throws IOException, InterruptedException {
        Task taskWithTime = manager.addTask("With time", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 10, 0));

        manager.addTask("Without time", "Description", TaskStatus.NEW,
                Duration.ofMinutes(45), null); // Без времени

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);

        assertEquals(200, response.statusCode());
        assertEquals(1, prioritizedTasks.length);

        assertEquals(taskWithTime, prioritizedTasks[0]);
    }
}