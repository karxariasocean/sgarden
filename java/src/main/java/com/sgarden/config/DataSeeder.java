package com.sgarden.config;

import com.sgarden.model.Product;
import com.sgarden.model.User;
import com.sgarden.repository.ProductRepository;
import com.sgarden.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedProducts();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@sgarden.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            userRepository.save(admin);
            logger.info("Seeded admin user");
        }

        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setEmail("user@sgarden.com");
            user.setPassword(passwordEncoder.encode("user1234"));
            user.setRole("user");
            userRepository.save(user);
            logger.info("Seeded regular user");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<Product> products = List.of(
                    createProduct("Wireless Mouse", "Ergonomic wireless mouse with USB receiver", "Electronics", 29.99, 150),
                    createProduct("Mechanical Keyboard", "RGB mechanical keyboard with Cherry MX switches", "Electronics", 89.99, 75),
                    createProduct("USB-C Hub", "7-in-1 USB-C hub with HDMI and Ethernet", "Electronics", 45.99, 200),
                    createProduct("Monitor Stand", "Adjustable monitor stand with USB ports", "Accessories", 34.99, 120),
                    createProduct("Webcam HD", "1080p HD webcam with built-in microphone", "Electronics", 59.99, 90),
                    createProduct("Desk Lamp", "LED desk lamp with adjustable brightness", "Accessories", 24.99, 180),
                    createProduct("Cable Organizer", "Silicone cable management clips, pack of 10", "Accessories", 9.99, 500),
                    createProduct("Laptop Sleeve", "Neoprene laptop sleeve for 15-inch laptops", "Accessories", 19.99, 250),
                    createProduct("External SSD", "1TB portable external SSD, USB 3.2", "Storage", 79.99, 60),
                    createProduct("USB Flash Drive", "64GB USB 3.0 flash drive", "Storage", 12.99, 400),
                    createProduct("Ethernet Cable", "Cat6 ethernet cable, 10 meters", "Networking", 8.99, 300),
                    createProduct("Wi-Fi Router", "Dual-band Wi-Fi 6 router", "Networking", 129.99, 45),
                    createProduct("Mouse Pad XL", "Extended gaming mouse pad, 900x400mm", "Accessories", 15.99, 200),
                    createProduct("Headphone Stand", "Aluminum headphone stand", "Accessories", 22.99, 100),
                    createProduct("Power Strip", "6-outlet power strip with USB charging", "Electronics", 18.99, 350)
            );

            productRepository.saveAll(products);
            logger.info("Seeded {} products", products.size());
        }
    }

    private Product createProduct(String name, String description, String category, double price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }
}
