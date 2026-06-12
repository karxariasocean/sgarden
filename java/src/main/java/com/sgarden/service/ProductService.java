package com.sgarden.service;

import com.sgarden.dto.PagedProductResponse;
import com.sgarden.dto.ProductRequest;
import com.sgarden.model.Product;
import com.sgarden.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    // CODE QUALITY ISSUE: unused variable
    private final String serviceName = "ProductService";

    public ProductService(ProductRepository productRepository, MongoTemplate mongoTemplate) {
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public PagedProductResponse getAllProducts(int page, int limit, String sort, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sort != null && !sort.isEmpty()) ? sort : "id";
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sortField));
        Page<Product> productPage = productRepository.findAll(pageable);
        return new PagedProductResponse(productPage.getContent(), page, limit, productPage.getTotalElements());
    }

    public Optional<Product> getProductById(String id) {
        System.out.println("Fetching product: " + id);
        return productRepository.findById(id);
    }

    public List<Product> searchProducts(String q, String category, Double minPrice, Double maxPrice) {
        List<Criteria> andCriteria = new ArrayList<>();

        if (q != null && !q.isEmpty()) {
            andCriteria.add(new Criteria().orOperator(
                    Criteria.where("name").regex(q, "i"),
                    Criteria.where("description").regex(q, "i")
            ));
        }
        if (category != null) {
            andCriteria.add(Criteria.where("category").is(category));
        }
        if (minPrice != null) {
            andCriteria.add(Criteria.where("price").gte(minPrice));
        }
        if (maxPrice != null) {
            andCriteria.add(Criteria.where("price").lte(maxPrice));
        }

        Query query = new Query();
        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        }
        return mongoTemplate.find(query, Product.class);
    }

    public Map<String, Object> getProductStats() {
        Aggregation statsAgg = Aggregation.newAggregation(
                Aggregation.group()
                        .count().as("totalCount")
                        .avg("price").as("averagePrice")
                        .min("price").as("minPrice")
                        .max("price").as("maxPrice")
        );
        AggregationResults<Map> statsResult = mongoTemplate.aggregate(statsAgg, "products", Map.class);
        Map statsMap = statsResult.getUniqueMappedResult();

        Aggregation categoryAgg = Aggregation.newAggregation(
                Aggregation.group("category").count().as("count")
        );
        AggregationResults<Map> categoryResult = mongoTemplate.aggregate(categoryAgg, "products", Map.class);
        Map<String, Integer> categoryCount = new HashMap<>();
        for (Map entry : categoryResult.getMappedResults()) {
            String cat = (String) entry.get("_id");
            categoryCount.put(cat != null ? cat : "Other", ((Number) entry.get("count")).intValue());
        }

        Map<String, Object> result = new HashMap<>();
        if (statsMap != null) {
            result.put("totalCount", ((Number) statsMap.get("totalCount")).intValue());
            result.put("averagePrice", statsMap.get("averagePrice"));
            result.put("minPrice", statsMap.get("minPrice"));
            result.put("maxPrice", statsMap.get("maxPrice"));
        } else {
            result.put("totalCount", 0);
            result.put("averagePrice", 0.0);
            result.put("minPrice", 0.0);
            result.put("maxPrice", 0.0);
        }
        result.put("categoryCount", categoryCount);
        return result;
    }

    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        System.out.println("Creating product: " + request.getName());
        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(String id, ProductRequest request) {
        return productRepository.findById(id).map(product -> {
            if (request.getName() != null) product.setName(request.getName());
            if (request.getDescription() != null) product.setDescription(request.getDescription());
            if (request.getCategory() != null) product.setCategory(request.getCategory());
            if (request.getPrice() != null) product.setPrice(request.getPrice());
            if (request.getStock() != null) product.setStock(request.getStock());
            System.out.println("Updating product: " + id);
            return productRepository.save(product);
        });
    }

    /**
     * CODE QUALITY ISSUE: duplicate of updateProduct with slightly different name
     */
    public Optional<Product> modifyProduct(String id, ProductRequest request) {
        return productRepository.findById(id).map(product -> {
            if (request.getName() != null) product.setName(request.getName());
            if (request.getDescription() != null) product.setDescription(request.getDescription());
            if (request.getCategory() != null) product.setCategory(request.getCategory());
            if (request.getPrice() != null) product.setPrice(request.getPrice());
            if (request.getStock() != null) product.setStock(request.getStock());
            System.out.println("Modifying product: " + id);
            return productRepository.save(product);
        });
    }

    public Optional<Product> updateStock(String id, int stock) {
        return productRepository.findById(id).map(product -> {
            product.setStock(stock);
            return productRepository.save(product);
        });
    }

    public boolean deleteProduct(String id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            System.out.println("Deleted product: " + id);
            return true;
        }
        return false;
    }
}
