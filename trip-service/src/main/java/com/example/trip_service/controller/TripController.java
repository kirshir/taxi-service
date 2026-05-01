package com.example.trip_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public Trip createTrip(@RequestBody CreateTripRequest request) {
        return tripService.createTrip(request);
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
}

