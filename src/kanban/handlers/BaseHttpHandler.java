package kanban.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import kanban.adapters.DurationAdapter;
import kanban.adapters.EpicAdapter;
import kanban.adapters.LocalDateTimeAdapter;
import kanban.model.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    protected Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(Epic.class, new EpicAdapter())
            .serializeNulls()
            .create();

    protected void sendText(HttpExchange httpExchange, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, resp.length);
        httpExchange.getResponseBody().write(resp);
        httpExchange.close();
    }

    protected void sendNotFound(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(404, 0);
        httpExchange.close();
    }

    protected void sendCreated(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(201, 0);
        httpExchange.close();
    }

    protected void sendHasInteractions(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(406, 0);
        httpExchange.close();
    }
}
