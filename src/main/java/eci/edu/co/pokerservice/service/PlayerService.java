package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.model.dto.GamePublicDTO;
import eci.edu.co.pokerservice.model.dto.PlayerPrivateDTO;
import eci.edu.co.pokerservice.model.dto.request.PlayerActionRequestDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class PlayerService {

    private final GameService gameService;

    public GamePublicDTO performAction(PlayerActionRequestDTO request) {
        return gameService.playerAction(request);
    }

    public PlayerPrivateDTO getHand(String gameId, String playerId) {
        return gameService.getPlayerHand(gameId, playerId);
    }
}