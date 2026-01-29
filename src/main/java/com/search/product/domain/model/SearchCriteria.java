package com.search.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Search Criteria Value Object
 * Encapsulates all possible search parameters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    
    // Text search
    private String query;
    
    // Filters
    private String category;
    private String brand;
    private Set<String> tags;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private Boolean inStockOnly;
    private Boolean activeOnly;
    
    // Sorting
    private String sortBy; // price, rating, name, createdAt
    private SortDirection sortDirection;
    
    // Pagination
    private Integer page;
    private Integer size;
    
    // For cursor-based pagination (more efficient for large datasets)
    private String cursor;
    
    public enum SortDirection {
        ASC, DESC
    }
    
    /**
     * Apply default values
     */
    public void applyDefaults() {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0 || size > 100) {
            size = 20; // Default page size
        }
        if (sortDirection == null) {
            sortDirection = SortDirection.DESC;
        }
        if (activeOnly == null) {
            activeOnly = true;
        }
    }
}
