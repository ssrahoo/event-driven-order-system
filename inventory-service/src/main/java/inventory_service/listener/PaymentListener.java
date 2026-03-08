package inventory_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import common.entity.OutboxEvent;
import common.event.OrderEvent;
import common.event.PaymentEvent;
import common.event.PaymentItemEvent;
import inventory_service.config.RabbitMqConfig;
import inventory_service.entity.InventoryReservation;
import inventory_service.repository.InventoryReservationRepository;
import inventory_service.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class PaymentListener {
    private final Logger logger = LoggerFactory.getLogger(PaymentListener.class);

    private final InventoryReservationRepository inventoryReservationRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public PaymentListener(InventoryReservationRepository inventoryReservationRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate) {
        this.inventoryReservationRepository = inventoryReservationRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    @RabbitListener(
            queues = RabbitMqConfig.INVENTORY_SERVICE_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void listen(PaymentEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        logger.info("Received payment event: {}", event);

        try {
            // skip if inventory reservation has already been processed (Idempotency)
            if (inventoryReservationRepository.existsByOrderId(event.orderId())) {
                logger.info("Payment already processed for orderId={}", event.orderId());
                channel.basicAck(tag, false);
                return;
            }

            boolean inventoryReservationSuccess = simulateInventoryReservation(event);

            // Persist InventoryReservation + Outbox in a single transaction
            for (PaymentItemEvent item : event.items()) {
                InventoryReservation inventoryReservation = new InventoryReservation(
                        UUID.randomUUID(),
                        event.orderId(),
                        item.productId(),
                        item.quantity()
                );
                inventoryReservationRepository.save(inventoryReservation);
            }

            Map<String, Object> payload = Map.of(
                    "orderId", event.orderId(),
                    "eventId", UUID.randomUUID(),
                    "status", inventoryReservationSuccess ? "SUCCESS" : "FAILED"
            );

            // Transactional Outbox Pattern
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "ORDER",
                    event.orderId().toString(),
                    inventoryReservationSuccess ? "INVENTORY_RESERVED" : "INVENTORY_OUT_OF_STOCK",
                    objectMapper.writeValueAsString(payload),
                    LocalDateTime.now()
            );
            outboxRepository.save(outboxEvent);

            channel.basicAck(tag, false); // ACK after successful processing
            logger.info("Inventory reservation processed for orderId={}", event.orderId());

        } catch (Exception e) {
            logger.error("Failed to process inventory reservation for orderId={}", event.orderId(), e);
            try {
                channel.basicNack(tag, false, true); // Requeue message for retry
            } catch (IOException ioException) {
                logger.error("Failed to nack message", ioException);
            }
        }

    }

    private boolean simulateInventoryReservation(PaymentEvent event) {
        return true;
    }

}

