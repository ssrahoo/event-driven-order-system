package order_service.dto;

import order_service.event.OrderItemEvent;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(String customerId, String currency, List<OrderItemDto> items){}
