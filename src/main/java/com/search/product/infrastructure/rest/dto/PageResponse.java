package com.search.product.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated Response DTO
 * Generic wrapper for paginated results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Paginated response")
public class PageResponse<T> {
    
    @Schema(description = "Page content")
    private List<T> content;
    
    @Schema(description = "Total number of elements", example = "1500")
    private Long totalElements;
    
    @Schema(description = "Total number of pages", example = "75")
    private Integer totalPages;
    
    @Schema(description = "Current page number", example = "0")
    private Integer currentPage;
    
    @Schema(description = "Page size", example = "20")
    private Integer pageSize;
    
    @Schema(description = "Has next page")
    private Boolean hasNext;
    
    @Schema(description = "Has previous page")
    private Boolean hasPrevious;
}
