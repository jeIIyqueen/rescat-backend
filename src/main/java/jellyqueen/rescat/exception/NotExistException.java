package jellyqueen.rescat.exception;

import lombok.Getter;

@Getter
public class NotExistException extends RuntimeException {
    private String field;

    public NotExistException() {
    }

    public NotExistException(String field, String message) {
        super(message);
        this.field = field;
    }
}
