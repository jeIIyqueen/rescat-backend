package jellyqueen.rescat.exception;

import lombok.Getter;

@Getter
public class NotMatchException extends RuntimeException {
    private String field;

    public NotMatchException(String s) {

    }

    public NotMatchException(String field, String message) {
        super(message);
        this.field = field;
    }
}
