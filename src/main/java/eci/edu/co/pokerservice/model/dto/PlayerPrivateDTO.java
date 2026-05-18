package eci.edu.co.pokerservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerPrivateDTO {
    private String playerId;
    private List<CartDTO> hand;
}