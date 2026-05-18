package eci.edu.co.pokerservice.model.document;

import eci.edu.co.pokerservice.model.document.enums.GamePhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "games")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Game {
    @Id
    private String id;
    private GamePhase phase;
    private boolean inGame;
    private int pot;
    private int actualBet;
    private int actualRaise;
    private int maxBet;
    private int currentPlayerIndex;
    private int dealerIndex;
    private int smallBlindIndex;
    private int bigBlindIndex;
    private int playersActedThisRound;
    private List<Cart> carts;
    private List<Cart> cartsInTable;
    @DBRef
    private List<Player> players;
    @DBRef
    private Player winner;
}