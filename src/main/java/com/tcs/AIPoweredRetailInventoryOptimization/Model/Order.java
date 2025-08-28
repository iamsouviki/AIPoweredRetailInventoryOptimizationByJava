package com.tcs.AIPoweredRetailInventoryOptimization.Model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String orderId;

    private String storeId;
    private String productId;
    private int quantity;

    private String supplierId;

    private Instant orderDate;
    private Instant expectedDeliveryDate;

    private OrderStatus status;

    public enum OrderStatus {
        PENDING, APPROVED, SHIPPED, DELIVERED, CANCELLED
    }
}

