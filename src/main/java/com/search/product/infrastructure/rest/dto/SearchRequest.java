package com.search.product.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Search Request DTO
 * Captures all search parameters from API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product search criteria")
public class SearchRequest {
    
    @Schema(description = "Search query text", example = "laptop gaming")
    private String query;
    
    @Schema(description = "Filter by category", example = "Electronics")
    private String category;
    
    @Schema(description = "Filter by brand", example = "Dell")
    private String brand;
    
    @Schema(description = "Filter by tags")
    private Set<String> tags;
    
    @Schema(description = "Minimum price", example = "500.00")
    private BigDecimal minPrice;
    
    @Schema(description = "Maximum price", example = "2000.00")
    private BigDecimal maxPrice;
    
    @Schema(description = "Minimum rating (0-5)", example = "4.0")
    @Min(0)
    @Max(5)
    private Double minRating;
    
    @Schema(description = "Show only in-stock products", example = "true")
    private Boolean inStockOnly;
    
    @Schema(description = "Show only active products", example = "true")
    private Boolean activeOnly;
    
    @Schema(description = "Sort field: price, rating, name, createdAt", example = "price")
    private String sortBy;
    
    @Schema(description = "Sort direction: ASC or DESC", example = "ASC")
    private String sortDirection;
    
    @Schema(description = "Page number (0-based)", example = "0")
    @Min(0)
    private Integer page;
    
    @Schema(description = "Page size (max 100)", example = "20")
    @Min(1)
    @Max(100)
    private Integer size;
}
