package notification_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="notifications")
public class Notification {

    @Id
    @Column(name = "notification_id")
    private UUID notificationId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "event_id")
    private UUID eventId;

    private String type;

    private String message;

    private Instant sentAt;

    public Notification() {}

    public Notification(UUID notificationId, UUID orderId, UUID eventId, String type, String message, Instant sentAt) {
        this.notificationId = notificationId;
        this.orderId = orderId;
        this.eventId = eventId;
        this.type = type;
        this.message = message;
        this.sentAt = sentAt;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}