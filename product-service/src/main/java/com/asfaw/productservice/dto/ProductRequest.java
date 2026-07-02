package com.asfaw.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

public class ProductRequest {

    @Data
    public static class CreateProductRequest {
        @NotBlank(message = "Product name is required")
        @Size(max = 200)
        private String name;

        @Size(max = 2000)
        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
        private BigDecimal price;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stockQuantity;

        private String category;
        private String imageUrl;
    }

    @Data
    public static class UpdateProductRequest {
        @Size(max = 200)
        private String name;

        @Size(max = 2000)
        private String description;

        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;

        @Min(value = 0)
        private Integer stockQuantity;

        private String category;
        private String imageUrl;
    }
}
