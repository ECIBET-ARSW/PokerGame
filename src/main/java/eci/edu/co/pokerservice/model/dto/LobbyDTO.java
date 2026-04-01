package eci.edu.co.pokerservice.model.dto;

import eci.edu.co.pokerservice.model.document.Cart;
import eci.edu.co.pokerservice.model.document.Player;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;
@Data
@AllArgsConstructor
public class LobbyDTO {
    private String id;
    private int totalBet;
    private List<Player> players;
    private List<Cart> carts;
    private List<Cart> cartsInTable;
}
