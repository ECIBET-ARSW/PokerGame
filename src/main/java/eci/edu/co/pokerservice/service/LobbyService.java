package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.mapper.LobbyMapper;
import eci.edu.co.pokerservice.model.dto.LobbyDTO;
import eci.edu.co.pokerservice.model.request.LobbyRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class LobbyService {
    private final LobbyMapper lobbyMapper;
    public LobbyDTO createLobby(LobbyRequestDTO lobbyDTO){

    }
}
