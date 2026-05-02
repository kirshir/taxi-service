package com.example.trip_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;

import com.example.trip_service.dto.CreateTripRequest;
import com.example.trip_service.entity.Trip;
import com.example.trip_service.repository.TripRepository;

@Service
public class TripService {
    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private UserClient userClient;

    @Autowired
    private RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${notification.service.url:http://localhost:8083}")
    private String notificationServiceUrl;

    private void sendNotification(Long tripId, String recipientType, Long recipientId, String message) {
        try {
            String url = notificationServiceUrl + "/notifications";
            Map<String, Object> payload = Map.of(
                    "tripId", tripId,
                    "recipientType", recipientType,
                    "recipientId", recipientId,
                    "message", message
            );
            restTemplate.postForObject(url, payload, Object.class);
            System.out.println("Уведомление отправлено в Notification Service");
        } catch (Exception e) {
            System.err.println("Ошибка отправки уведомления: " + e.getMessage());
        }
    }

    private void sendNotificationAfterCommit(Long tripId, String recipientType, Long recipientId, String message) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendNotification(tripId, recipientType, recipientId, message);
                }
            });
            return;
        }
        sendNotification(tripId, recipientType, recipientId, message);
    }
    
    @Transactional
    public Trip createTrip(CreateTripRequest request) {

        if (!userClient.checkPassengerExists(request.getPassengerId())) {
            throw new RuntimeException("Пассажир не найден");
        }

        Trip trip = new Trip(
            request.getPassengerId(),
            request.getOrigin(),
            request.getDestination()
        );
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        Long driverId = userClient.findFreeDriver();
        System.out.println("Назначен водитель: " + driverId);

        trip.setDriverId(driverId);
        trip.setStatus("DRIVER_ASSIGNED");

        userClient.updateDriverStatus(driverId, "BUSY");
        trip.setPrice(100.0);

        Trip savedTrip = tripRepository.save(trip);

        sendNotificationAfterCommit(savedTrip.getId(), "PASSENGER", savedTrip.getPassengerId(),
            "Ваша поездка создана. Водитель назначен.");
        
        sendNotificationAfterCommit(savedTrip.getId(), "DRIVER", savedTrip.getDriverId(),
            "Вам назначена новая поездка.");

        return savedTrip;
    }


    public Trip getTrip(Long id) {
        return tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Поездка не найдена с ID: " + id));
    }
    
    public List<Trip> getPassengerTrips(Long passengerId) {
        return tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId);
    }
    
    @Transactional
    public Trip updateStatus(Long tripId, String status) {
        Trip trip = getTrip(tripId);
        
        String oldStatus = trip.getStatus();
        trip.setStatus(status);
        trip.setUpdatedAt(LocalDateTime.now());
        
        if ((status.equals("COMPLETED") || status.equals("CANCELLED")) 
            && oldStatus.equals("DRIVER_ASSIGNED")) {
            
            Long driverId = trip.getDriverId();
            if (driverId != null) {
                userClient.updateDriverStatus(driverId, "FREE");
            }
        }

        String passengerMessage = "Статус вашей поездки изменён с " + oldStatus + " на " + status;
        String driverMessage = "Статус поездки изменён с " + oldStatus + " на " + status;

        if (status.equals("COMPLETED")) {
            passengerMessage = "Ваша поездка завершена.";
            driverMessage = "Поездка завершена.";
        } else if (status.equals("CANCELLED")) {
            passengerMessage = "Ваша поездка отменена.";
            driverMessage = "Поездка отменена.";
        }

        sendNotificationAfterCommit(tripId, "PASSENGER", trip.getPassengerId(), passengerMessage);
    
        if (trip.getDriverId() != null) {
            sendNotificationAfterCommit(tripId, "DRIVER", trip.getDriverId(), driverMessage);
        }
        
        return tripRepository.save(trip);
    }

    @Transactional
    public Trip addRating(Long tripId, Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Оценка должна быть от 1 до 5");
        }
        
        Trip trip = getTrip(tripId);
        
        if (!"COMPLETED".equals(trip.getStatus())) {
            throw new RuntimeException("Оценить поездку можно только после её завершения");
        }
        
        trip.setRating(rating);
        
        sendNotification(tripId, "DRIVER", trip.getDriverId(), 
            "Пассажир оценил поездку на " + rating + " звёзд");
        
        return tripRepository.save(trip);
    }
}