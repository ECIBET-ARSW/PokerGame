package eci.edu.co.pokerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler({
            LobbyBadRequestException.class,
            GameBadRequestException.class,
    })
    public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
        return response(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            LobbyNotFoundException.class,
            GameNotFoundException.class,
    })
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {
        return response(ex, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<?> response(RuntimeException ex, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Map.of("error", ex.getMessage()));
    }
}