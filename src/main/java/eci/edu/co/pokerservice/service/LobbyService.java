package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.exception.LobbyBadRequestException;
import eci.edu.co.pokerservice.exception.LobbyNotFoundException;
import eci.edu.co.pokerservice.mapper.LobbyMapper;
import eci.edu.co.pokerservice.model.document.Game;
import eci.edu.co.pokerservice.model.document.Lobby;
import eci.edu.co.pokerservice.model.document.Player;
import eci.edu.co.pokerservice.model.dto.LobbyDTO;
import eci.edu.co.pokerservice.model.dto.request.AddPlayerRequestDTO;
import eci.edu.co.pokerservice.model.dto.request.EndGameRequestDTO;
import eci.edu.co.pokerservice.model.dto.request.LeaveLobbyRequestDTO;
import eci.edu.co.pokerservice.model.dto.request.LobbyRequestDTO;
import eci.edu.co.pokerservice.repository.CartRepository;
import eci.edu.co.pokerservice.repository.GameRepository;
import eci.edu.co.pokerservice.repository.LobbyRepository;
import eci.edu.co.pokerservice.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class LobbyService {

    private final PlayerRepository playerRepository;
    private final LobbyRepository lobbyRepository;
    private final CartRepository cartRepository;
    private final GameRepository gameRepository;
    private final LobbyMapper lobbyMapper;

    @Transactional
    public LobbyDTO createLobby(LobbyRequestDTO lobbyRequestDTO) {
        cleanPlayerFromLobbies(lobbyRequestDTO.getPlayerId());

        Player player = playerValidations(lobbyRequestDTO.getPlayerId());
        player.setName(lobbyRequestDTO.getPlayerName());
        player.setCredit(lobbyRequestDTO.getCredits());
        player.setAvatarIndex(1);
        Game game = newGame(List.of(player));
        Lobby lobby = newLobby(game);
        lobby.setLeaderId(player.getId());
        lobby.setGames(new ArrayList<>());
        validateBigBlind(player);
        gameRepository.save(game);
        playerRepository.save(player);
        lobbyRepository.save(lobby);
        return lobbyMapper.toDTO(lobby);
    }

    private Lobby newLobby(Game game) {
        return Lobby.builder()
                .actualGame(game)
                .smallBlind(1000)
                .bigBlind(2000)
                .lobbyCreated(LocalDateTime.now())
                .build();
    }

    private void cleanPlayerFromLobbies(String playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (optionalPlayer.isEmpty()) return;

        Player player = optionalPlayer.get();
        if (!player.isInLobby()) return;

        List<Lobby> allLobbies = lobbyRepository.findAll();
        for (Lobby lobby : allLobbies) {
            Game game = lobby.getActualGame();
            if (game == null) continue;

            List<Player> players = game.getPlayers();
            if (players == null) continue;

            boolean wasInThisLobby = players.removeIf(p -> p.getId().equals(playerId));
            if (wasInThisLobby) {
                if (players.isEmpty()) {
                    gameRepository.delete(game);
                    lobbyRepository.delete(lobby);
                } else {
                    if (playerId.equals(lobby.getLeaderId())) {
                        lobby.setLeaderId(players.get(0).getId());
                    }
                    gameRepository.save(game);
                    lobbyRepository.save(lobby);
                }
                break;
            }
        }

        player.setInLobby(false);
        playerRepository.save(player);
    }

    private Player playerValidations(String playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        Player player = optionalPlayer.orElseGet(() -> Player.builder()
                .id(playerId)
                .build());
        if (player.isInLobby()) throw new LobbyBadRequestException("Player in another lobby");
        player.setInLobby(true);
        return player;
    }

    @Transactional
    public LobbyDTO startGame(String lobbyId) {
        Lobby lobby = validateLobby(lobbyId);
        Game game = validateGameExist(lobby.getActualGame().getId());
        validateGame(game);
        game.setInGame(true);
        gameRepository.save(game);
        Lobby savedLobby = validateLobby(lobbyId);
        return lobbyMapper.toDTO(savedLobby);
    }

    private void validateGame(Game game) {
        log.info(game.getPlayers().toString());
        if (game.getPlayers().size() < 2) throw new LobbyBadRequestException("Players number is not enough");
    }

    @Transactional
    public LobbyDTO endGame(EndGameRequestDTO endGameRequestDTO) {
        Lobby lobby = validateLobby(endGameRequestDTO.getLobbyId());
        Game currentGame = lobby.getActualGame();
        if (!currentGame.isInGame() && currentGame.getWinner() == null) {
            return lobbyMapper.toDTO(lobby);
        }

        Player winner = playerRepository.findById(endGameRequestDTO.getWinnerId())
                .orElseThrow(() -> new LobbyBadRequestException("Winner not exist"));
        Game game = lobby.getActualGame();
        game.setInGame(false);
        game.setWinner(winner);
        lobby.getGames().add(game);
        List<Player> allPlayers = game.getPlayers();
        allPlayers.forEach(p -> {
            p.setInLobby(true);
            playerRepository.save(p);
        });
        Game newGame = newGame(new ArrayList<>(allPlayers));
        lobby.setActualGame(newGame);
        lobby.setLeaderId(winner.getId());

        gameRepository.save(game);
        gameRepository.save(newGame);
        lobbyRepository.save(lobby);
        Lobby savedLobby = validateLobby(endGameRequestDTO.getLobbyId());
        return lobbyMapper.toDTO(savedLobby);
    }

    private Lobby validateLobby(String lobbyId) {
        return lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));
    }

    private Game newGame(List<Player> players) {
        return Game.builder()
                .players(new ArrayList<>(players))
                .carts(cartRepository.findAll())
                .inGame(false)
                .build();
    }

    @Transactional
    public LobbyDTO addPlayer(AddPlayerRequestDTO addPlayerRequestDTO) {
        cleanPlayerFromLobbies(addPlayerRequestDTO.getPlayerId());
        Player player = playerValidations(addPlayerRequestDTO.getPlayerId());
        player.setName(addPlayerRequestDTO.getPlayerName());
        player.setCredit(addPlayerRequestDTO.getCredits());
        Lobby lobby = validateLobby(addPlayerRequestDTO.getLobbyId());
        Game game = validateGameExist(lobby.getActualGame().getId());
        validatePlayersNumber(game.getPlayers());
        java.util.Set<Integer> usedIndexes = game.getPlayers().stream()
                .map(Player::getAvatarIndex)
                .collect(java.util.stream.Collectors.toSet());
        int freeIndex = java.util.stream.IntStream.rangeClosed(1, 6)
                .filter(i -> !usedIndexes.contains(i))
                .findFirst()
                .orElse(game.getPlayers().size() + 1);
        player.setAvatarIndex(freeIndex);
        game.getPlayers().add(player);
        validateBigBlind(player);
        playerRepository.save(player);
        gameRepository.save(game);
        lobbyRepository.save(lobby);
        Lobby savedLobby = validateLobby(addPlayerRequestDTO.getLobbyId());
        return lobbyMapper.toDTO(savedLobby);
    }

    private void validateBigBlind(Player player) {
        if (player.getCredit() < 2000) throw new LobbyBadRequestException("Credits are not enough");
    }

    private void validatePlayersNumber(List<Player> players) {
        if (players.size() >= 6) throw new LobbyBadRequestException("Lobby is full");
    }

    private Game validateGameExist(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new LobbyBadRequestException("Game not found"));
    }

    public List<LobbyDTO> getLobbies() {
        List<Lobby> lobbies = lobbyRepository.findAll();
        return lobbies.stream().map(lobbyMapper::toDTO).toList();
    }

    @Transactional
    public void removePlayer(LeaveLobbyRequestDTO leaveLobbyRequestDTO) {
        String playerId = leaveLobbyRequestDTO.getPlayerId();
        String lobbyId  = leaveLobbyRequestDTO.getLobbyId();

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new LobbyNotFoundException("Player not found"));

        if (lobbyId != null && !lobbyId.isEmpty()) {
            try {
                Lobby lobby = validateLobby(lobbyId);
                Game game = lobby.getActualGame();

                if (game != null && game.getPlayers() != null) {
                    game.getPlayers().removeIf(p -> p.getId().equals(playerId));

                    if (game.getPlayers().isEmpty()) {
                        gameRepository.delete(game);
                        lobbyRepository.delete(lobby);
                    } else {
                        if (playerId.equals(lobby.getLeaderId())) {
                            lobby.setLeaderId(game.getPlayers().get(0).getId());
                        }
                        gameRepository.save(game);
                        lobbyRepository.save(lobby);
                    }
                }
            } catch (LobbyNotFoundException e) {
                log.warn("Lobby not found when removing player: {}", lobbyId);
            }
        } else {
            cleanPlayerFromLobbies(playerId);
        }

        player.setInLobby(false);
        playerRepository.save(player);
    }
}