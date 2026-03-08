package common.event;

import java.math.BigDecimal;

public record OrderItemEvent(String productId, Integer quantity, BigDecimal price){}
