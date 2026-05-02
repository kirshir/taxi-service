package com.example.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.entity.Passenger;
import com.example.user_service.service.PassengerService;

@RestController
@RequestMapping("/passengers")
public class PassengerController {
    
    @Autowired
    private PassengerService passengerService;
    
    @PostMapping
    public Passenger registerPassenger(@RequestBody Passenger passenger) {
        return passengerService.registerPassenger(passenger);
    }
    
    @GetMapping("/{id}")
    public Passenger getPassenger(@PathVariable Long id) {
        return passengerService.getPassenger(id);
    }
}