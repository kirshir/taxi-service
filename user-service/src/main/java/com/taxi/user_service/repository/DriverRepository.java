package com.taxi.user_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taxi.user_service.entity.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    List<Driver> findByStatus(String status);
    
    Optional<Driver> findFirstByStatusOrderByIdAsc(String status);
}