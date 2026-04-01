package eci.edu.co.pokerservice.model.document;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "players")
@Data
@AllArgsConstructor
public class Player {
    @Id
    private String id;
    private String name;
    private int credit;
    private int bet;
    private List<Cart> carts;
}
