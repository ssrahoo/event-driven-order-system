package order_service.controller;

import order_service.dto.OrderCreatedEvent;
import order_service.publisher.OrderCreatedPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCreatedPublisher publisher;

    public OrderController(OrderCreatedPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody OrderCreatedEvent order) {
        publisher.publishOrderCreated(order);
        return ResponseEntity.accepted().build();
    }
}