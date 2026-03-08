package common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderEvent(UUID orderId, UUID eventId, String customerId, BigDecimal totalAmount, String currency, List<OrderItemEvent> items, Instant createdAt){}
