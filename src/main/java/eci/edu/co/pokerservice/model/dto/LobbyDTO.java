package eci.edu.co.pokerservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class LobbyDTO {
    private String id;
    private GameDTO actualGame;
    private int smallBlind;
    private int bigBlind;
    private List<GameDTO> games;
    private LocalDateTime lobbyCreated;
}
