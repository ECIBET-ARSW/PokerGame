package eci.edu.co.pokerservice.controller;

import eci.edu.co.pokerservice.model.dto.GamePublicDTO;
import eci.edu.co.pokerservice.model.dto.PlayerPrivateDTO;
import eci.edu.co.pokerservice.model.dto.request.PlayerActionRequestDTO;
import eci.edu.co.pokerservice.response.ApiResponse;
import eci.edu.co.pokerservice.service.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/player")
@AllArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PutMapping("/action")
    public ResponseEntity<ApiResponse<Object>> performAction(@RequestBody PlayerActionRequestDTO request) {
        GamePublicDTO gameDTO = playerService.performAction(request);
        return response(200, gameDTO, "Action performed");
    }

    @GetMapping("/{gameId}/{playerId}/hand")
    public ResponseEntity<ApiResponse<Object>> getHand(
            @PathVariable String gameId,
            @PathVariable String playerId) {
        PlayerPrivateDTO hand = playerService.getHand(gameId, playerId);
        return response(200, hand, "Hand retrieved");
    }

    private ResponseEntity<ApiResponse<Object>> response(int code, Object data, String message) {
        ApiResponse<Object> response = ApiResponse.builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(code).body(response);
    }
}