package eci.edu.co.pokerservice.exception;

public class GameBadRequestException extends RuntimeException {
    public GameBadRequestException(String message) {
        super(message);
    }
}
