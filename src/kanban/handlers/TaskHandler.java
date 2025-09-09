package kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.exception.NotFoundException;
import kanban.manager.TaskManager;
import kanban.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public TaskHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case Endpoint.GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case Endpoint.GET_TASK_BY_ID: {
                handleGetTaskById(exchange);
                break;
            }
            case Endpoint.POST_TASK: {
                handlePostTask(exchange);
                break;
            }
            case Endpoint.DELETE_TASK: {
                handleRemoveTaskById(exchange);
                break;
            }
            default:
                this.sendNotFound(exchange);
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && requestMethod.equals("GET")) {
            return Endpoint.GET_TASKS;
        } else if (pathParts.length == 3 && requestMethod.equals("GET")) {
            return Endpoint.GET_TASK_BY_ID;
        } else if (pathParts.length == 2 && requestMethod.equals("POST")) {
            return Endpoint.POST_TASK;
        } else if (pathParts.length == 3 && requestMethod.equals("DELETE")) {
            return Endpoint.DELETE_TASK;
        }

        return Endpoint.UNKNOWN;
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getAllTasks()));
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int taskId = Integer.parseInt(pathParts[2]);
            sendText(exchange, gson.toJson(manager.getTask(taskId)));
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
        }
    }

    private void handleRemoveTaskById(HttpExchange exchange) throws IOException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            int taskId = Integer.parseInt(pathParts[2]);
            manager.removeTask(taskId);
            sendText(exchange, "Задача успешно удалена");
        } catch (NumberFormatException | NotFoundException exception) {
            sendNotFound(exchange);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String unparsedTask = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Task task = gson.fromJson(unparsedTask, Task.class);

        if (!manager.checkTimeTaskAvailable(task.getStartTime(), task.getDuration())) {
            sendHasInteractions(exchange);
            return;
        }

        if (task.getId() != 0) {
            manager.updateTask(task);
        } else {
            manager.addTask(task.getTitle(), task.getDescription(), task.getStatus(), task.getDuration(), task.getStartTime());
        }

        sendCreated(exchange);
    }

    enum Endpoint {GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK, UNKNOWN}
}
