package notification_service.repository;

import common.entity.OutboxEvent;
import notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    boolean existsByEventId(UUID eventId);
}