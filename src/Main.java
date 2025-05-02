import kanban.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Managers managers = new Managers();
    private static final TaskManager taskManager = managers.getDefault();
    private static final HistoryManager historyManager = managers.getDefaultHistory();

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
                    getTask();
                    break;
                case "5":
                    getEpic();
                    break;
                case "6":
                    getSubTask();
                    break;
                case "7":
                    listAllTasks();
                    break;
                case "8":
                    listAllEpics();
                    break;
                case "9":
                    listAllSubtasks();
                    break;
                case "10":
                    listSubtasksByEpic();
                    break;
                case "11":
                    updateSubtaskStatus();
                    break;
                case "12":
                    removeTaskById();
                    break;
                case "13":
                    getHistory();
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
        System.out.println("4 – Показать задачу");
        System.out.println("5 – Показать эпик");
        System.out.println("6 – Показать подзадачу");
        System.out.println("7 – Показать все задачи");
        System.out.println("8 – Показать все эпики");
        System.out.println("9 – Показать все подзадачи");
        System.out.println("10 – Показать подзадачи эпика");
        System.out.println("11 – Обновить статус подзадачи");
        System.out.println("12 – Удалить задачу/эпик/подзадачу по ID");
        System.out.println("13 - Вывести историю последних 10 просмотров");
        System.out.println("0 – Выход");
        System.out.println("Выберите пункт: ");
    }


    private static void createTask() {
        System.out.println("Введите название задачи: ");
        String title = scanner.next();
        System.out.println("Введите описание задачи: ");
        String description = scanner.next();
        taskManager.addTask(title, description, TaskStatus.NEW);
    }

    private static void getTask() {
        System.out.println("Введите ID задачи: ");
        int id = scanner.nextInt();
        Task task = taskManager.getTask(id);
        System.out.println(task);
    }

    private static void getEpic() {
        System.out.println("Введите ID эпика: ");
        int id = scanner.nextInt();
        Epic epic = taskManager.getEpic(id);
        System.out.println(epic);
    }

    private static void getSubTask() {
        System.out.println("Введите ID подзадачи: ");
        int id = scanner.nextInt();
        Subtask subtask = taskManager.getSubtask(id);
        System.out.println(subtask);
    }

    private static void createEpic() {
        System.out.println("Введите название эпика: ");
        String title = scanner.next();
        System.out.println("Введите описание эпика: ");
        String description = scanner.next();
        taskManager.addEpic(title, description);
    }

    private static void createSubtask() {
        System.out.println("Введите ID эпика, к которому относится подзадача: ");
        int epicId = scanner.nextInt();
        Epic epic = taskManager.getEpic(epicId);
        if (epic == null) {
            System.out.println("Эпик с таким ID не найден.");
            return;
        }
        System.out.println("Введите название подзадачи: ");
        String title = scanner.next();
        System.out.println("Введите описание подзадачи: ");
        String description = scanner.next();
        taskManager.addSubtask(title, description, TaskStatus.NEW, epicId);
    }

    private static void listAllTasks() {
        System.out.println("Задачи:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
    }

    private static void listAllEpics() {
        System.out.println("Эпики: ");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
    }

    private static void listAllSubtasks() {
        System.out.println("Подзадачи:");
        for (Subtask sub : taskManager.getAllSubtasks()) {
            System.out.println(sub);
        }
    }

    private static void listSubtasksByEpic() {
        System.out.println("Введите ID эпика: ");
        int epicId = scanner.nextInt();
        ArrayList<Subtask> subtasks = taskManager.getSubtasksByEpic(epicId);
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
        Subtask sub = taskManager.getAllSubtasks().get(id);
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
            taskManager.updateSubtask(sub);
            System.out.println("Статус обновлён.");
        } catch (IllegalArgumentException e) {
            System.out.println("Некорректный статус.");
        }
    }

    private static void removeTaskById() {
        System.out.println("Введите ID: ");
        int id = scanner.nextInt();

        if (taskManager.getTask(id) != null) {
            taskManager.removeTask(id);
            System.out.println("Задача удалена.");
        } else if (taskManager.getEpic(id) != null) {
            taskManager.removeEpic(id);
            System.out.println("Эпик и его подзадачи удалены.");
        } else if (taskManager.getAllSubtasks().get(id) != null) {
            taskManager.removeSubtask(id);
            System.out.println("Подзадача удалена.");
        } else {
            System.out.println("Задача с таким ID не найдена.");
        }
    }

    private static void getHistory() {
        System.out.println("Задачи:");
        List<Task> tasks =  historyManager.getHistory();
        for (Task task : tasks) {
            System.out.println(task);
        }
    }
}
