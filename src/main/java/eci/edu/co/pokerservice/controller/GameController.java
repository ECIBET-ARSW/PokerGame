package eci.edu.co.pokerservice.controller;

import eci.edu.co.pokerservice.model.dto.GamePublicDTO;
import eci.edu.co.pokerservice.response.ApiResponse;
import eci.edu.co.pokerservice.service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/game")
@AllArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Object>> getGame(@PathVariable String gameId) {
        GamePublicDTO gameDTO = gameService.getGame(gameId);
        return response(200, gameDTO, "Game found");
    }

    @PutMapping("/{gameId}/deal")
    public ResponseEntity<ApiResponse<Object>> dealCards(@PathVariable String gameId) {
        GamePublicDTO gameDTO = gameService.dealCards(gameId);
        return response(200, gameDTO, "Cards dealt");
    }

    @PutMapping("/{gameId}/phase")
    public ResponseEntity<ApiResponse<Object>> nextPhase(@PathVariable String gameId) {
        GamePublicDTO gameDTO = gameService.nextPhase(gameId);
        return response(200, gameDTO, "Phase advanced");
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
