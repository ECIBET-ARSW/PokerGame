package eci.edu.co.pokerservice.exception;

public class LobbyBadRequestException extends RuntimeException{

    public LobbyBadRequestException(String mensaje) {
        super(mensaje);
    }
}
