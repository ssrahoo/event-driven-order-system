package order_service.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "order.exchange";
    public static final String ROUTING_KEY = "order.created";
    public static final String PAYMENT_SERVICE_QUEUE = "payment-service.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE);
    }

    //TODO: move queue and binding from order-service to payment-service
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_SERVICE_QUEUE).build();
    }

    @Bean
    public Binding paymentBinding(TopicExchange exchange) {
        return BindingBuilder
                .bind(paymentQueue())
                .to(exchange)
                .with(ROUTING_KEY);
                //.with("order.*");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}