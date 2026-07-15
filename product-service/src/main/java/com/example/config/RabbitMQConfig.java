package com.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:admin}")
    private String username;

    @Value("${spring.rabbitmq.password:admin}")
    private String password;

    @Value("${rabbitmq.exchange:order.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.order:order.created}")
    private String orderRoutingKey;

    @Value("${rabbitmq.routing.response:order.response}")
    private String responseRoutingKey;

    @Value("${rabbitmq.queue.order:order.queue}")
    private String orderQueueName;

    @Value("${rabbitmq.queue.response:order.response.queue}")
    private String responseQueueName;

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange,true,false);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setConnectionTimeout(30000);
        connectionFactory.setRequestedHeartBeat(30);
        return connectionFactory;
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(orderQueueName, true);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(responseQueueName, true);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(exchange())
                .with(orderRoutingKey);
    }

    @Bean
    public Binding responseBinding() {
        return BindingBuilder
                .bind(responseQueue())
                .to(exchange())
                .with(responseRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setReplyTimeout(10000);
        return rabbitTemplate;
    }
}