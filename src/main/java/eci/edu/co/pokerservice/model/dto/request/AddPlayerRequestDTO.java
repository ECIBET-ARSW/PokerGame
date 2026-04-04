package eci.edu.co.pokerservice.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AddPlayerRequestDTO {
    private String lobbyId;
    private String playerId;
    private String playerName;
    private int credits;
}
