package eci.edu.co.pokerservice.controller;

import eci.edu.co.pokerservice.model.dto.LobbyDTO;
import eci.edu.co.pokerservice.model.request.LobbyRequestDTO;
import eci.edu.co.pokerservice.model.response.ApiResponse;
import eci.edu.co.pokerservice.service.LobbyService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lobby")
@AllArgsConstructor
public class LobbyController {
    private final LobbyService lobbyService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createLobby(@RequestBody LobbyRequestDTO lobbyDTO){
        LobbyDTO lobbyResponse = lobbyService.createLobby(lobbyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder().data(lobbyDTO).build());
    }
}
