package order_service.service;

import order_service.dto.OrderDto;
import order_service.dto.OrderItemDto;
import order_service.event.OrderEvent;
import order_service.event.OrderItemEvent;
import order_service.publisher.OrderPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderPublisher publisher;

    public OrderService(OrderPublisher publisher) {
        this.publisher = publisher;
    }

    public void createOrder(OrderDto orderDto) {
        OrderEvent orderEvent = new OrderEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                orderDto.customerId(),
                getTotalAmount(orderDto.items()),
                orderDto.currency(),
                mapItems(orderDto.items()),
                Instant.now()
        );

        publisher.publishOrderCreated(orderEvent);
    }

    private BigDecimal getTotalAmount(List<OrderItemDto> items) {
        BigDecimal total = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    private static List<OrderItemEvent> mapItems(List<OrderItemDto> items) {
        return items.stream()
                .map(orderItemDto -> new OrderItemEvent(
                        orderItemDto.productId(),
                        orderItemDto.quantity(),
                        orderItemDto.price()
                ))
                .toList();
    }



}
