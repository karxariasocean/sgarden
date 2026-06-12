package com.sgarden.service;

import com.sgarden.model.Product;
import com.sgarden.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private volatile int threshold = 10;

    private final ProductRepository productRepository;

    public AlertService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public int setThreshold(int threshold) {
        this.threshold = threshold;
        return this.threshold;
    }

    public List<Map<String, Object>> getAlerts() {
        return productRepository.findByStockLessThan(threshold).stream()
                .map(p -> {
                    int stock = p.getStock() != null ? p.getStock() : 0;
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("productId", p.getId());
                    alert.put("name", p.getName());
                    alert.put("productName", p.getName());
                    alert.put("stock", stock);
                    alert.put("currentStock", stock);
                    alert.put("threshold", threshold);
                    alert.put("severity", getSeverity(stock));
                    return alert;
                })
                .collect(Collectors.toList());
    }

    private String getSeverity(int stock) {
        if (stock < threshold * 0.25) return "critical";
        if (stock < threshold * 0.5) return "warning";
        return "info";
    }
}
