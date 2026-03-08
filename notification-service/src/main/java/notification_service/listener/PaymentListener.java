package notification_service.listener;

import com.rabbitmq.client.Channel;
import common.event.PaymentEvent;
import jakarta.transaction.Transactional;
import notification_service.config.RabbitMqConfig;
import notification_service.entity.Notification;
import notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentListener {
    private final Logger logger = LoggerFactory.getLogger(InventoryListener.class);

    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;

    public PaymentListener(NotificationRepository notificationRepository, RabbitTemplate rabbitTemplate) {
        this.notificationRepository = notificationRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    @RabbitListener(
            queues = RabbitMqConfig.NOTIFICATION_SERVICE_QUEUE_PAYMENT,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void listen(PaymentEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        logger.info("Received payment event: {}", event);

        try {
            // skip if notification has already been processed (Idempotency)
            if (notificationRepository.existsByEventId(event.eventId())) {
                logger.info("Notification(payment) already processed for orderId={}, eventId={}", event.orderId(), event.eventId());
                channel.basicAck(tag, false);
                return;
            }

            Notification notification = new Notification();
            notification.setNotificationId(UUID.randomUUID());
            notification.setOrderId(event.orderId());
            notification.setEventId(event.eventId());
            notification.setType(event.status().equals("SUCCESS") ? "PAYMENT_SUCCEEDED" : "PAYMENT_FAILED");
            notification.setMessage(event.status().equals("SUCCESS") ? "Payment succeeded." : "Payment failed.");
            notification.setSentAt(Instant.now());
            notificationRepository.save(notification);

            channel.basicAck(tag, false); // ACK after successful processing
            logger.info("Notification processed for orderId={}", event.orderId());

        } catch (Exception e) {
            logger.error("Failed to process notification for orderId={}", event.orderId(), e);
            try {
                channel.basicNack(tag, false, true); // Requeue message for retry
            } catch (IOException ioException) {
                logger.error("Failed to nack message", ioException);
            }
        }
    }
}