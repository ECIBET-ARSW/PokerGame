package eci.edu.co.pokerservice.model.dto.request;

import eci.edu.co.pokerservice.model.document.enums.PlayerAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerActionRequestDTO {
    private String gameId;
    private String playerId;
    private PlayerAction action;
    private int raiseAmount;
}