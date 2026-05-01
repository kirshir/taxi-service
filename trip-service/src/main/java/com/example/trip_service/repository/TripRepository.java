package com.example.trip_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.trip_service.entity.Trip;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    List<Trip> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
}