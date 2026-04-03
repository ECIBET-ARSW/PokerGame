package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.exception.LobbyException;
import eci.edu.co.pokerservice.mapper.LobbyMapper;
import eci.edu.co.pokerservice.model.document.Cart;
import eci.edu.co.pokerservice.model.document.Game;
import eci.edu.co.pokerservice.model.document.Lobby;
import eci.edu.co.pokerservice.model.document.Player;
import eci.edu.co.pokerservice.model.dto.LobbyDTO;
import eci.edu.co.pokerservice.model.dto.request.LobbyRequestDTO;
import eci.edu.co.pokerservice.repository.CartRepository;
import eci.edu.co.pokerservice.repository.LobbyRepository;
import eci.edu.co.pokerservice.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class LobbyService {

    private final PlayerRepository playerRepository;
    private final LobbyRepository lobbyRepository;
    private final CartRepository cartRepository;
    private final LobbyMapper lobbyMapper;

    @Transactional
    public LobbyDTO createLobby(LobbyRequestDTO lobbyRequestDTO) {
        Player player = playerValidations(lobbyRequestDTO);
        player.setName(lobbyRequestDTO.getPlayerName());
        List<Cart> carts = cartRepository.findAll();
        Game game = Game.builder()
                .players(List.of(player))
                .carts(carts)
                .inGame(false)
                .build();
        Lobby lobby = Lobby.builder()
                .actualGame(game)
                .smallBlind(1000)
                .bigBlind(2000)
                .lobbyCreated(LocalDateTime.now())
                .build();
        Lobby savedLobby = lobbyRepository.save(lobby);
        LobbyDTO dto = lobbyMapper.toDTO(savedLobby);
        playerRepository.save(player);
        return dto;
    }

    private Player playerValidations(LobbyRequestDTO lobbyRequestDTO){
        Optional<Player> optionalPlayer = playerRepository.findById(lobbyRequestDTO.getPlayerId());
        Player player = optionalPlayer.orElseGet(() -> Player.builder()
                .id(lobbyRequestDTO.getPlayerId())
                .build());
        if(player.isInLobby()) throw new LobbyException("Player in another lobby");
        player.setInLobby(true);
        return player;
    }
}
