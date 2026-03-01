package order_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(String orderId, String customerId, BigDecimal totalAmount, String currency, List<OrderItemEvent> items){}
