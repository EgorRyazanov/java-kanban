package kanban.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kanban.HttpTaskServer;
import kanban.adapters.DurationAdapter;
import kanban.adapters.LocalDateTimeAdapter;
import kanban.manager.TaskManager;
import kanban.model.Epic;
import kanban.model.Subtask;
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

class SubtaskHandlerTest {

    private HttpTaskServer taskServer;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;
    private final int PORT = HttpTaskServer.PORT;
    private Epic testEpic;

    @BeforeEach
    void beforeEach() throws IOException {
        File tempFile = File.createTempFile("subtasks_test", ".csv");
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

        testEpic = manager.addEpic("Test Epic", "Epic Description");
    }

    @AfterEach
    void afterEach() {
        if (taskServer != null) {
            taskServer.stop();
        }
    }

    @Test
    void shouldReturnEmptyListWhenNoSubtaskAdded() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnAllSubtasks() throws IOException, InterruptedException {
        Subtask subtask = manager.addSubtask("Test Subtask", "Description", TaskStatus.NEW,
                testEpic.getId(), Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] returnedSubtasks = gson.fromJson(response.body(), Subtask[].class);

        assertEquals(200, response.statusCode());
        assertEquals(1, returnedSubtasks.length);
        assertEquals(returnedSubtasks[0], subtask);
    }

    @Test
    void shouldReturnSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = manager.addSubtask("Test Subtask", "Description", TaskStatus.NEW,
                testEpic.getId(), Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Subtask returnedSubtask = gson.fromJson(response.body(), Subtask.class);

        assertEquals(200, response.statusCode());
        assertEquals(returnedSubtask, subtask);
    }

    @Test
    void shouldReturnNotFoundWhenNoSubtaskById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldCreateNewSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("New Subtask", "Description", 2, TaskStatus.NEW,
                testEpic.getId(), Duration.ofMinutes(30), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals(subtasks.getFirst(), subtask);
    }

    @Test
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        Subtask existingSubtask = manager.addSubtask("Original Subtask", "Description", TaskStatus.NEW,
                testEpic.getId(), Duration.ofMinutes(30), LocalDateTime.now());

        Subtask updatedSubtask = new Subtask("Updated Subtask", "New Description", existingSubtask.getId(),
                TaskStatus.IN_PROGRESS, testEpic.getId(), Duration.ofMinutes(45),
                LocalDateTime.of(2001, 12, 12, 12, 12, 12));
        String subtaskJson = gson.toJson(updatedSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Subtask subtaskFromManager = manager.getSubtask(existingSubtask.getId());
        assertEquals(updatedSubtask, subtaskFromManager);
    }

    @Test
    void shouldDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = manager.addSubtask("Subtask to delete", "Description", TaskStatus.NEW,
                testEpic.getId(), Duration.ofMinutes(30), LocalDateTime.now());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Подзадача успешно удалена"));
        assertEquals(0, manager.getAllSubtasks().size());
    }

    @Test
    void shouldReturnNotFoundWhenNoSubtaskToDelete() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnConflict() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now();
        manager.addSubtask("First Subtask", "Description", TaskStatus.NEW,
                testEpic.getId(), Duration.ofMinutes(60), startTime);

        Subtask conflictingSubtask = new Subtask("Conflicting Subtask", "Description", 0, TaskStatus.NEW,
                testEpic.getId(), Duration.ofMinutes(30), startTime.plusMinutes(30));
        String subtaskJson = gson.toJson(conflictingSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
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