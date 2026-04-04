package eci.edu.co.pokerservice.controller;

import eci.edu.co.pokerservice.model.dto.LobbyDTO;
import eci.edu.co.pokerservice.model.dto.request.AddPlayerRequestDTO;
import eci.edu.co.pokerservice.model.dto.request.EndGameRequestDTO;
import eci.edu.co.pokerservice.model.dto.request.LeaveLobbyRequestDTO;
import eci.edu.co.pokerservice.model.dto.request.LobbyRequestDTO;
import eci.edu.co.pokerservice.response.ApiResponse;
import eci.edu.co.pokerservice.service.LobbyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lobby")
@AllArgsConstructor
public class LobbyController {
    private final LobbyService lobbyService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createLobby(@RequestBody LobbyRequestDTO lobbyRequestDTO){
        LobbyDTO lobbyResponse = lobbyService.createLobby(lobbyRequestDTO);
        return response(201, lobbyResponse, "Lobby created");
    }
    @PutMapping("/{lobbyId}")
    public ResponseEntity<ApiResponse<Object>> startGame(@PathVariable(value = "lobbyId") String lobbyId){
        LobbyDTO lobbyDTO = lobbyService.startGame(lobbyId);
        return response(200, lobbyDTO, "Game is started");
    }
    @PutMapping()
    public ResponseEntity<ApiResponse<Object>> endGame(@RequestBody EndGameRequestDTO endGameRequestDTO){
        LobbyDTO lobbyDTO = lobbyService.endGame(endGameRequestDTO);
        return response(200, lobbyDTO, "Game is finished");
    }
    @PutMapping("/player")
    public ResponseEntity<ApiResponse<Object>> addPlayer(@RequestBody AddPlayerRequestDTO addPlayerRequestDTO){
        LobbyDTO lobbyDTO = lobbyService.addPlayer(addPlayerRequestDTO);
        return response(200, lobbyDTO, "Player is added");
    }
    @PutMapping("/player/end")
    public ResponseEntity<ApiResponse<Object>> removePlayer(@RequestBody LeaveLobbyRequestDTO leaveLobbyRequestDTO){
        lobbyService.removePlayer(leaveLobbyRequestDTO);
        return response(200, "player removed", "Player is removed");
    }
    @GetMapping()
    public ResponseEntity<ApiResponse<Object>> getLobbies(){
        List<LobbyDTO> lobbyDTOS = lobbyService.getLobbies();
        return response(200,lobbyDTOS,"Lobbies found");
    }

    private ResponseEntity<ApiResponse<Object>> response(int code, Object data, String message){
        ApiResponse<Object> response = ApiResponse.builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(code).body(response);
    }
}
