package order_service.dto;

import java.util.List;

public record OrderDto(String customerId, String currency, List<OrderItemDto> items){}
