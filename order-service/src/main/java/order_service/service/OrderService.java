package order_service.service;

import order_service.dto.OrderDto;
import order_service.dto.OrderItemDto;
import order_service.entity.Order;
import order_service.entity.OrderItem;
import order_service.event.OrderEvent;
import order_service.event.OrderItemEvent;
import order_service.publisher.OrderPublisher;
import order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderPublisher publisher;
    private OrderRepository orderRepository;

    public OrderService(OrderPublisher publisher, OrderRepository orderRepository) {
        this.publisher = publisher;
        this.orderRepository = orderRepository;
    }

    public void createOrder(OrderDto orderDto) {
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        BigDecimal totalAmount = getTotalAmount(orderDto.items());

        OrderEvent orderEvent = new OrderEvent(
                orderId,
                eventId,
                orderDto.customerId(),
                totalAmount,
                orderDto.currency(),
                mapItems(orderDto.items()),
                Instant.now()
        );
        publisher.publishOrderCreated(orderEvent);

        Order order = new Order(
                orderId,
                orderDto.customerId(),
                totalAmount,
                orderDto.currency(),
                null,
                Instant.now(),
                null
        );

        List<OrderItem> orderItemList = new ArrayList<>();
        for (OrderItemDto dto : orderDto.items()) {
            OrderItem item = new OrderItem(UUID.randomUUID(), order, dto.productId(), dto.quantity(), dto.price());
            orderItemList.add(item);
        }
        order.setOrderItemList(orderItemList); // this sets both sides

        orderRepository.save(order);
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
