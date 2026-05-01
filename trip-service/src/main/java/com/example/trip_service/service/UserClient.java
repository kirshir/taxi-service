package com.example.trip_service.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserClient {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${user.service.url}")
    private String userServiceUrl;
    
    public boolean checkPassengerExists(Long passengerId) {
        try {
            String url = userServiceUrl + "/passengers/" + passengerId;
            restTemplate.getForObject(url, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public Long findFreeDriver() {
        try {
            String url = userServiceUrl + "/drivers/free";
            System.out.println("URL запроса: " + url);
            
            String jsonResponse = restTemplate.getForObject(url, String.class);
            System.out.println("Ответ от user-service: " + jsonResponse);
            
            Object[] drivers = restTemplate.getForObject(url, Object[].class);
            System.out.println("Массив водителей: " + java.util.Arrays.toString(drivers));
            
            if (drivers != null && drivers.length > 0) {
                Map<String, Object> firstDriver = (Map<String, Object>) drivers[0];
                Long driverId = ((Number) firstDriver.get("id")).longValue();
                System.out.println("Назначен водитель ID: " + driverId);
                return driverId;
            }
            
            throw new RuntimeException("Нет свободных водителей");
        } catch (Exception e) {
            System.err.println("ОШИБКА в findFreeDriver: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Ошибка поиска водителя: " + e.getMessage());
        }
    }
    
    public void updateDriverStatus(Long driverId, String status) {
        try {
            String url = userServiceUrl + "/drivers/" + driverId + "/status?status=" + status;
            System.out.println("URL запроса: " + url);
            
            restTemplate.put(url, null);
            System.out.println("PUT выполнен успешно");
            
        } catch (Exception e) {
            System.err.println("ОШИБКА в updateDriverStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }
}