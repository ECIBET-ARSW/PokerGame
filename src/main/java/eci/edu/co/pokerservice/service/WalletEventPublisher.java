package eci.edu.co.pokerservice.service;

import eci.edu.co.pokerservice.config.RabbitMQConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBetConfirmed(String userId, int amount, String gameId) {
        BetEvent event = new BetEvent(gameId, userId, (double) amount);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_REQUESTS_EXCHANGE, RabbitMQConfig.BET_CONFIRMED_ROUTING_KEY, event);
        log.info("BetConfirmed publicado: userId={} amount={}", userId, amount);
    }

    public void publishBetWon(String userId, int amount, String gameId) {
        BetEvent event = new BetEvent(gameId, userId, (double) amount);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_REQUESTS_EXCHANGE, RabbitMQConfig.BET_WON_ROUTING_KEY, event);
        log.info("BetWon publicado: userId={} amount={}", userId, amount);
    }

    public void publishBetLost(String userId, int amount, String gameId) {
        BetLostEvent event = new BetLostEvent(gameId, userId, (double) amount);
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_REQUESTS_EXCHANGE, RabbitMQConfig.BET_LOST_ROUTING_KEY, event);
        log.info("BetLost publicado: userId={} amount={}", userId, amount);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BetEvent {
        private String betId;
        private String userId;
        private Double amount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BetLostEvent {
        private String betId;
        private String userId;
        private Double stake;
    }
}