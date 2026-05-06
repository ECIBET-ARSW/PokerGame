package eci.edu.co.pokerservice.model.dto;

import eci.edu.co.pokerservice.model.document.enums.Suit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {
    private Suit suit;
    private String value;
}
