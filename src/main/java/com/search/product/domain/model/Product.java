package com.search.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Product Domain Entity - Core business object
 * Represents a product in the system following Domain-Driven Design principles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    private String id;
    private String name;
    private String description;
    private String sku;
    private String brand;
    private String category;
    private Set<String> tags;
    private BigDecimal price;
    private String currency;
    private Integer stock;
    private Boolean active;
    private Double rating;
    private Integer reviewCount;
    private List<String> imageUrls;
    private ProductAttributes attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Business method: Check if product is available for purchase
     */
    public boolean isAvailable() {
        return active != null && active && stock != null && stock > 0;
    }
    
    /**
     * Business method: Check if product is in stock
     */
    public boolean isInStock() {
        return stock != null && stock > 0;
    }
    
    /**
     * Business method: Check if product is highly rated
     */
    public boolean isHighlyRated() {
        return rating != null && rating >= 4.0;
    }
}
