package kanban.manager;

import kanban.exception.ManagerSaveException;
import kanban.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file, List<Task> initialValues) {
        super(historyManager);
        this.file = file;

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

    @Override
    public Task addTask(String title, String description, TaskStatus status) {
        Task task = super.addTask(title, description, status);
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
    public Subtask addSubtask(String title, String description, TaskStatus status, int epicId) {
        Subtask task = super.addSubtask(title,description,status,epicId);
        try {
            save(task);
        } catch (ManagerSaveException exception) {
            System.out.println(exception.getMessage());
            exception.printStackTrace();
        }

        return task;
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
        String[] taskValues = value.split("\\,");
        TaskType type = TaskType.valueOf(taskValues[1]);
        int id = Integer.parseInt(taskValues[0]);
        String title = taskValues[2];
        TaskStatus status = TaskStatus.valueOf(taskValues[3]);
        String description = taskValues[4];
        if (type == TaskType.TASK) {
            return new Task(title, description, id, status);
        } else if (type == TaskType.EPIC) {
            return new Epic(title, description, id);
        } else if (type == TaskType.SUBTASK) {
            int epicId = Integer.parseInt(taskValues[5]);
            return new Subtask(title, description,id, status, epicId);
        }

        return null;
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
}
