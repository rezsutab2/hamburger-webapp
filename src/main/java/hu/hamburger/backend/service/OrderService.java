package hu.hamburger.backend.service;

import hu.hamburger.backend.model.Order;
import hu.hamburger.backend.model.OrderItem;
import hu.hamburger.backend.model.OrderStatus;
import hu.hamburger.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order placeOrder(Order order) {
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // minden OrderItem-hez be kell állítani a vissza-hivatkozást az Order-re,
        // mert a mappedBy = "order" ezt várja el a JPA-tól
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        return orderRepository.save(order); // cascade miatt az items is mentődik
    }
}