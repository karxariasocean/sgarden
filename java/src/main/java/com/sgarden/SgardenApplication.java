package com.sgarden;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SgardenApplication {

    // CODE QUALITY ISSUE: unused variable
    private static final String unusedVariable = "I am not used anywhere";

    public static void main(String[] args) {
        SpringApplication.run(SgardenApplication.class, args);
    }
}
