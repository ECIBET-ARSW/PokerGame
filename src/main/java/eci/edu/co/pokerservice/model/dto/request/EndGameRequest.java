package eci.edu.co.pokerservice.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EndGameRequest {
    private String lobbyId;
    private String winnerId;
}
