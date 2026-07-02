package com.asfaw.orderservice.dto;

import com.asfaw.orderservice.entity.Order;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class OrderRequest {

    @Data
    public static class CreateOrderRequest {
        @NotNull(message = "User ID is required")
        private Long userId;

        @NotEmpty(message = "Order must contain at least one item")
        private List<OrderItemRequest> items;

        private String shippingAddress;
        private String orderNotes;
    }

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long productId;

        @NotBlank
        private String productName;

        @NotNull
        @Min(1)
        private Integer quantity;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal unitPrice;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull
        private Order.OrderStatus status;
    }
}
