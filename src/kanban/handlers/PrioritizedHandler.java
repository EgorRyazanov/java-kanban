package kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        if (endpoint.equals(Endpoint.GET_PRIORITIZED)) {
            handleGetPrioritized(exchange);
        } else {
            this.sendNotFound(exchange);
        }
    }

    private void handleGetPrioritized(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getPrioritizedTasks()));
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && requestMethod.equals("GET")) {
            return Endpoint.GET_PRIORITIZED;
        }

        return Endpoint.UNKNOWN;
    }

    enum Endpoint { GET_PRIORITIZED, UNKNOWN }
}
