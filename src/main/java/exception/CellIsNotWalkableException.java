package exception;

public class CellIsNotWalkableException extends RuntimeException {
    public CellIsNotWalkableException(String message) {
        super(message);
    }
}
