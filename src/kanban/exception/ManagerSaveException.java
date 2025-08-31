package kanban.exception;

import java.io.IOException;

public class ManagerSaveException extends IOException {
    public ManagerSaveException(final Throwable cause) {
        super(cause);
    }
}
