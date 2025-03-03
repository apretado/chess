package service.exception;

public class UsernameTakenException extends Exception {
    public UsernameTakenException(String message) {
        super(message);
    }
}
