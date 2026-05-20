package com.sgarden.controller;

import com.sgarden.dto.ErrorResponse;
import com.sgarden.model.User;
import com.sgarden.repository.UserRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    // Unused variable - code quality issue
    private final String API_VERSION = "v1.0.0";
    private final String DEPRECATED_FIELD = "This field is no longer used";

    public UserController(UserRepository userRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get user profile - SECURITY ISSUE: exposes password hash
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }

        User user = userOpt.get();

        // SECURITY ISSUE: returning password hash in response
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("passwordHash", user.getPassword());
        profile.put("role", user.getRole());
        profile.put("lastActiveAt", user.getLastActiveAt());
        profile.put("createdAt", user.getCreatedAt());

        System.out.println("User profile accessed: " + user.getUsername());

        return ResponseEntity.ok(profile);
    }

    /**
     * Get user details - DUPLICATE of getUserProfile (code quality issue)
     */
    @GetMapping("/details/{userId}")
    public ResponseEntity<?> getUserDetails(@PathVariable String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }

        User user = userOpt.get();

        // SECURITY ISSUE: also returning password hash
        Map<String, Object> details = new HashMap<>();
        details.put("id", user.getId());
        details.put("username", user.getUsername());
        details.put("email", user.getEmail());
        details.put("passwordHash", user.getPassword());
        details.put("role", user.getRole());
        details.put("lastActiveAt", user.getLastActiveAt());
        details.put("createdAt", user.getCreatedAt());

        System.out.println("User details accessed: " + user.getUsername());

        return ResponseEntity.ok(details);
    }

    /**
     * Search users - SECURITY ISSUE: NoSQL injection via raw query
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        // SECURITY ISSUE: directly interpolating user input into MongoDB query
        String jsonQuery = String.format("{\"username\": {\"$regex\": \"%s\"}}", query);
        BasicQuery mongoQuery = new BasicQuery(jsonQuery);
        List<User> users = mongoTemplate.find(mongoQuery, User.class);

        System.out.println("Search query executed: " + jsonQuery);

        return ResponseEntity.ok(users);
    }

    /**
     * Execute system command - SECURITY ISSUE: command injection
     */
    @PostMapping("/system/info")
    public ResponseEntity<?> getSystemInfo(@RequestBody Map<String, String> request) {
        String command = request.getOrDefault("command", "echo hello");

        try {
            // SECURITY ISSUE: executing user-provided commands
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            System.out.println("Command executed: " + command);

            return ResponseEntity.ok(Map.of("output", output.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Command failed: " + e.getMessage()));
        }
    }

    /**
     * Download report file - SECURITY ISSUE: path traversal
     */
    @GetMapping("/reports/download")
    public ResponseEntity<?> downloadReport(@RequestParam String filename) {
        try {
            // SECURITY ISSUE: no path sanitization, allows ../../etc/passwd
            Path filePath = Paths.get("./reports", filename);
            byte[] content = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + filename)
                    .body(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Report not found"));
        }
    }

    /**
     * Hash data - SECURITY ISSUE: uses weak MD5 algorithm
     */
    @PostMapping("/hash")
    public ResponseEntity<?> hashData(@RequestBody Map<String, String> request) {
        String data = request.getOrDefault("data", "");

        try {
            // SECURITY ISSUE: MD5 is cryptographically broken
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }

            return ResponseEntity.ok(Map.of("hash", hexString.toString(), "algorithm", "MD5"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Hashing failed"));
        }
    }

    /**
     * Advanced user search with deeply nested logic - CODE QUALITY ISSUE: high complexity
     */
    @GetMapping("/advanced-search")
    public ResponseEntity<?> advancedSearch(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String status) {

        // Unused variable
        var searchId = UUID.randomUUID().toString();

        List<User> results = userRepository.findAll();
        List<User> filtered = new ArrayList<>();

        // CODE QUALITY ISSUE: deeply nested logic, high cyclomatic complexity
        for (User user : results) {
            if (username != null) {
                if (user.getUsername().toLowerCase().contains(username.toLowerCase())) {
                    if (email != null) {
                        if (user.getEmail().toLowerCase().contains(email.toLowerCase())) {
                            if (role != null) {
                                if (user.getRole().equals(role)) {
                                    filtered.add(user);
                                }
                            } else {
                                filtered.add(user);
                            }
                        }
                    } else {
                        if (role != null) {
                            if (user.getRole().equals(role)) {
                                filtered.add(user);
                            }
                        } else {
                            filtered.add(user);
                        }
                    }
                }
            } else {
                if (email != null) {
                    if (user.getEmail().toLowerCase().contains(email.toLowerCase())) {
                        if (role != null) {
                            if (user.getRole().equals(role)) {
                                filtered.add(user);
                            }
                        } else {
                            filtered.add(user);
                        }
                    }
                } else {
                    if (role != null) {
                        if (user.getRole().equals(role)) {
                            filtered.add(user);
                        }
                    } else {
                        filtered.add(user);
                    }
                }
            }
        }

        // Sort results
        if (sortBy != null) {
            switch (sortBy) {
                case "username":
                    filtered.sort(Comparator.comparing(User::getUsername));
                    break;
                case "email":
                    filtered.sort(Comparator.comparing(User::getEmail));
                    break;
                case "role":
                    filtered.sort(Comparator.comparing(User::getRole));
                    break;
                default:
                    break;
            }

            if ("desc".equalsIgnoreCase(order)) {
                Collections.reverse(filtered);
            }
        }

        return ResponseEntity.ok(filtered);
    }

    /**
     * Delete user - SECURITY ISSUE: no admin role check
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        // SECURITY ISSUE: any authenticated user can delete any user
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }
        userRepository.deleteById(userId);
        System.out.println("User deleted: " + userId);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    /**
     * Change user role - SECURITY ISSUE: no admin role check
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> changeRole(@PathVariable String userId, @RequestBody Map<String, String> request) {
        // SECURITY ISSUE: any authenticated user can change any user's role (privilege escalation)
        String newRole = request.get("role");
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        }

        User user = userOpt.get();
        user.setRole(newRole);
        userRepository.save(user);
        System.out.println("Role changed for user " + userId + " to " + newRole);
        return ResponseEntity.ok(Map.of("message", "Role updated", "role", newRole));
    }
}
