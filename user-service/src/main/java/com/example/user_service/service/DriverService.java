package com.example.user_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.user_service.entity.Driver;
import com.example.user_service.repository.DriverRepository;

@Service
public class DriverService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    public Driver registerDriver(Driver driver) {
        driver.setCreatedAt(LocalDateTime.now());
        driver.setStatus("FREE");
        return driverRepository.save(driver);
    }
    
    public Driver getDriver(Long id) {
        return driverRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Водитель не найден с ID: " + id));
    }
    
    public Driver updateStatus(Long id, String status) {
        Driver driver = getDriver(id);
        driver.setStatus(status);
        Driver saved = driverRepository.save(driver);
        System.out.println("Статус водителя " + id + " изменён на " + saved.getStatus());
        return saved;
    }
    
    public List<Driver> getAllFreeDrivers() {
        return driverRepository.findByStatus("FREE");
    }
    
    public Driver findFreeDriver() {
        return driverRepository.findFirstByStatusOrderByIdAsc("FREE")
            .orElseThrow(() -> new RuntimeException("Нет свободных водителей"));
    }
}