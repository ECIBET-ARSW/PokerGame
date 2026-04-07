package eci.edu.co.pokerservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerDTO {
    private String id;
    private String name;
    private int credit;
    private boolean inLobby;
    private List<CartDTO> hand;
}