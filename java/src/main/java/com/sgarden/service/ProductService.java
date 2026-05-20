package com.sgarden.service;

import com.sgarden.dto.ProductRequest;
import com.sgarden.model.Product;
import com.sgarden.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // CODE QUALITY ISSUE: unused variable
    private final String serviceName = "ProductService";

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        System.out.println("Fetching all products");
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        System.out.println("Fetching product: " + id);
        return productRepository.findById(id);
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

    public boolean deleteProduct(String id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            System.out.println("Deleted product: " + id);
            return true;
        }
        return false;
    }
}
