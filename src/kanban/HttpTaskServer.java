package kanban;

import com.sun.net.httpserver.HttpServer;
import kanban.handlers.*;
import kanban.manager.TaskManager;
import kanban.util.Managers;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private HttpServer httpServer;

    public void start(TaskManager manager) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TaskHandler(manager));
            httpServer.createContext("/subtasks", new SubtaskHandler(manager));
            httpServer.createContext("/epics", new EpicHandler(manager));
            httpServer.createContext("/history", new HistoryHandler(manager));
            httpServer.createContext("/prioritized", new PrioritizedHandler(manager));

            httpServer.start();
            System.out.printf("Сервер запустился на порту %s", PORT);
        } catch (IOException exception) {
            System.out.println("При запуске сервера произошла ошибка");
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    public static void main(String[] args) {
        HttpTaskServer server = new HttpTaskServer();
        server.start(Managers.getFileBackedTaskManager(new File("tasks.csv")));
    }
}
