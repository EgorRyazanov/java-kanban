package kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import kanban.manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;

    public HistoryHandler(TaskManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        if (endpoint.equals(Endpoint.GET_HISTORY)) {
            handleGetHistory(exchange);
        } else {
            this.sendNotFound(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getHistory()));
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && requestMethod.equals("GET")) {
            return Endpoint.GET_HISTORY;
        }

        return Endpoint.UNKNOWN;
    }

    enum Endpoint {GET_HISTORY, UNKNOWN}
}
