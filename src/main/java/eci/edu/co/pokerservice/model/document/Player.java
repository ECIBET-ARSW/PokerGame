package eci.edu.co.pokerservice.model.document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(collection = "players")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    @Id
    private String id;
    private String name;
    private int credit;
    private boolean inLobby = false;

    // Estado dentro de la partida
    private List<Cart> hand;
    private int currentBet;
    private boolean folded;
    private boolean allIn;
}
