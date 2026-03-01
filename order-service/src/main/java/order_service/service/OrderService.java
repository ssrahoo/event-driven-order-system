package order_service.service;

import order_service.dto.OrderCreatedEvent;
import order_service.publisher.OrderCreatedPublisher;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderCreatedPublisher publisher;

    public OrderService(OrderCreatedPublisher publisher) {
        this.publisher = publisher;
    }

    public void createOrder(OrderCreatedEvent order) {
        publisher.publishOrderCreated(order);
    }

}
