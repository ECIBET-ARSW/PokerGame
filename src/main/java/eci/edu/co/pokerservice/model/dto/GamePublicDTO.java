package eci.edu.co.pokerservice.model.dto;


import eci.edu.co.pokerservice.model.document.enums.GamePhase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GamePublicDTO {
    private String id;
    private GamePhase phase;
    private boolean inGame;
    private int pot;
    private int actualBet;
    private int actualRaise;
    private int currentPlayerIndex;
    private int dealerIndex;
    private int smallBlindIndex;
    private int bigBlindIndex;
    private List<CartDTO> cartsInTable;
    private List<PlayerPublicDTO> players;
    private PlayerDTO winner;
}
