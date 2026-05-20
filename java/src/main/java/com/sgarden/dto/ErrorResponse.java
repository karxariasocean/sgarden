package com.sgarden.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private Map<String, String> errors;

    public ErrorResponse(String message) {
        this.message = message;
        this.errors = null;
    }
}
