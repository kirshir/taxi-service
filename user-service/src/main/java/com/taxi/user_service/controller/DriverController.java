package com.taxi.user_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taxi.user_service.entity.Driver;
import com.taxi.user_service.service.DriverService;

@RestController
@RequestMapping("/drivers")
public class DriverController {
    
    @Autowired
    private DriverService driverService;
    
    @PostMapping
    public Driver registerDriver(@RequestBody Driver driver) {
        return driverService.registerDriver(driver);
    }
    
    @GetMapping("/{id}")
    public Driver getDriver(@PathVariable Long id) {
        return driverService.getDriver(id);
    }
    
    @PutMapping("/{id}/status")
    public Driver updateStatus(@PathVariable Long id, @RequestParam String status) {
        return driverService.updateStatus(id, status);
    }
    
    @GetMapping("/free")
    public List<Driver> getFreeDrivers() {
        return driverService.getAllFreeDrivers();
    }
}