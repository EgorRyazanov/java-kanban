package kanban.manager;

import kanban.exception.ManagerSaveException;
import kanban.model.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public static FileBackedTaskManager createManager(HistoryManager historyManager, File file) {
        try {
            List<Task> initialTasks = FileBackedTaskManager.loadFromFile(file);
            return new FileBackedTaskManager(historyManager, file, initialTasks);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public Task addTask(String title, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        Task task = super.addTask(title, description, status, duration, startTime);
        try {
            save(task);
        } catch (ManagerSaveException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }
        return task;
    }

    @Override
    public Epic addEpic(String title, String desc) {
        Epic task = super.addEpic(title, desc);
        try {
            save(task);
        } catch (ManagerSaveException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }

        return task;
    }

    @Override
    public Subtask addSubtask(String title, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        Subtask task = super.addSubtask(title,description,status,epicId, duration, startTime);
        try {
            save(task);
        } catch (ManagerSaveException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }

        return task;
    }

    public static List<Task> loadFromFile(File file) throws IOException {
        FileReader reader = new FileReader(file.getAbsolutePath());
        BufferedReader br = new BufferedReader(reader);
        List<Task> tasks = new ArrayList<>();
        br.readLine();

        while (br.ready()) {
            String line = br.readLine();
            tasks.add(fromString(line));
        }

        br.close();
        return tasks;
    }

    private FileBackedTaskManager(HistoryManager historyManager, File file, List<Task> initialValues) {
        super(historyManager);
        this.file = file;

        if (!initialValues.isEmpty()) {
            idCounter = initialValues.size() + 1;
        }

        for (Task task : initialValues) {
            if (task.getType().equals(TaskType.TASK)) {
                tasks.put(task.getId(), task);
            } else if (task.getType().equals(TaskType.EPIC)) {
                epics.put(task.getId(), (Epic) task);
            } else if (task.getType().equals(TaskType.SUBTASK)) {
                subtasks.put(task.getId(), (Subtask) task);
            }
        }
    }

    private void save(Task task) throws ManagerSaveException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String formattedTask = task.toString();
            writer.write(formattedTask);
            writer.newLine();
        } catch (IOException exception) {
            throw new ManagerSaveException(exception);
        }
    }

    private static Task fromString(String value) {
        String[] taskValues = value.split(",");
        TaskType type = TaskType.valueOf(taskValues[1]);
        int id = Integer.parseInt(taskValues[0]);
        String title = taskValues[2];
        TaskStatus status = TaskStatus.valueOf(taskValues[3]);
        String description = taskValues[4];
        Duration duration = Duration.ofMinutes(Integer.parseInt(taskValues[5]));
        LocalDateTime startTime = null;
        if (taskValues.length == 7) {
            startTime = LocalDateTime.parse(taskValues[6], DateTimeFormatter.ISO_DATE_TIME);
        }
        if (type == TaskType.TASK) {
            return new Task(title, description, id, status, duration, startTime);
        } else if (type == TaskType.EPIC) {
            return new Epic(title, description, id, duration, startTime);
        } else if (type == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(taskValues[5]);
            return new Subtask(title, description,id, status, epicId, duration, startTime);
        }

        return null;
    }
}
