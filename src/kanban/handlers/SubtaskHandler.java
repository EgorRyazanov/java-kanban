package kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.exception.NotFoundException;
import kanban.manager.TaskManager;
import kanban.model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public SubtaskHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case Endpoint.GET_SUBTASKS: {
                handleGetSubtasks(exchange);
                break;
            }
            case Endpoint.GET_SUBTASK_BY_ID: {
                handleGetSubtaskById(exchange);
                break;
            }
            case Endpoint.POST_SUBTASK: {
                handlePostSubtask(exchange);
                break;
            }
            case Endpoint.DELETE_SUBTASK: {
                handleRemoveSubtaskById(exchange);
                break;
            }
            default:
                this.sendNotFound(exchange);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && requestMethod.equals("GET")) {
            return Endpoint.GET_SUBTASKS;
        } else if (pathParts.length == 3 && requestMethod.equals("GET")) {
            return Endpoint.GET_SUBTASK_BY_ID;
        } else if (pathParts.length == 2 && requestMethod.equals("POST")) {
            return Endpoint.POST_SUBTASK;
        } else if (pathParts.length == 3 && requestMethod.equals("DELETE")) {
            return Endpoint.DELETE_SUBTASK;
        }

        return Endpoint.UNKNOWN;
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getAllSubtasks()));
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int subtaskId = Integer.parseInt(pathParts[2]);
            sendText(exchange, gson.toJson(manager.getSubtask(subtaskId)));
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
        }
    }

    private void handleRemoveSubtaskById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int subtaskId = Integer.parseInt(pathParts[2]);
            manager.removeSubtask(subtaskId);
            sendText(exchange, "Подзадача успешно удалена");
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        String unparsedTask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(unparsedTask, Subtask.class);

        if (!manager.checkTimeTaskAvailable(subtask.getStartTime(), subtask.getDuration())) {
            sendHasInteractions(exchange);
            return;
        }

        if (subtask.getId() != 0) {
            manager.updateSubtask(subtask);
        } else {
            manager.addSubtask(subtask.getTitle(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicId(), subtask.getDuration(), subtask.getStartTime());
        }

        sendCreated(exchange);
    }

    enum Endpoint {GET_SUBTASKS, GET_SUBTASK_BY_ID, POST_SUBTASK, DELETE_SUBTASK, UNKNOWN}
}
