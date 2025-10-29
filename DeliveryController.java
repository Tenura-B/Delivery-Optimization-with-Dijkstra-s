package com.dijkstras.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @Autowired
    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    // CORS configuration to allow the frontend (if hosted separately) to access the API
    @CrossOrigin(origins = "*")
    @PostMapping("/order")
    public ResponseEntity<String> addOrder(@RequestBody DeliveryOrder order) {
        try {
            deliveryService.addOrder(order);
            return ResponseEntity.ok("Order added successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/simulate")
    public ResponseEntity<?> startSimulation(
            @RequestParam String startLocation,
            @RequestParam double congestionFactor) {
        try {
            List<DeliveryService.RouteResult> results = deliveryService.runSimulation(startLocation, congestionFactor);
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}