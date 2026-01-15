package exception;

public class CannotMoveToTheCellException extends RuntimeException {
    public CannotMoveToTheCellException(String message) {
        super(message);
    }
}
