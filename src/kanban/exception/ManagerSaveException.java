package kanban.exception;

import java.io.IOException;

public class ManagerSaveException extends IOException {
    public ManagerSaveException() {
        super();
    }

    public ManagerSaveException(final String message) {
        super(message);
    }

    public ManagerSaveException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ManagerSaveException(final Throwable cause) {
        super(cause);
    }
}
