package com.taxi.user_service.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taxi.user_service.entity.Passenger;
import com.taxi.user_service.repository.PassengerRepository;

@Service
public class PassengerService {
    
    @Autowired
    private PassengerRepository passengerRepository;
    
    public Passenger registerPassenger(Passenger passenger) {
        passenger.setCreatedAt(LocalDateTime.now());
        return passengerRepository.save(passenger);
    }
    
    public Passenger getPassenger(Long id) {
        return passengerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пассажир не найден с ID: " + id));
    }
}