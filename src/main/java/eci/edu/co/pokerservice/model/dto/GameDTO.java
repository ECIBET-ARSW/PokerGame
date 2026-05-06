package eci.edu.co.pokerservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDTO {
    private String id;
    private int turn;
    private int totalBet;
    private int betRound;
    private int actualBet;
    private int actualRaise;
    private List<PlayerDTO> players;
    private List<CartDTO> carts;
    private List<CartDTO> cartsInTable;
    private boolean inGame;
    private PlayerDTO winner;
}