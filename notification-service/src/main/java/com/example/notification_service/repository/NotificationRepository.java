package com.example.notification_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.notification_service.entity.NotificationTask;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationTask, Long> {
    
    @Query(value = "SELECT * FROM notification_tasks WHERE status = 'PENDING' LIMIT 1 FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<NotificationTask> findOnePendingWithLock();
    
    List<NotificationTask> findByTripIdOrderByCreatedAtDesc(Long tripId);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationTask n SET n.status = :status WHERE n.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status);
}