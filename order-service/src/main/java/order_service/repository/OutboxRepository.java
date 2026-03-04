package order_service.repository;

import order_service.entity.Order;
import order_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID>{
    List<OutboxEvent> findByProcessedFalse();
    List<OutboxEvent> findTop100ByProcessedFalseOrderByCreatedAtAsc();
}