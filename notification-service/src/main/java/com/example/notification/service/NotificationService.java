package com.example.notification.service;

import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void receiveMessage(String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notificationRepository.save(notification);

        System.out.println("Notification received and saved: " + message);
    }
}