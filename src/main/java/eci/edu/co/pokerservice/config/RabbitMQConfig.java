package eci.edu.co.pokerservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WALLET_REQUESTS_EXCHANGE = "wallet.requests.topic";
    public static final String BET_CONFIRMED_ROUTING_KEY = "bet.confirmed";
    public static final String BET_WON_ROUTING_KEY = "bet.won";
    public static final String BET_LOST_ROUTING_KEY = "bet.lost";

    @Bean
    public TopicExchange walletRequestsExchange() {
        return new TopicExchange(WALLET_REQUESTS_EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}