package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.exception.GameBadRequestException;
import eci.edu.co.pokerservice.exception.GameNotFoundException;
import eci.edu.co.pokerservice.mapper.CartMapper;
import eci.edu.co.pokerservice.mapper.GameMapper;
import eci.edu.co.pokerservice.model.document.Cart;
import eci.edu.co.pokerservice.model.document.Game;
import eci.edu.co.pokerservice.model.document.Player;
import eci.edu.co.pokerservice.model.document.enums.GamePhase;
import eci.edu.co.pokerservice.model.document.enums.PlayerAction;
import eci.edu.co.pokerservice.model.dto.GamePublicDTO;
import eci.edu.co.pokerservice.model.dto.PlayerPrivateDTO;
import eci.edu.co.pokerservice.model.dto.request.PlayerActionRequestDTO;
import eci.edu.co.pokerservice.repository.GameRepository;
import eci.edu.co.pokerservice.repository.PlayerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Service
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameMapper gameMapper;
    private final CartMapper cartMapper;

    @Transactional
    public GamePublicDTO dealCards(String gameId) {
        Game game = validateGame(gameId);
        validateGameIsRunning(game);
        validatePhase(game, GamePhase.PREFLOP, "Cards have already been dealt");

        List<Cart> deck = new ArrayList<>(game.getCarts());
        Collections.shuffle(deck);

        List<Player> players = game.getPlayers().stream()
                .filter(Player::isInLobby)
                .toList();

        int cardIndex = 0;
        for (Player player : players) {
            player.setHand(List.of(deck.get(cardIndex), deck.get(cardIndex + 1)));
            player.setFolded(false);
            player.setAllIn(false);
            player.setCurrentBet(0);
            cardIndex += 2;
            playerRepository.save(player);
        }

        List<Cart> remainingDeck = deck.subList(cardIndex, deck.size());
        game.setCarts(new ArrayList<>(remainingDeck));
        game.setCartsInTable(new ArrayList<>());
        game.setPhase(GamePhase.PREFLOP);
        game.setPot(0);
        game.setActualBet(0);
        game.setActualRaise(0);
        game.setPlayersActedThisRound(0);

        int dealerIndex = game.getDealerIndex();
        int smallBlindIndex = nextActivePlayer(players, dealerIndex);
        int bigBlindIndex = nextActivePlayer(players, smallBlindIndex);
        int firstToAct = nextActivePlayer(players, bigBlindIndex);

        game.setDealerIndex(dealerIndex);
        game.setSmallBlindIndex(smallBlindIndex);
        game.setBigBlindIndex(bigBlindIndex);
        game.setCurrentPlayerIndex(firstToAct);


        postBlind(players.get(smallBlindIndex), 1000, game);
        postBlind(players.get(bigBlindIndex), 2000, game);
        game.setActualBet(2000);

        gameRepository.save(game);
        return gameMapper.toPublicDTO(game);
    }

    @Transactional
    public GamePublicDTO nextPhase(String gameId) {
        Game game = validateGame(gameId);
        validateGameIsRunning(game);

        GamePhase currentPhase = game.getPhase();

        if (currentPhase == GamePhase.SHOWDOWN) {
            throw new GameBadRequestException("Game is already in showdown");
        }

        switch (currentPhase) {
            case PREFLOP -> revealCommunityCards(game, 3, GamePhase.FLOP);
            case FLOP    -> revealCommunityCards(game, 1, GamePhase.TURN);
            case TURN    -> revealCommunityCards(game, 1, GamePhase.RIVER);
            case RIVER   -> game.setPhase(GamePhase.SHOWDOWN);
            default      -> throw new GameBadRequestException("Invalid phase transition");
        }
        resetRoundBets(game);
        List<Player> players = game.getPlayers();
        if (currentPhase != GamePhase.RIVER) {
            int firstToAct = nextActivePlayer(players, game.getDealerIndex());
            game.setCurrentPlayerIndex(firstToAct);
        }

        gameRepository.save(game);
        return gameMapper.toPublicDTO(game);
    }

    @Transactional
    public GamePublicDTO playerAction(PlayerActionRequestDTO request) {
        Game game = validateGame(request.getGameId());
        validateGameIsRunning(game);

        List<Player> players = game.getPlayers();
        Player player = findPlayer(players, request.getPlayerId());

        validatePlayerTurn(game, players, player);
        validatePlayerNotFolded(player);

        switch (request.getAction()) {
            case CHECK -> handleCheck(game, player);
            case CALL  -> handleCall(game, player);
            case RAISE -> handleRaise(game, player, request.getRaiseAmount());
            case FOLD  -> handleFold(player);
        }

        playerRepository.save(player);
        int nextIndex = nextActivePlayer(players, game.getCurrentPlayerIndex());
        game.setCurrentPlayerIndex(nextIndex);
        game.setPlayersActedThisRound(game.getPlayersActedThisRound() + 1);

        gameRepository.save(game);
        return gameMapper.toPublicDTO(game);
    }

    public List<PlayerPrivateDTO> getPlayerHands(String gameId) {
        Game game = validateGame(gameId);
        return game.getPlayers().stream()
                .filter(p -> p.getHand() != null && !p.getHand().isEmpty())
                .map(p -> new PlayerPrivateDTO(
                        p.getId(),
                        p.getHand().stream().map(cartMapper::toDTO).toList()
                ))
                .toList();
    }

    public PlayerPrivateDTO getPlayerHand(String gameId, String playerId) {
        Game game = validateGame(gameId);
        Player player = findPlayer(game.getPlayers(), playerId);
        if (player.getHand() == null || player.getHand().isEmpty()) {
            throw new GameBadRequestException("Player has no cards yet");
        }
        return new PlayerPrivateDTO(
                player.getId(),
                player.getHand().stream().map(cartMapper::toDTO).toList()
        );
    }

    public GamePublicDTO getGame(String gameId) {
        return gameMapper.toPublicDTO(validateGame(gameId));
    }


    private void revealCommunityCards(Game game, int count, GamePhase nextPhase) {
        List<Cart> deck = new ArrayList<>(game.getCarts());
        List<Cart> table = new ArrayList<>(game.getCartsInTable());

        deck.remove(0);

        for (int i = 0; i < count; i++) {
            table.add(deck.remove(0));
        }

        game.setCarts(deck);
        game.setCartsInTable(table);
        game.setPhase(nextPhase);
    }

    private void resetRoundBets(Game game) {
        game.setActualBet(0);
        game.setActualRaise(0);
        game.setPlayersActedThisRound(0);
        game.getPlayers().forEach(p -> {
            p.setCurrentBet(0);
            playerRepository.save(p);
        });
    }

    private void postBlind(Player player, int amount, Game game) {
        int toPay = Math.min(amount, player.getCredit());
        player.setCredit(player.getCredit() - toPay);
        player.setCurrentBet(toPay);
        if (player.getCredit() == 0) player.setAllIn(true);
        game.setPot(game.getPot() + toPay);
        playerRepository.save(player);
    }

    private void handleCheck(Game game, Player player) {
        if (game.getActualBet() > player.getCurrentBet()) {
            throw new GameBadRequestException("Cannot check, there is an active bet. Call or raise");
        }
        log.info("Player {} checks", player.getId());
    }

    private void handleCall(Game game, Player player) {
        int amountToCall = game.getActualBet() - player.getCurrentBet();
        if (amountToCall <= 0) throw new GameBadRequestException("Nothing to call, you can check");
        int toPay = Math.min(amountToCall, player.getCredit());
        player.setCredit(player.getCredit() - toPay);
        player.setCurrentBet(player.getCurrentBet() + toPay);
        if (player.getCredit() == 0) player.setAllIn(true);
        game.setPot(game.getPot() + toPay);
    }

    private void handleRaise(Game game, Player player, int raiseAmount) {
        if (raiseAmount < game.getActualRaise()) {
            throw new GameBadRequestException("Raise must be at least " + game.getActualRaise());
        }
        int totalBet = game.getActualBet() + raiseAmount;
        int amountNeeded = totalBet - player.getCurrentBet();
        if (amountNeeded > player.getCredit()) {
            throw new GameBadRequestException("Not enough credits to raise");
        }
        player.setCredit(player.getCredit() - amountNeeded);
        player.setCurrentBet(totalBet);
        if (player.getCredit() == 0) player.setAllIn(true);
        game.setPot(game.getPot() + amountNeeded);
        game.setActualBet(totalBet);
        game.setActualRaise(raiseAmount);
        game.setPlayersActedThisRound(0);
    }

    private void handleFold(Player player) {
        player.setFolded(true);
        log.info("Player {} folds", player.getId());
    }

    private int nextActivePlayer(List<Player> players, int fromIndex) {
        int size = players.size();
        int next = (fromIndex + 1) % size;
        int attempts = 0;
        while ((players.get(next).isFolded() || !players.get(next).isInLobby()) && attempts < size) {
            next = (next + 1) % size;
            attempts++;
        }
        return next;
    }

    private void validatePlayerTurn(Game game, List<Player> players, Player player) {
        Player currentPlayer = players.get(game.getCurrentPlayerIndex());
        if (!currentPlayer.getId().equals(player.getId())) {
            throw new GameBadRequestException("It's not your turn");
        }
    }

    private void validatePlayerNotFolded(Player player) {
        if (player.isFolded()) throw new GameBadRequestException("Player has already folded");
    }

    private void validatePhase(Game game, GamePhase expected, String message) {
        if (game.getPhase() != null && game.getPhase() != expected) {
            throw new GameBadRequestException(message);
        }
    }

    private void validateGameIsRunning(Game game) {
        if (!game.isInGame()) throw new GameBadRequestException("Game is not running");
    }

    private Player findPlayer(List<Player> players, String playerId) {
        return players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new GameNotFoundException("Player not found in this game"));
    }

    private Game validateGame(String gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found"));
    }
}