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
import eci.edu.co.pokerservice.model.dto.request.LeaveLobbyRequestDTO;
import eci.edu.co.pokerservice.model.dto.request.PlayerActionRequestDTO;
import eci.edu.co.pokerservice.repository.GameRepository;
import eci.edu.co.pokerservice.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final GameMapper gameMapper;
    private final CartMapper cartMapper;
    private final WalletEventPublisher walletEventPublisher;
    @Lazy private final LobbyService lobbyService;
    private final Map<String, LocalDateTime> lastHeartbeat = new ConcurrentHashMap<>();
    private final Map<String, String> currentTurnPlayer   = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> turnStartTime = new ConcurrentHashMap<>();
    private static final int TIMEOUT_SECONDS    = 120;
    private static final int DISCONNECT_SECONDS = 30;

    public void registerHeartbeat(String playerId) {
        lastHeartbeat.put(playerId, LocalDateTime.now());
    }

    private boolean isConnected(String playerId) {
        LocalDateTime last = lastHeartbeat.get(playerId);
        if (last == null) return true;
        return java.time.Duration.between(last, LocalDateTime.now()).getSeconds() < DISCONNECT_SECONDS;
    }

    @Scheduled(fixedDelay = 10000)
    public void checkInactivity() {
        List<Game> activeGames = gameRepository.findAll().stream()
                .filter(Game::isInGame).toList();
        for (Game game : activeGames) {
            List<Player> players = game.getPlayers();
            if (players == null || players.isEmpty()) continue;
            int idx = game.getCurrentPlayerIndex();
            if (idx < 0 || idx >= players.size()) continue;
            Player current = players.get(idx);
            if (current.isFolded()) continue;
            String key = game.getId();
            String currentId = current.getId();
            if (!currentId.equals(currentTurnPlayer.get(key))) {
                currentTurnPlayer.put(key, currentId);
                turnStartTime.put(key, LocalDateTime.now());
                continue;
            }
            LocalDateTime start = turnStartTime.get(key);
            if (start == null) { turnStartTime.put(key, LocalDateTime.now()); continue; }
            long elapsed = java.time.Duration.between(start, LocalDateTime.now()).getSeconds();
            boolean disconnected = !isConnected(currentId);
            if (elapsed >= TIMEOUT_SECONDS || disconnected) {
                try {
                    PlayerAction action = disconnected ? PlayerAction.FOLD
                            : (game.getActualBet() > 0 ? PlayerAction.CALL : PlayerAction.CHECK);
                    PlayerActionRequestDTO req = new PlayerActionRequestDTO();
                    req.setGameId(key); req.setPlayerId(currentId);
                    req.setAction(action); req.setRaiseAmount(0);
                    playerAction(req);
                    turnStartTime.put(key, LocalDateTime.now());
                    if (disconnected) {
                        try {
                            LeaveLobbyRequestDTO leaveReq = LeaveLobbyRequestDTO.builder()
                                    .playerId(currentId)
                                    .lobbyId(null)
                                    .build();
                            lobbyService.removePlayer(leaveReq);
                            lastHeartbeat.remove(currentId);
                        } catch (Exception ex) {
                            log.warn("No se pudo remover jugador {} del lobby: {}", currentId, ex.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error acción automática jugador {}: {}", currentId, e.getMessage());
                    turnStartTime.put(key, LocalDateTime.now());
                }
            }
        }
    }

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
            player.setTotalBet(0);
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
        game.setMaxBet(0);

        int dealerIndex = game.getDealerIndex();
        int smallBlindIndex = nextActivePlayer(players, dealerIndex);
        int bigBlindIndex = nextActivePlayer(players, smallBlindIndex);
        int firstToAct = nextActivePlayer(players, bigBlindIndex);

        game.setDealerIndex(dealerIndex);
        game.setSmallBlindIndex(smallBlindIndex);
        game.setBigBlindIndex(bigBlindIndex);
        game.setCurrentPlayerIndex(firstToAct);
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
            case CALL  -> handleCall(game, player, request.getGameId());
            case RAISE -> handleRaise(game, player, request.getRaiseAmount(), request.getGameId());
            case FOLD  -> handleFold(player);
        }

        playerRepository.save(player);
        List<Player> activePlayers = players.stream()
                .filter(p -> p.isInLobby() && !p.isFolded())
                .toList();

        if (activePlayers.size() == 1) {
            Player winner = activePlayers.get(0);
            winner.setCredit(winner.getCredit() + game.getPot());
            playerRepository.save(winner);
            game.setWinner(winner);
            game.setInGame(false);
            walletEventPublisher.publishBetWon(winner.getId(), game.getPot(), game.getId());
            players.stream()
                    .filter(p -> p.isInLobby() && !p.getId().equals(winner.getId()))
                    .forEach(p -> walletEventPublisher.publishBetConfirmed(p.getId(), p.getTotalBet(), game.getId()));
            gameRepository.save(game);
            return gameMapper.toPublicDTO(game);
        }

        int nextIndex = nextActivePlayer(players, game.getCurrentPlayerIndex());
        game.setCurrentPlayerIndex(nextIndex);
        game.setPlayersActedThisRound(game.getPlayersActedThisRound() + 1);

        List<Player> activeNonFolded = players.stream()
                .filter(p -> p.isInLobby() && !p.isFolded())
                .toList();

        boolean allBetsEqual = activeNonFolded.stream()
                .allMatch(p -> p.getCurrentBet() == game.getActualBet());

        if (allBetsEqual && game.getPlayersActedThisRound() >= activeNonFolded.size()) {
            if (game.getPhase() != GamePhase.SHOWDOWN) {
                nextPhase(game);
            }
        }

        gameRepository.save(game);
        return gameMapper.toPublicDTO(game);
    }

    private void nextPhase(Game game) {
        GamePhase currentPhase = game.getPhase();
        switch (currentPhase) {
            case PREFLOP -> revealCommunityCards(game, 3, GamePhase.FLOP);
            case FLOP    -> revealCommunityCards(game, 1, GamePhase.TURN);
            case TURN    -> revealCommunityCards(game, 1, GamePhase.RIVER);
            case RIVER   -> {
                game.setPhase(GamePhase.SHOWDOWN);
                evaluateShowdown(game);
            }
            default -> {}
        }
        resetRoundBets(game);
        List<Player> players = game.getPlayers();
        if (currentPhase != GamePhase.RIVER) {
            int firstToAct = nextActivePlayer(players, game.getDealerIndex());
            game.setCurrentPlayerIndex(firstToAct);
        }
    }

    private void evaluateShowdown(Game game) {
        List<Cart> communityCards = game.getCartsInTable();
        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.isInLobby() && !p.isFolded())
                .toList();

        Player winner = activePlayers.stream()
                .max((a, b) -> Integer.compare(
                        evaluateHand(a.getHand(), communityCards),
                        evaluateHand(b.getHand(), communityCards)
                ))
                .orElse(null);

        if (winner != null) {
            winner.setCredit(winner.getCredit() + game.getPot());
            playerRepository.save(winner);
            game.setWinner(winner);
            game.setInGame(false);
            walletEventPublisher.publishBetWon(winner.getId(), game.getPot(), game.getId());
            final String winnerId = winner.getId();
            activePlayers.stream()
                    .filter(p -> !p.getId().equals(winnerId))
                    .forEach(p -> walletEventPublisher.publishBetConfirmed(p.getId(), p.getTotalBet(), game.getId()));
        }
    }

    private int evaluateHand(List<Cart> hand, List<Cart> community) {
        if (hand == null || hand.isEmpty() || community == null) return 0;
        List<Cart> all = Stream.concat(hand.stream(), community.stream()).toList();

        Map<String, Long> valueCount = all.stream()
                .collect(Collectors.groupingBy(Cart::getValue, Collectors.counting()));
        Map<String, Long> suitCount = all.stream()
                .collect(Collectors.groupingBy(c -> c.getSuit().name(), Collectors.counting()));

        List<Long> counts = valueCount.values().stream()
                .sorted(Comparator.reverseOrder())
                .toList();
        long maxFreq    = counts.get(0);
        long secondFreq = counts.size() > 1 ? counts.get(1) : 0;

        boolean flush        = suitCount.values().stream().anyMatch(v -> v >= 5);
        boolean straight     = hasStraight(all);
        boolean royal        = hasRoyalFlush(all);
        boolean straightFlush = hasStraightFlush(all);
        boolean four         = maxFreq == 4;
        boolean fullHouse    = maxFreq == 3 && secondFreq >= 2;
        boolean three        = maxFreq == 3 && secondFreq < 2;
        boolean twoPairs     = maxFreq == 2 && secondFreq == 2;
        boolean onePair      = maxFreq == 2 && secondFreq < 2;

        int ranking;
        if (royal)               ranking = 9;
        else if (straightFlush)  ranking = 8;
        else if (four)           ranking = 7;
        else if (fullHouse)      ranking = 6;
        else if (flush)          ranking = 5;
        else if (straight)       ranking = 4;
        else if (three)          ranking = 3;
        else if (twoPairs)       ranking = 2;
        else if (onePair)        ranking = 1;
        else                     ranking = 0;

        if (ranking == 4 || ranking == 8) {
            int highStraight = highestStraightCard(all);
            return ranking * 100000 + highStraight;
        }

        List<Integer> sortedValues = valueCount.entrySet().stream()
                .sorted((a, b) -> {
                    int freqCmp = Long.compare(b.getValue(), a.getValue());
                    if (freqCmp != 0) return freqCmp;
                    return Integer.compare(cardValue(b.getKey()), cardValue(a.getKey()));
                })
                .map(e -> cardValue(e.getKey()))
                .limit(5)
                .collect(Collectors.toList());

        int tiebreak = 0;
        int multiplier = 1;
        for (int i = sortedValues.size() - 1; i >= 0; i--) {
            tiebreak += sortedValues.get(i) * multiplier;
            multiplier *= 15;
        }

        return ranking * 100000 + tiebreak;
    }

    private boolean hasRoyalFlush(List<Cart> cards) {
        String[] royalValues = {"A", "K", "Q", "J", "10"};
        for (String suit : new String[]{"SPADES", "HEARTS", "DIAMONDS", "CLUBS"}) {
            List<String> suitCards = cards.stream()
                    .filter(c -> c.getSuit().name().equals(suit))
                    .map(Cart::getValue)
                    .toList();
            boolean isRoyal = true;
            for (String v : royalValues) {
                if (!suitCards.contains(v)) { isRoyal = false; break; }
            }
            if (isRoyal) return true;
        }
        return false;
    }

    private boolean hasStraightFlush(List<Cart> cards) {
        Map<String, List<Cart>> bySuit = cards.stream()
                .collect(Collectors.groupingBy(c -> c.getSuit().name()));
        for (List<Cart> suited : bySuit.values()) {
            if (suited.size() >= 5 && hasStraight(suited)) return true;
        }
        return false;
    }

    private boolean hasStraight(List<Cart> cards) {
        List<Integer> values = cards.stream()
                .map(c -> cardValue(c.getValue()))
                .distinct()
                .sorted()
                .toList();
        int consecutive = 1;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) == values.get(i - 1) + 1) {
                consecutive++;
                if (consecutive >= 5) return true;
            } else {
                consecutive = 1;
            }
        }
        if (values.contains(14)) {
            List<Integer> withLowAce = new ArrayList<>(values);
            withLowAce.add(0, 1);
            int consec = 1;
            for (int i = 1; i < withLowAce.size(); i++) {
                if (withLowAce.get(i) == withLowAce.get(i - 1) + 1) {
                    consec++;
                    if (consec >= 5) return true;
                } else consec = 1;
            }
        }
        return false;
    }

    private int highestStraightCard(List<Cart> cards) {
        List<Integer> values = cards.stream()
                .map(c -> cardValue(c.getValue()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        for (int i = 0; i < values.size() - 4; i++) {
            boolean isStr = true;
            for (int j = 0; j < 4; j++) {
                if (values.get(i + j) - values.get(i + j + 1) != 1) { isStr = false; break; }
            }
            if (isStr) return values.get(i);
        }
        if (values.contains(14) && values.contains(2) && values.contains(3)
                && values.contains(4) && values.contains(5)) return 5;
        return values.get(0);
    }

    private int cardValue(String value) {
        return switch (value) {
            case "A"  -> 14;
            case "K"  -> 13;
            case "Q"  -> 12;
            case "J"  -> 11;
            case "10" -> 10;
            default   -> Integer.parseInt(value);
        };
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

    private void handleCheck(Game game, Player player) {
        if (game.getActualBet() > player.getCurrentBet()) {
            throw new GameBadRequestException("Cannot check, there is an active bet. Call or raise");
        }
        log.info("Player {} checks", player.getId());
    }

    private void handleCall(Game game, Player player, String gameId) {
        int amountToCall = game.getActualBet() - player.getCurrentBet();
        if (amountToCall <= 0) throw new GameBadRequestException("Nothing to call, you can check");
        // Si no tiene suficiente, paga todo lo que tiene
        int toPay = Math.min(amountToCall, player.getCredit());
        player.setCredit(player.getCredit() - toPay);
        player.setCurrentBet(player.getCurrentBet() + toPay);
        player.setTotalBet(player.getTotalBet() + toPay);
        game.setPot(game.getPot() + toPay);
        walletEventPublisher.publishBetConfirmed(player.getId(), toPay, gameId);
        log.info("Player {} calls {}", player.getId(), toPay);
    }

    private void handleRaise(Game game, Player player, int raiseAmount, String gameId) {
        int totalBet = game.getActualBet() + raiseAmount;
        if (totalBet <= game.getActualBet()) {
            throw new GameBadRequestException("Raise must be higher than current bet");
        }
        int amountNeeded = totalBet - player.getCurrentBet();
        if (amountNeeded <= 0) {
            throw new GameBadRequestException("Raise must be higher than your current bet");
        }
        // Si no tiene suficiente, paga todo lo que tiene
        int toPay = Math.min(amountNeeded, player.getCredit());
        player.setCredit(player.getCredit() - toPay);
        player.setCurrentBet(player.getCurrentBet() + toPay);
        player.setTotalBet(player.getTotalBet() + toPay);
        game.setPot(game.getPot() + toPay);
        game.setActualBet(player.getCurrentBet());
        game.setActualRaise(raiseAmount);
        game.setPlayersActedThisRound(0);
        walletEventPublisher.publishBetConfirmed(player.getId(), toPay, gameId);
        log.info("Player {} raises to {}", player.getId(), player.getCurrentBet());
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