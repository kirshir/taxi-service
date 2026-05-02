package com.example.notification_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.notification_service.dto.CreateNotificationRequest;
import com.example.notification_service.entity.NotificationTask;
import com.example.notification_service.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping
    public NotificationTask createNotification(
            @RequestBody(required = false) CreateNotificationRequest body,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) String recipientType,
            @RequestParam(required = false) Long recipientId,
            @RequestParam(required = false) String message) {

        Long finalTripId = body != null && body.getTripId() != null ? body.getTripId() : tripId;
        String finalRecipientType = body != null && body.getRecipientType() != null ? body.getRecipientType() : recipientType;
        Long finalRecipientId = body != null && body.getRecipientId() != null ? body.getRecipientId() : recipientId;
        String finalMessage = body != null && body.getMessage() != null ? body.getMessage() : message;

        if (finalTripId == null || finalRecipientType == null || finalRecipientId == null || finalMessage == null) {
            throw new IllegalArgumentException("Не переданы обязательные поля уведомления");
        }
        
        return notificationService.createNotification(finalTripId, finalRecipientType, finalRecipientId, finalMessage);
    }
    
    @GetMapping
    public List<NotificationTask> getNotifications(@RequestParam("trip_id") Long tripId) {
        return notificationService.getNotificationsByTrip(tripId);
    }
}