package kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.exception.NotFoundException;
import kanban.manager.TaskManager;
import kanban.model.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public EpicHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_EPICS: {
                handleGetEpics(exchange);
                break;
            }
            case Endpoint.GET_EPIC_BY_ID: {
                handleGetEpicById(exchange);
                break;
            }
            case Endpoint.POST_EPIC: {
                handlePostEpic(exchange);
                break;
            }
            case Endpoint.DELETE_EPIC: {
                handleRemoveEpicById(exchange);
                break;
            }
            case Endpoint.GET_SUBTASKS_BY_EPIC_ID: {
                handleGetEpicComments(exchange);
                break;
            }
            default:
                this.sendNotFound(exchange);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && requestMethod.equals("GET")) {
            return Endpoint.GET_EPICS;
        } else if (pathParts.length == 3 && requestMethod.equals("GET")) {
            return Endpoint.GET_EPIC_BY_ID;
        } else if (pathParts.length == 2 && requestMethod.equals("POST")) {
            return Endpoint.POST_EPIC;
        } else if (pathParts.length == 3 && requestMethod.equals("DELETE")) {
            return Endpoint.DELETE_EPIC;
        } else if (pathParts.length == 4 && requestMethod.equals("GET") && pathParts[3].contains("subtasks")) {
            return Endpoint.GET_SUBTASKS_BY_EPIC_ID;
        }

        return Endpoint.UNKNOWN;
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getAllEpics()));
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int epicId = Integer.parseInt(pathParts[2]);
            sendText(exchange, gson.toJson(manager.getEpic(epicId)));
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicComments(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int epicId = Integer.parseInt(pathParts[2]);
            sendText(exchange, gson.toJson(manager.getSubtasksByEpic(epicId)));
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
        }
    }

    private void handleRemoveEpicById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int epicId = Integer.parseInt(pathParts[2]);
            manager.removeEpic(epicId);
            sendText(exchange, "Эпик успешно удален");
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String unparsedTask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Epic epic = gson.fromJson(unparsedTask, Epic.class);

        manager.addEpic(epic.getTitle(), epic.getDescription());
        sendCreated(exchange);
    }

    enum Endpoint {GET_EPICS, GET_EPIC_BY_ID, POST_EPIC, DELETE_EPIC, GET_SUBTASKS_BY_EPIC_ID, UNKNOWN}
}
