package eci.edu.co.pokerservice.model.document;

import eci.edu.co.pokerservice.model.document.enums.Suit;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "carts")
@Data
@AllArgsConstructor
public class Cart{
    @Id
    private Long id;
    private Suit suit;
    private String value;
}
