package com.search.product.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Product Response DTO
 * Optimized for API responses with minimal data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product information")
public class ProductResponse {
    
    @Schema(description = "Product unique identifier", example = "60d5ec49f1b2c8b1f8e4e1a1")
    private String id;
    
    @Schema(description = "Product name", example = "Laptop Dell XPS 15")
    private String name;
    
    @Schema(description = "Product description")
    private String description;
    
    @Schema(description = "Stock Keeping Unit", example = "DELL-XPS15-2024")
    private String sku;
    
    @Schema(description = "Brand name", example = "Dell")
    private String brand;
    
    @Schema(description = "Product category", example = "Electronics")
    private String category;
    
    @Schema(description = "Product tags for search")
    private Set<String> tags;
    
    @Schema(description = "Product price", example = "1299.99")
    private BigDecimal price;
    
    @Schema(description = "Currency code", example = "USD")
    private String currency;
    
    @Schema(description = "Available stock quantity", example = "45")
    private Integer stock;
    
    @Schema(description = "Product active status")
    private Boolean active;
    
    @Schema(description = "Average rating", example = "4.5")
    private Double rating;
    
    @Schema(description = "Number of reviews", example = "128")
    private Integer reviewCount;
    
    @Schema(description = "Product image URLs")
    private List<String> imageUrls;
    
    @Schema(description = "Additional product attributes")
    private Map<String, Object> attributes;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
