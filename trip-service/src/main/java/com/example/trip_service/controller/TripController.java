package com.example.trip_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.trip_service.dto.CreateTripRequest;
import com.example.trip_service.entity.Trip;
import com.example.trip_service.service.TripService;

@RestController
@RequestMapping("/trips")
public class TripController {
    
    @Autowired
    private TripService tripService;
    
    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody CreateTripRequest request) {
        try {
            Trip trip = tripService.createTrip(request);
            return ResponseEntity.ok(trip);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("Пассажир не найден")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", message));
            }
            if (message != null && message.contains("Нет свободных водителей")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", message));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
        }
    }
    
    @GetMapping("/{id}")
    public Trip getTrip(@PathVariable Long id) {
        return tripService.getTrip(id);
    }
    
    @GetMapping
    public List<Trip> getTrips(@RequestParam("passenger_id") Long passengerId) {
        return tripService.getPassengerTrips(passengerId);
    }

    @PatchMapping("/{id}/status")
    public Trip updateStatus(@PathVariable Long id, @RequestParam String status) {
        return tripService.updateStatus(id, status);
    }

    @PatchMapping("/{id}/rating")
    public Trip addRating(@PathVariable Long id, @RequestParam Integer rating) {
        return tripService.addRating(id, rating);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        if (message != null && message.contains("не найден")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", message));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", message));
    }
}

