package com.ecommerce.order_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Kedar Kashinath Koshti
 * Date:
 * Time:
 * Project Name: order-service
 * Package Name: com.ecommerce.order_service
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Autowired
    private KafkaTemplate<String, String>   kafkaTemplate;

    @PostMapping
    public String createOrder(@RequestBody String orderDetails){
        kafkaTemplate.send("orders-topic", orderDetails);
        return "Order sent to kafka!";
    }
}
