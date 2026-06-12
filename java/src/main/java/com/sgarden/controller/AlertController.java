package com.sgarden.controller;

import com.sgarden.dto.ErrorResponse;
import com.sgarden.service.AlertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAlerts() {
        return ResponseEntity.ok(alertService.getAlerts());
    }

    @PutMapping("/threshold")
    public ResponseEntity<?> setThreshold(@RequestBody Map<String, Integer> body) {
        Integer threshold = body.get("threshold");
        if (threshold == null || threshold < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Validation failed",
                            Map.of("threshold", "Threshold must be a non-negative number")));
        }
        return ResponseEntity.ok(Map.of("threshold", alertService.setThreshold(threshold)));
    }
}
