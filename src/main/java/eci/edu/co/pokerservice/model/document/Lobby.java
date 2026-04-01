package eci.edu.co.pokerservice.model.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "carts")
@Data
@AllArgsConstructor
public class Lobby{
    @Id
    private String id;
    private int totalBet;
    @DBRef
    private List<Player> players;
    private List<Cart> carts;
    private List<Cart> cartsInTable;
}
