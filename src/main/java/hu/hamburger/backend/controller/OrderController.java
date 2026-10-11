package hu.hamburger.backend.controller;

import hu.hamburger.backend.dto.StatusUpdateRequest;
import hu.hamburger.backend.model.Order;
import hu.hamburger.backend.model.OrderStatus;
import hu.hamburger.backend.model.User;
import hu.hamburger.backend.service.OrderService;
import hu.hamburger.backend.repository.OrderRepository;
import hu.hamburger.backend.repository.UserRepository;
import hu.hamburger.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Felhasználó nem található"));
    }

    @PostMapping
    public Order placeOrder(@RequestBody Order order, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        order.setUser(user);
        return orderService.placeOrder(order);
    }

    @GetMapping
    public List<Order> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return orderRepository.findByUserId(user.getId());
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendelés nem található: " + id));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Rendelés nem található: " + id);
        }

        return order;
    }

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Érvénytelen állapot: " + request.status());
        }
        return orderService.updateStatus(id, status);
    }
}