package eci.edu.co.pokerservice.model.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "carts")
@Data
@AllArgsConstructor
public class Lobby{
    @Id
    private String id;
    // Bet control
    private int totalBet;
    private int betRound;
    private int actualBet;
    @DBRef
    private List<Player> players;
    private List<Cart> carts;
    private List<Cart> cartsInTable;
    // History control
    private boolean inGame;
    private LocalDateTime lobbyCreated;
}
