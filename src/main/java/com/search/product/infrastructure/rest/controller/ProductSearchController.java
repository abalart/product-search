package com.search.product.infrastructure.rest.controller;

import com.search.product.application.service.ProductSearchService;
import com.search.product.domain.model.PageResult;
import com.search.product.domain.model.Product;
import com.search.product.domain.model.SearchCriteria;
import com.search.product.infrastructure.rest.dto.PageResponse;
import com.search.product.infrastructure.rest.dto.ProductResponse;
import com.search.product.infrastructure.rest.dto.SearchRequest;
import com.search.product.infrastructure.rest.mapper.RestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Product Search
 * Provides endpoints for efficient product search operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Search", description = "Ultra-efficient product search API")
public class ProductSearchController {
    
    private final ProductSearchService searchService;
    private final RestMapper mapper;
    
    /**
     * Search products with filters and pagination
     */
    @PostMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Search products",
        description = "Search products with advanced filters, sorting, and pagination. " +
                     "Optimized for high-throughput scenarios with caching and efficient queries."
    )
    @ApiResponse(responseCode = "200", description = "Successful search")
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @Valid @RequestBody SearchRequest request) {
        
        log.info("Search request received: {}", request);
        
        // Convert DTO to domain model
        SearchCriteria criteria = mapper.toCriteria(request);
        
        // Execute search
        PageResult<Product> result = searchService.search(criteria);
        
        // Convert to response DTO
        List<ProductResponse> responseList = mapper.toResponseList(result.getContent());
        PageResponse<ProductResponse> response = mapper.toPageResponse(result, responseList);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get product by ID
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get product by ID", description = "Retrieve a single product by its unique identifier")
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ProductResponse> getById(
            @Parameter(description = "Product ID") 
            @PathVariable String id) {
        
        log.info("Get product by id: {}", id);
        
        return searchService.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Autocomplete for search suggestions
     */
    @GetMapping(value = "/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Autocomplete search",
        description = "Fast autocomplete for search suggestions. Returns up to 10 results."
    )
    @ApiResponse(responseCode = "200", description = "Autocomplete results")
    public ResponseEntity<List<ProductResponse>> autocomplete(
            @Parameter(description = "Search query", example = "laptop")
            @RequestParam String query,
            @Parameter(description = "Max results (default: 10)")
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Autocomplete request: {}", query);
        
        List<Product> products = searchService.autocomplete(query, limit);
        List<ProductResponse> response = mapper.toResponseList(products);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Find similar products
     */
    @GetMapping(value = "/{id}/similar", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Find similar products",
        description = "Find products similar to the given product based on category and tags"
    )
    @ApiResponse(responseCode = "200", description = "Similar products found")
    public ResponseEntity<List<ProductResponse>> findSimilar(
            @Parameter(description = "Product ID")
            @PathVariable String id,
            @Parameter(description = "Max results (default: 10)")
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Find similar products for: {}", id);
        
        List<Product> products = searchService.findSimilar(id, limit);
        List<ProductResponse> response = mapper.toResponseList(products);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Health check", description = "Check if the service is healthy")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\": \"UP\"}");
    }
}
