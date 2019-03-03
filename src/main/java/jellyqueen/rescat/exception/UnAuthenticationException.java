package jellyqueen.rescat.exception;

import lombok.Getter;

@Getter
public class UnAuthenticationException extends RuntimeException {
    private String field;

    public UnAuthenticationException() {
    }

    public UnAuthenticationException(String field, String message) {
        super(message);
        this.field = field;
    }
}
