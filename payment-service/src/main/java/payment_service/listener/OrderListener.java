package payment_service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import common.entity.OutboxEvent;
import payment_service.config.RabbitMqConfig;
import payment_service.entity.Payment;
import common.event.OrderEvent;
import payment_service.repository.OutboxRepository;
import payment_service.repository.PaymentRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderListener {
    private final Logger logger = LoggerFactory.getLogger(OrderListener.class);

    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public OrderListener(PaymentRepository paymentRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    @RabbitListener(
            queues = RabbitMqConfig.PAYMENT_SERVICE_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void listen(OrderEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        logger.info("Received order event: {}", event);

        try {
            // skip if payment has already been processed (Idempotency)
            if (paymentRepository.existsByOrderId(event.orderId())) {
                logger.info("Payment already processed for orderId={}", event.orderId());
                channel.basicAck(tag, false);
                return;
            }

            boolean paymentSuccess = simulatePayment(event);

            // Persist Payment + Outbox in a single transaction
            Payment payment = new Payment();
            payment.setOrderId(event.orderId());
            payment.setAmount(event.totalAmount());
            payment.setCurrency(event.currency());
            payment.setCreatedAt(LocalDateTime.now());
            payment.setStatus(paymentSuccess ? "SUCCESS" : "FAILED");
            paymentRepository.save(payment);

            // Create Outbox Event
            Map<String, Object> payload = Map.of(
                    "orderId", event.orderId(),
                    "status", paymentSuccess ? "SUCCESS" : "FAILED",
                    "processedAt", Instant.now(),
                    "items", event.items().stream()
                            .map(item -> Map.of(
                                    "productId", item.productId(),
                                    "quantity", item.quantity()
                            ))
                            .toList()
            );

            // Transactional Outbox Pattern
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "ORDER",
                    event.orderId().toString(),
                    paymentSuccess ? "PAYMENT_SUCCEEDED" : "PAYMENT_FAILED",
                    objectMapper.writeValueAsString(payload),
                    LocalDateTime.now()
            );
            outboxRepository.save(outboxEvent);

            channel.basicAck(tag, false); // ACK after successful processing

            logger.info("Payment processed for orderId={} with status={}", event.orderId(), payment.getStatus());

        } catch (Exception e) {
            logger.error("Failed to process payment for orderId={}", event.orderId(), e);
            try {
                channel.basicNack(tag, false, true); // Requeue message for retry
            } catch (IOException ioException) {
                logger.error("Failed to nack message", ioException);
            }
        }
    }

    private boolean simulatePayment(OrderEvent event) {
        // In production system, call payment gateway here. For this demo, always succeed.
        return true;
    }

}