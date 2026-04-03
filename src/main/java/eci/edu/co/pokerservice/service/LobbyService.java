package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.exception.LobbyException;
import eci.edu.co.pokerservice.mapper.LobbyMapper;
import eci.edu.co.pokerservice.model.document.Player;
import eci.edu.co.pokerservice.model.dto.LobbyDTO;
import eci.edu.co.pokerservice.model.request.LobbyRequestDTO;
import eci.edu.co.pokerservice.repository.LobbyRepository;
import eci.edu.co.pokerservice.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
public class LobbyService {

    private final PlayerRepository playerRepository;
    private final LobbyRepository lobbyRepository;
    private final LobbyMapper lobbyMapper;
    public LobbyDTO createLobby(LobbyRequestDTO lobbyRequestDTO){
        Player player = playerValidations(lobbyRequestDTO);
        player.setName(lobbyRequestDTO.getPlayerName());
        Lobby
    }

    private Player playerValidations(LobbyRequestDTO lobbyRequestDTO){
        Optional<Player> optionalPlayer = playerRepository.findById(lobbyRequestDTO.getPlayerId());
        return optionalPlayer.orElseGet(() -> Player.builder()
                .id(lobbyRequestDTO.getPlayerId())
                .build());
    }
}
