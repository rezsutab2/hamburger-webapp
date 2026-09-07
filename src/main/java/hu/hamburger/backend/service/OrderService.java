package hu.hamburger.backend.service;

import hu.hamburger.backend.exception.ResourceNotFoundException;
import hu.hamburger.backend.model.Order;
import hu.hamburger.backend.model.OrderStatus;
import hu.hamburger.backend.model.User;
import hu.hamburger.backend.repository.OrderItemRepository;
import hu.hamburger.backend.repository.OrderRepository;
import hu.hamburger.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Order placeOrder(String email, Order order) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Felhasználó nem található!"));

        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // 4. Elmentjük a tételeket
        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                item.setOrder(savedOrder);
                orderItemRepository.save(item);
            });
        }

        return savedOrder;
    }
}