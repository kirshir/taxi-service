package com.example.notification_service.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.notification_service.entity.NotificationTask;
import com.example.notification_service.repository.NotificationRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class NotificationWorker {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Value("${notification.worker.thread-count:4}")
    private int threadCount;
    
    private ExecutorService executorService;
    private volatile boolean running = true;
    
    @PostConstruct
    public void startWorkers() {
        System.out.println("Запуск Notification Worker с " + threadCount + " потоками");
        
        executorService = Executors.newFixedThreadPool(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int workerId = i;
            executorService.submit(() -> {
                System.out.println("Воркер " + workerId + " запущен");
                while (running) {
                    try {
                        processOneTask();
                    } catch (Exception e) {
                        System.err.println("Воркер " + workerId + " ошибка: " + e.getMessage());
                    }
                }
                System.out.println("Воркер " + workerId + " остановлен");
            });
        }
    }
    
    private void processOneTask() {
        try {
            java.util.Optional<NotificationTask> taskOpt = notificationRepository.findOnePendingWithLock();
            
            if (taskOpt.isEmpty()) {
                Thread.sleep(1000);
                return;
            }
            
            NotificationTask task = taskOpt.get();
            System.out.println("Обработка задачи ID=" + task.getId());
            
            task.setStatus("PROCESSING");
            notificationRepository.save(task);
            
            boolean success = notificationService.sendNotification(task);
            
            if (success) {
                task.setStatus("SENT");
                System.out.println("Задача ID=" + task.getId() + " отправлена");
            } else {
                task.setAttempts(task.getAttempts() + 1);
                if (task.getAttempts() >= 3) {
                    task.setStatus("FAILED");
                    System.err.println("Задача ID=" + task.getId() + " окончательно провалена после 3 попыток");
                } else {
                    task.setStatus("PENDING"); 
                    System.out.println("Задача ID=" + task.getId() + " будет повторена (попытка " + task.getAttempts() + "/3)");
                }
            }
            
            notificationRepository.save(task);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Ошибка в processOneTask: " + e.getMessage());
        }
    }
    
    @PreDestroy
    public void stopWorkers() {
        System.out.println("Остановка Notification Worker...");
        running = false;
        
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Не все потоки завершились вовремя");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Notification Worker остановлен");
    }
}