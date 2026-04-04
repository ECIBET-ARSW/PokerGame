package eci.edu.co.pokerservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerPublicDTO {
    private String id;
    private String name;
    private int credit;
    private int currentBet;
    private boolean folded;
    private boolean allIn;
    private boolean inLobby;
}
