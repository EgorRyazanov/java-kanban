package kanban.handlers;

import com.google.gson.*;
import kanban.HttpTaskServer;
import kanban.adapters.DurationAdapter;
import kanban.adapters.LocalDateTimeAdapter;
import kanban.manager.TaskManager;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest {

    private HttpTaskServer taskServer;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;
    private final int PORT = HttpTaskServer.PORT;

    @BeforeEach
    void beforeEach() throws IOException {
        File tempFile = File.createTempFile("tasks_test", ".csv");
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
    void shouldReturnEmptyListWhenNoTaskAdded() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnAllTasks() throws IOException, InterruptedException {
        Task task = manager.addTask("Test Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task[] returnedTasks = gson.fromJson(response.body(), Task[].class);

        assertEquals(200, response.statusCode());
        assertEquals(returnedTasks[0], task);
    }

    @Test
    void shouldReturnTaskById() throws IOException, InterruptedException {
        Task task = manager.addTask("Test Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task returnedTask = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode());
        assertEquals(task, returnedTask);
    }

    @Test
    void shouldReturnNotFoundWhenNoTaskById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldCreateNewTask() throws IOException, InterruptedException {
        Task task = new Task("New Task", "Description", 1, TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals(task, tasks.getFirst());
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        Task existingTask = manager.addTask("Original Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        Task updatedTask = new Task("Updated Task", "New Description", existingTask.getId(),
                TaskStatus.IN_PROGRESS, Duration.ofMinutes(45), LocalDateTime.of(2001, 12, 12, 12, 12, 12));
        String taskJson = gson.toJson(updatedTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task taskFromManager = manager.getTask(existingTask.getId());
        assertEquals(taskFromManager, updatedTask);
    }

    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {
        Task task = manager.addTask("Task to delete", "Description", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Задача успешно удалена"));
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    void shouldReturnNotFoundWhenNoTaskToDelete() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnConflict() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now();
        manager.addTask("First Task", "Description", TaskStatus.NEW,
                Duration.ofMinutes(60), startTime);

        Task conflictingTask = new Task("Conflicting Task", "Description", 0, TaskStatus.NEW,
                Duration.ofMinutes(30), startTime.plusMinutes(30));
        String taskJson = gson.toJson(conflictingTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    void shouldReturnNotFoundWhenUnknownURL() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/invalid"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}