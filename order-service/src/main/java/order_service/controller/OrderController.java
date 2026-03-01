package order_service.controller;

import order_service.dto.OrderCreatedEvent;
import order_service.publisher.OrderCreatedPublisher;
import order_service.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody OrderCreatedEvent order) {
        orderService.createOrder(order);
        return ResponseEntity.accepted().build();
    }
}