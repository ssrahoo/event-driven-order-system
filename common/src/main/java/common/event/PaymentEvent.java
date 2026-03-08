package common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PaymentEvent(UUID orderId, UUID eventId, String status, Instant processedAt, List<PaymentItemEvent> items){}
