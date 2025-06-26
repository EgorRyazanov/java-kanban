package kanban.manager;

import kanban.model.HistoryManager;
import kanban.model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList<Task> history = new CustomLinkedList<>();

    public void add(Task task) {
        history.linkLast(task);
    }

    public void remove(int id) {
        history.removeNode(id);
    }

    public List<Task> getHistory() {
        return history.getTask();
    }


    static class Node<T> {
        public T data;
        public Node<T> previous;
        public Node<T> next;

        public Node(Node<T> previous, T data, Node<T> next) {
            this.next = next;
            this.data = data;
            this.previous = previous;
        }

        @Override
        public String toString() {
            return "currentData: " + data;
        }
    }

    static class CustomLinkedList<T extends Task> {
        private Node<T> firstNode;
        private Node<T> tailNode;

        private final Map<Number, Node<T>> elementsMap = new HashMap<>();

        public void linkLast(T element) {
            if (elementsMap.containsKey(element.getId())) {
                removeNode(element.getId());
            }

            Node<T> newNode = new Node<>(null, element, null);
            if (firstNode == null) {
                firstNode = newNode;
            } else {
                tailNode.next = newNode;
                newNode.previous = tailNode;
            }

            tailNode = newNode;
            elementsMap.put(element.getId(), newNode);
        }

        public List<T> getTask() {
            Node<T> currentNode = firstNode;
            List<T> list = new ArrayList<>();

            if (currentNode == null) {
                return list;
            }

            while (true) {
                if (currentNode == null) {
                    return list;
                }

                list.add(currentNode.data);
                currentNode = currentNode.next;
            }
        }

        public void removeNode(Number id) {
            Node<T> node = elementsMap.get(id);
            if (node == null) {
                return;
            }

            Node<T> prevNode = node.previous;
            Node<T> nextNode = node.next;

            elementsMap.remove(id);
            if (prevNode == null && nextNode == null) {
                firstNode = tailNode = null;
                return;
            }

            if (prevNode == null) {
                firstNode = nextNode;
                nextNode.previous = null;
                return;
            }

            if (nextNode == null) {
                prevNode.next = null;
                tailNode = prevNode;
                return;
            }

            prevNode.next = nextNode;
            nextNode.previous = prevNode;
        }
    }
}
