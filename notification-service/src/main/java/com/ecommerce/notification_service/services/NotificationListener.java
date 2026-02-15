package com.ecommerce.notification_service.services;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * @author: Kedar Kashinath Koshti
 * Date:
 * Time:
 * Project Name: notification-service
 * Package Name: com.ecommerce.notification_service
 */
@Service
public class NotificationListener {

    @KafkaListener(topics = "orders-topic", groupId = "notification-group")
    public void listen(String message) {
        System.out.println("Received msg: " + message);
    }

}
