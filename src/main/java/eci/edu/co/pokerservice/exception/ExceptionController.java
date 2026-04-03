package eci.edu.co.pokerservice.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler({
            LobbyException.class,
    })
    public ResponseEntity<?> handleResponsibleBadRequest(RuntimeException ex) {
        return response(ex, HttpStatus.BAD_REQUEST);
    }



    private ResponseEntity<?> response(RuntimeException ex, HttpStatus status){
        return ResponseEntity.status(status)
                .body(Map.of(
                        "error", ex.getMessage()
                ));
    }

}
