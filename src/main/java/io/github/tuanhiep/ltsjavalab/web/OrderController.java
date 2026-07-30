package io.github.tuanhiep.ltsjavalab.web;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record CreateOrder(@NotBlank String sku, @Positive int quantity) {
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateOrder request) {
        return ResponseEntity.ok(Map.of("sku", request.sku(), "quantity", request.quantity()));
    }

    @GetMapping("/quantity")
    public ResponseEntity<Map<String, Integer>> quantity(@RequestParam @Positive int value) {
        return ResponseEntity.ok(Map.of("quantity", value));
    }
}
