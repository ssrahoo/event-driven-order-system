package payment_service.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "payment.exchange";

    public static final String INVENTORY_ROUTING_KEY = "payment.succeeded";
    public static final String NOTIFICATION_ROUTING_KEY = "payment.*";

    public static final String INVENTORY_SERVICE_QUEUE = "inventory-service.queue";
    public static final String NOTIFICATION_SERVICE_QUEUE = "notification-service.queue";
    public static final String PAYMENT_SERVICE_QUEUE = "payment-service.queue";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(EXCHANGE);
    }

    //TODO: move queue and binding from payment-service to inventory-service and notification-service
    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(INVENTORY_SERVICE_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_SERVICE_QUEUE).build();
    }

    @Bean
    public Binding inventoryBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(exchange)
                .with(INVENTORY_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(notificationQueue())
                .to(exchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // Manual ack
        return factory;
    }

}
