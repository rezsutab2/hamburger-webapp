package hu.hamburger.backend.service;

import hu.hamburger.backend.exception.ResourceNotFoundException;
import hu.hamburger.backend.model.Order;
import hu.hamburger.backend.model.OrderItem;
import hu.hamburger.backend.model.OrderStatus;
import hu.hamburger.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order placeOrder(Order order) {
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendelés nem található: " + id));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}