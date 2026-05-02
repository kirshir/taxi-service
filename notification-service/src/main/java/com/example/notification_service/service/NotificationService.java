package com.example.notification_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.notification_service.entity.NotificationTask;
import com.example.notification_service.repository.NotificationRepository;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    public NotificationTask createNotification(Long tripId, String recipientType, Long recipientId, String message) {
        NotificationTask task = new NotificationTask(tripId, recipientType, recipientId, message);
        return notificationRepository.save(task);
    }
    
    public List<NotificationTask> getNotificationsByTrip(Long tripId) {
        return notificationRepository.findByTripIdOrderByCreatedAtDesc(tripId);
    }
    
    public boolean sendNotification(NotificationTask task) {
        try {
            System.out.println("ОТПРАВКА УВЕДОМЛЕНИЯ:");
            System.out.println("   Кому: " + task.getRecipientType() + " ID=" + task.getRecipientId());
            System.out.println("   Сообщение: " + task.getMessage());
            System.out.println("   Статус поездки ID=" + task.getTripId());
            
            Thread.sleep(500);
            
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
            return false;
        }
    }
}