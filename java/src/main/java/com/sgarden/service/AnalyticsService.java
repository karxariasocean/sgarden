package com.sgarden.service;

import com.sgarden.model.Order;
import com.sgarden.model.OrderItem;
import com.sgarden.repository.ProductRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final ProductRepository productRepository;

    public AnalyticsService(MongoTemplate mongoTemplate, ProductRepository productRepository) {
        this.mongoTemplate = mongoTemplate;
        this.productRepository = productRepository;
    }

    public Map<String, Object> getSalesAnalytics(String startDate, String endDate) {
        Query query = new Query();
        if (startDate != null || endDate != null) {
            Criteria criteria = Criteria.where("createdAt");
            if (startDate != null) criteria = criteria.gte(parseDate(startDate, false));
            if (endDate != null) criteria = criteria.lte(parseDate(endDate, true));
            query.addCriteria(criteria);
        }

        List<Order> orders = mongoTemplate.find(query, Order.class);

        double totalRevenue = orders.stream()
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal() : 0.0)
                .sum();
        int totalOrders = orders.size();

        // Aggregate quantities per product
        Map<String, Integer> productQuantities = new HashMap<>();
        for (Order order : orders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    productQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
                }
            }
        }

        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            productRepository.findById(entry.getKey()).ifPresent(product -> {
                double productRevenue = entry.getValue() * (product.getPrice() != null ? product.getPrice() : 0.0);
                Map<String, Object> stat = new HashMap<>();
                stat.put("productId", product.getId());
                stat.put("name", product.getName());
                stat.put("totalQuantity", entry.getValue());
                stat.put("quantity", entry.getValue());
                stat.put("totalRevenue", productRevenue);
                stat.put("revenue", productRevenue);
                topProducts.add(stat);
            });
        }
        topProducts.sort((a, b) -> Double.compare((Double) b.get("totalRevenue"), (Double) a.get("totalRevenue")));

        // Revenue grouped by day
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
        Map<String, Double> periodMap = new TreeMap<>();
        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                String period = fmt.format(order.getCreatedAt());
                periodMap.merge(period, order.getTotal() != null ? order.getTotal() : 0.0, Double::sum);
            }
        }

        List<Map<String, Object>> revenueByPeriod = periodMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("period", e.getKey());
                    m.put("revenue", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("totalOrders", totalOrders);
        result.put("topProducts", topProducts);
        result.put("revenueByPeriod", revenueByPeriod);
        return result;
    }

    private Instant parseDate(String date, boolean endOfDay) {
        LocalDate localDate = LocalDate.parse(date);
        if (endOfDay) {
            return localDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
        }
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
