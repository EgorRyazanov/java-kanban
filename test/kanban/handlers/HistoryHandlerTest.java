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

class HistoryHandlerTest {

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
    void shouldReturnEmptyHistoryWhenNoTasksViewed() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnHistoryWithTasks() throws IOException, InterruptedException {
        Task task1 = manager.addTask("Test Task 1", "Description 1", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = manager.addTask("Test Task 2", "Description 2", TaskStatus.NEW,
                Duration.ofMinutes(25), LocalDateTime.now().plusHours(2));
        Task task3 = manager.addTask("Test Task 3", "Description 3", TaskStatus.NEW,
                Duration.ofMinutes(40), LocalDateTime.now().plusHours(2));

        manager.getTask(task3.getId());
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] history = gson.fromJson(response.body(), Task[].class);

        assertEquals(200, response.statusCode());
        assertEquals(3, history.length);
        assertEquals(history[0], task3);
        assertEquals(history[1], task1);
        assertEquals(history[2], task2);
    }

    @Test
    void shouldReturnHistoryInCorrectOrder() throws IOException, InterruptedException {
        Task task1 = manager.addTask("Task 1", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Task task2 = manager.addTask("Task 2", "Description", TaskStatus.NEW,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task1.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] history = gson.fromJson(response.body(), Task[].class);

        assertEquals(200, response.statusCode());
        assertEquals(2, history.length);

        assertEquals(task2, history[0]);
        assertEquals(task1, history[1]);
    }

    @Test
    void shouldReturnNotFoundForInvalidHistoryEndpoint() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/history/invalid"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnEmptyHistoryAfterTaskDeletion() throws IOException, InterruptedException {
        Task task = manager.addTask("Test Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        manager.getTask(task.getId());

        manager.removeTask(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }
}