package eci.edu.co.pokerservice.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LobbyRequestDTO {
    private String playerName;
}
