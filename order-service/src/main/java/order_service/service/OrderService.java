package order_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import order_service.dto.OrderDto;
import order_service.dto.OrderItemDto;
import order_service.entity.Order;
import order_service.entity.OrderItem;
import common.entity.OutboxEvent;
import common.event.OrderEvent;
import common.event.OrderItemEvent;
import order_service.repository.OrderRepository;
import order_service.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository orderRepository, OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createOrder(OrderDto orderDto) throws JsonProcessingException {
        logger.info("Creating order");

        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        BigDecimal totalAmount = getTotalAmount(orderDto.items());

        Order order = new Order(
                orderId,
                orderDto.customerId(),
                totalAmount,
                orderDto.currency(),
                null,
                Instant.now(),
                null
        );

        mapAndSetItems(orderDto.items(), order);

        OrderEvent orderEvent = new OrderEvent(
                orderId,
                eventId,
                orderDto.customerId(),
                totalAmount,
                orderDto.currency(),
                mapItems(orderDto.items()),
                Instant.now()
        );

        // Transactional Outbox Pattern
        OutboxEvent outboxEvent = new OutboxEvent(
            UUID.randomUUID(),
            "ORDER",
            order.getOrderId().toString(),
            "ORDER_CREATED",
            objectMapper.writeValueAsString(orderEvent),
            LocalDateTime.now()
        );

        orderRepository.save(order);
        logger.info("Order saved id: {}", orderId);
        outboxRepository.save(outboxEvent);
        logger.info("Outbox event saved id: {}", outboxEvent.getId());
    }

    private BigDecimal getTotalAmount(List<OrderItemDto> items) {
        BigDecimal total = items.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    private static void mapAndSetItems(List<OrderItemDto> items, Order order) {
        List<OrderItem> list = new ArrayList<>();
        for (OrderItemDto dto : items) {
            OrderItem item = new OrderItem(UUID.randomUUID(), order, dto.productId(), dto.quantity(), dto.price());
            list.add(item);
        }
        order.setItems(list);
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
