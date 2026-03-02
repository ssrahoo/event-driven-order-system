package order_service.dto;

import java.math.BigDecimal;

public record OrderItemDto(String productId, Integer quantity, BigDecimal price){}