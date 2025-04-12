import kanban.*;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final TaskManager manager = new TaskManager();

    public static void main(String[] args) {
        while (true) {
            printMenu();
            String input = scanner.next();

            switch (input) {
                case "1":
                    createTask();
                    break;
                case "2":
                    createEpic();
                    break;
                case "3":
                    createSubtask();
                    break;
                case "4":
                    listAllTasks();
                    break;
                case "5":
                    listAllEpics();
                    break;
                case "6":
                    listAllSubtasks();
                    break;
                case "7":
                    listSubtasksByEpic();
                    break;
                case "8":
                    updateSubtaskStatus();
                    break;
                case "9":
                    removeTaskById();
                    break;
                case "0": {
                    System.out.println("Выход.");
                    return;
                }
                default:
                    System.out.println("Неверный ввод.");
                    break;
            }
        }
    }

    private static void printMenu() {
        System.out.println("1 – Создать задачу");
        System.out.println("2 – Создать эпик");
        System.out.println("3 – Создать подзадачу");
        System.out.println("4 – Показать все задачи");
        System.out.println("5 – Показать все эпики");
        System.out.println("6 – Показать все подзадачи");
        System.out.println("7 – Показать подзадачи эпика");
        System.out.println("8 – Обновить статус подзадачи");
        System.out.println("9 – Удалить задачу/эпик/подзадачу по ID");
        System.out.println("0 – Выход");
        System.out.println("Выберите пункт: ");
    }


    private static void createTask() {
        System.out.println("Введите название задачи: ");
        String title = scanner.next();
        System.out.println("Введите описание задачи: ");
        String description = scanner.next();
        int id = manager.generateId();
        Task task = new Task(title, description, id, TaskStatus.NEW);
        manager.addTask(task);
        System.out.println("Задача создана с ID: " + id);
    }

    private static void createEpic() {
        System.out.println("Введите название эпика: ");
        String title = scanner.next();
        System.out.println("Введите описание эпика: ");
        String description = scanner.next();
        int id = manager.generateId();
        Epic epic = new Epic(title, description, id);
        manager.addEpic(epic);
        System.out.println("Эпик создан с ID: " + id);
    }

    private static void createSubtask() {
        System.out.println("Введите ID эпика, к которому относится подзадача: ");
        int epicId = scanner.nextInt();
        Epic epic = manager.getEpic(epicId);
        if (epic == null) {
            System.out.println("Эпик с таким ID не найден.");
            return;
        }
        System.out.println("Введите название подзадачи: ");
        String title = scanner.next();
        System.out.println("Введите описание подзадачи: ");
        String description = scanner.next();
        int id = manager.generateId();
        Subtask subtask = new Subtask(title, description, id, TaskStatus.NEW, epicId);
        manager.addSubtask(subtask);
        System.out.println("Подзадача создана с ID: " + id);
    }

    private static void listAllTasks() {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
    }

    private static void listAllEpics() {
        System.out.println("Эпики: ");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);
        }
    }

    private static void listAllSubtasks() {
        System.out.println("Подзадачи:");
        for (Subtask sub : manager.getAllSubtasks()) {
            System.out.println(sub);
        }
    }

    private static void listSubtasksByEpic() {
        System.out.println("Введите ID эпика: ");
        int epicId = scanner.nextInt();
        ArrayList<Subtask> subtasks = manager.getSubtasksByEpic(epicId);
        if (subtasks.isEmpty()) {
            System.out.println("Нет подзадач для этого эпика.");
        } else {
            for (Subtask sub : subtasks) {
                System.out.println(sub);
            }
        }
    }

    private static void updateSubtaskStatus() {
        System.out.println("Введите ID подзадачи: ");
        int id = scanner.nextInt();
        Subtask sub = manager.getAllSubtasks().get(id);
        if (sub == null) {
            System.out.println("Подзадача не найдена.");
            return;
        }

        System.out.println("Текущий статус: " + sub.getStatus());
        System.out.println("Введите новый статус (NEW, IN_PROGRESS, DONE): ");
        String statusStr = scanner.next().toUpperCase();

        try {
            TaskStatus newStatus = TaskStatus.valueOf(statusStr);
            sub.setStatus(newStatus);
            manager.updateSubtask(sub);
            System.out.println("Статус обновлён.");
        } catch (IllegalArgumentException e) {
            System.out.println("Некорректный статус.");
        }
    }

    private static void removeTaskById() {
        System.out.println("Введите ID: ");
        int id = scanner.nextInt();

        if (manager.getTask(id) != null) {
            manager.removeTask(id);
            System.out.println("Задача удалена.");
        } else if (manager.getEpic(id) != null) {
            manager.removeEpic(id);
            System.out.println("Эпик и его подзадачи удалены.");
        } else if (manager.getAllSubtasks().get(id) != null) {
            manager.removeSubtask(id);
            System.out.println("Подзадача удалена.");
        } else {
            System.out.println("Задача с таким ID не найдена.");
        }
    }
}
