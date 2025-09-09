package kanban.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kanban.HttpTaskServer;
import kanban.adapters.DurationAdapter;
import kanban.adapters.EpicAdapter;
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

class EpicHandlerTest {

    private HttpTaskServer taskServer;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;
    private final int PORT = HttpTaskServer.PORT;

    @BeforeEach
    void beforeEach() throws IOException {
        File tempFile = File.createTempFile("epics_test", ".csv");
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
    void shouldReturnEmptyListWhenNoEpicAdded() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnAllEpics() throws IOException, InterruptedException {
        Epic epic = manager.addEpic("Test Epic", "Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic[] returnedEpics = gson.fromJson(response.body(), Epic[].class);

        assertEquals(200, response.statusCode());
        assertEquals(1, returnedEpics.length);
        assertEquals(epic, returnedEpics[0]);
    }

    @Test
    void shouldReturnEpicById() throws IOException, InterruptedException {
        Epic epic = manager.addEpic("Test Epic", "Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Epic returnedEpic = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertEquals(epic, returnedEpic);
    }

    @Test
    void shouldReturnNotFoundWhenNoEpicById() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldCreateNewEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("New Epic", "Description", 1, Duration.ofMinutes(0), LocalDateTime.of(2001, 2, 3, 4, 1, 2));
        String epicJson = gson.toJson(epic);
        epic.setEndDate(LocalDateTime.of(2, 3, 5, 6 ,7 ,8));
        epic.addSubtask(2);
        epic.addSubtask(3);
        epic.addSubtask(4);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epics = manager.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals(epic, epics.getFirst());
    }

    @Test
    void shouldDeleteEpic() throws IOException, InterruptedException {
        Epic epic = manager.addEpic("Epic to delete", "Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Эпик успешно удален"));
        assertEquals(0, manager.getAllEpics().size());
    }

    @Test
    void shouldReturnNotFoundWhenNoEpicToDelete() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldReturnSubtasksByEpicId() throws IOException, InterruptedException {
        Epic epic = manager.addEpic("Test Epic", "Description");
        Subtask subtask1 = manager.addSubtask("Subtask 1", "Description 1", TaskStatus.NEW,
                epic.getId(), Duration.ofMinutes(30), LocalDateTime.now());
        Subtask subtask2 = manager.addSubtask("Subtask 2", "Description 2", TaskStatus.IN_PROGRESS,
                epic.getId(), Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask[] returnedSubtasks = gson.fromJson(response.body(), Subtask[].class);

        assertEquals(200, response.statusCode());
        assertEquals(2, returnedSubtasks.length);
        assertEquals(subtask1, returnedSubtasks[0]);
        assertEquals(subtask2, returnedSubtasks[1]);
    }

    @Test
    void shouldReturnEmptySubtasksForEpicWithoutSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.addEpic("Test Epic", "Description");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnNotFoundWhenGettingSubtasksForNonExistentEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/999/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
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

    @Test
    void shouldDeleteEpicWithSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.addEpic("Epic with subtasks", "Description");
        manager.addSubtask("Subtask 1", "Description 1", TaskStatus.NEW,
                epic.getId(), Duration.ofMinutes(30), LocalDateTime.now());
        manager.addSubtask("Subtask 2", "Description 2", TaskStatus.DONE,
                epic.getId(), Duration.ofMinutes(45), LocalDateTime.now().plusHours(1));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + PORT + "/epics/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Эпик успешно удален"));
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubtasks().size());
    }
}