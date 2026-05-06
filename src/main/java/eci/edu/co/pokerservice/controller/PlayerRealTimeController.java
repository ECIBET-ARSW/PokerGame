package eci.edu.co.pokerservice.controller;

import eci.edu.co.pokerservice.model.dto.GamePublicDTO;
import eci.edu.co.pokerservice.model.dto.PlayerPrivateDTO;
import eci.edu.co.pokerservice.model.dto.request.PlayerActionRequestDTO;
import eci.edu.co.pokerservice.service.GameService;
import eci.edu.co.pokerservice.service.PlayerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
@Slf4j
public class PlayerRealTimeController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;
    private final PlayerService playerService;


    @MessageMapping("/game/{gameId}/deal")
    public void dealCards(@DestinationVariable String gameId) {
        GamePublicDTO gameDTO = gameService.dealCards(gameId);
        broadcastGameState(gameId, gameDTO);
        sendPrivateHands(gameId);
    }

    @MessageMapping("/game/{gameId}/phase")
    public void nextPhase(@DestinationVariable String gameId) {
        GamePublicDTO gameDTO = gameService.nextPhase(gameId);
        broadcastGameState(gameId, gameDTO);
    }


    @MessageMapping("/game/{gameId}/action")
    public void playerAction(@DestinationVariable String gameId,
                             @Payload PlayerActionRequestDTO request) {
        request.setGameId(gameId);
        GamePublicDTO gameDTO = playerService.performAction(request);
        broadcastGameState(gameId, gameDTO);
    }

    private void broadcastGameState(String gameId, GamePublicDTO gameDTO) {
        messagingTemplate.convertAndSend("/topic/game/" + gameId, gameDTO);
        log.info("Broadcasted game state for game {} - phase {}", gameId, gameDTO.getPhase());
    }

    private void sendPrivateHands(String gameId) {
        List<PlayerPrivateDTO> hands = gameService.getPlayerHands(gameId);
        hands.forEach(hand -> {
            messagingTemplate.convertAndSendToUser(
                    hand.getPlayerId(),
                    "/queue/hand",
                    hand
            );
            log.info("Sent private hand to player {}", hand.getPlayerId());
        });
    }
}