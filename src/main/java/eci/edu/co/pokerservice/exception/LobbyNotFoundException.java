package eci.edu.co.pokerservice.exception;

public class LobbyNotFoundException extends RuntimeException{
    public LobbyNotFoundException(String message){
        super(message);
    }
}
