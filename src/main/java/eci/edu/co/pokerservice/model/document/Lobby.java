package eci.edu.co.pokerservice.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "lobbies")
@Data
@AllArgsConstructor
@Builder
public class Lobby {
    @Id
    private String id;
    @DBRef
    private Game actualGame;
    private int smallBlind;
    private int bigBlind;
    private int maxBet;
    private String leaderId;
    @DBRef
    private List<Game> games;
    private LocalDateTime lobbyCreated;
}