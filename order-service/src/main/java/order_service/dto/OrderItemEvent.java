package order_service.dto;

import java.math.BigDecimal;

public record OrderItemEvent(String productId, Integer quantity, BigDecimal price){}
