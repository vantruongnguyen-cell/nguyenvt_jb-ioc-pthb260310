package com.example.identityservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getProducts() {
        List<Map<String, Object>> products = List.of(
                Map.of("id", 1, "name", "Laptop Pro", "price", 1200),
                Map.of("id", 2, "name", "Smartphone X", "price", 800)
        );
        return ResponseEntity.ok(products);
    }
}