package eci.edu.co.pokerservice.model.document;

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
    private int totalBet;
    private int betRound;
    private int actualBet;
    private int actualRaise;
    @DBRef
    private List<Player> players;
    private List<Cart> carts;
    private List<Cart> cartsInTable;
    private boolean inGame;
    @DBRef
    private Player winner;
}
