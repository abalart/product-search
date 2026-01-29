package com.search.product.infrastructure.persistence.mongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MongoDB Product Document
 * Optimized with indexes for efficient querying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
@CompoundIndexes({
    @CompoundIndex(name = "category_active_idx", def = "{'category': 1, 'active': 1}"),
    @CompoundIndex(name = "brand_active_idx", def = "{'brand': 1, 'active': 1}"),
    @CompoundIndex(name = "price_rating_idx", def = "{'price': 1, 'rating': -1}"),
    @CompoundIndex(name = "category_price_idx", def = "{'category': 1, 'price': 1}"),
    @CompoundIndex(name = "active_stock_idx", def = "{'active': 1, 'stock': 1}")
})
public class ProductDocument {
    
    @Id
    private String id;
    
    @TextIndexed(weight = 10)
    private String name;
    
    @TextIndexed(weight = 5)
    private String description;
    
    @Indexed(unique = true)
    private String sku;
    
    @Indexed
    private String brand;
    
    @Indexed
    private String category;
    
    @Indexed
    private Set<String> tags;
    
    @Indexed
    private BigDecimal price;
    
    private String currency;
    
    @Indexed
    private Integer stock;
    
    @Indexed
    private Boolean active;
    
    @Indexed
    private Double rating;
    
    private Integer reviewCount;
    
    private List<String> imageUrls;
    
    private ProductAttributesEmbedded attributes;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAttributesEmbedded {
        private String color;
        private String size;
        private Double weight;
        private String weightUnit;
        private Map<String, String> dimensions;
        private String material;
        private String manufacturer;
        private String countryOfOrigin;
        private Map<String, Object> customAttributes;
    }
}
