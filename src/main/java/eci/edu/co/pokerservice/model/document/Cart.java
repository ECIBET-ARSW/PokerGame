package eci.edu.co.pokerservice.model.document;

import eci.edu.co.pokerservice.model.document.enums.Suit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cart{
    private Suit suit;
    private String value;
}
