package com.example.trip_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trips")
public class Trip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;
    
    @Column(name = "driver_id")
    private Long driverId;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String origin;
    
    @Column(nullable = false)
    private String destination;
    
    private Double price;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Integer rating;
    
    public Trip() {}
    
    public Trip(Long passengerId, String origin, String destination) {
        this.passengerId = passengerId;
        this.origin = origin;
        this.destination = destination;
        this.status = "WAITING_FOR_DRIVER";
    }
    
    public Long getId() { return id; }
    public Long getPassengerId() { return passengerId; }
    public Long getDriverId() { return driverId; }
    public String getStatus() { return status; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public Double getPrice() { return price; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Integer getRating() { return rating; }
    
    public void setId(Long id) { this.id = id; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public void setStatus(String status) { this.status = status; }
    public void setOrigin(String origin) { this.origin = origin; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setPrice(Double price) { this.price = price; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setRating(Integer rating) { this.rating = rating; }
}