package com.sgarden.service;

import com.sgarden.dto.OrderItemRequest;
import com.sgarden.dto.OrderRequest;
import com.sgarden.exception.BadRequestException;
import com.sgarden.exception.NotFoundException;
import com.sgarden.model.Order;
import com.sgarden.model.OrderItem;
import com.sgarden.model.Product;
import com.sgarden.repository.OrderRepository;
import com.sgarden.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "pending",   Set.of("confirmed", "cancelled"),
            "confirmed", Set.of("shipped"),
            "shipped",   Set.of("delivered"),
            "delivered", Set.of(),
            "cancelled", Set.of()
    );

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public Order createOrder(OrderRequest request) {
        // First pass: fetch all products and validate stock before touching anything
        List<Product> resolvedProducts = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product " + itemReq.getProductId() + " not found"));
            if (product.getStock() < itemReq.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product " + itemReq.getProductId());
            }
            resolvedProducts.add(product);
        }

        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest itemReq = request.getItems().get(i);
            Product product = resolvedProducts.get(i);
            items.add(new OrderItem(itemReq.getProductId(), itemReq.getQuantity()));
            total += product.getPrice() * itemReq.getQuantity();
        }

        Order order = new Order();
        order.setItems(items);
        order.setTotal(total);
        order.setStatus("pending");
        Order saved = orderRepository.save(order);

        // Reduce stock only after order is persisted
        for (int i = 0; i < request.getItems().size(); i++) {
            Product product = resolvedProducts.get(i);
            product.setStock(product.getStock() - request.getItems().get(i).getQuantity());
            productRepository.save(product);
        }

        return saved;
    }

    public List<Order> getAllOrders(String status) {
        if (status != null && !status.isEmpty()) {
            return orderRepository.findByStatus(status);
        }
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(String id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> updateOrderStatus(String id, String newStatus) {
        return orderRepository.findById(id).map(order -> {
            String current = order.getStatus() != null ? order.getStatus() : "pending";
            Set<String> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
            if (!allowed.contains(newStatus)) {
                throw new BadRequestException(
                        "Invalid status transition from '" + current + "' to '" + newStatus + "'");
            }
            order.setStatus(newStatus);
            return orderRepository.save(order);
        });
    }

    public Optional<Order> updateOrder(String id, OrderRequest request) {
        return orderRepository.findById(id).map(order -> {
            List<OrderItem> items = new ArrayList<>();
            double total = 0.0;
            for (OrderItemRequest itemReq : request.getItems()) {
                Product product = productRepository.findById(itemReq.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product " + itemReq.getProductId() + " not found"));
                items.add(new OrderItem(itemReq.getProductId(), itemReq.getQuantity()));
                total += product.getPrice() * itemReq.getQuantity();
            }
            order.setItems(items);
            order.setTotal(total);
            return orderRepository.save(order);
        });
    }

    public boolean deleteOrder(String id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
