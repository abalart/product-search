package com.search.product.application.service;

import com.search.product.domain.model.PageResult;
import com.search.product.domain.model.Product;
import com.search.product.domain.model.SearchCriteria;
import com.search.product.domain.port.outbound.CacheRepository;
import com.search.product.domain.port.outbound.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Product Search Service - Application Layer
 * Orchestrates business logic and coordinates between ports
 * Implements caching strategy and resilience patterns
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {
    
    private final ProductRepository productRepository;
    private final CacheRepository cacheRepository;
    
    private static final String CACHE_PREFIX_PRODUCT = "product:";
    private static final String CACHE_PREFIX_SEARCH = "search:";
    private static final long CACHE_TTL_PRODUCT = 3600; // 1 hour
    private static final long CACHE_TTL_SEARCH = 300; // 5 minutes
    
    /**
     * Find product by ID with caching
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "findByIdFallback")
    public Optional<Product> findById(String id) {
        log.debug("Searching product by id: {}", id);
        
        // Try cache first
        String cacheKey = CACHE_PREFIX_PRODUCT + id;
        Optional<Product> cachedProduct = cacheRepository.get(cacheKey, Product.class);
        
        if (cachedProduct.isPresent()) {
            log.debug("Product found in cache: {}", id);
            return cachedProduct;
        }
        
        // If not in cache, query database
        Optional<Product> product = productRepository.findById(id);
        
        // Cache the result
        product.ifPresent(p -> cacheRepository.put(cacheKey, p, CACHE_TTL_PRODUCT));
        
        return product;
    }
    
    /**
     * Search products with criteria - Main search method
     */
    @CircuitBreaker(name = "productService", fallbackMethod = "searchFallback")
    public PageResult<Product> search(SearchCriteria criteria) {
        log.debug("Searching products with criteria: {}", criteria);
        
        // Apply defaults
        criteria.applyDefaults();
        
        // Generate cache key based on search criteria
        String cacheKey = generateSearchCacheKey(criteria);
        
        // Try cache first for common searches
        if (isCacheable(criteria)) {
            @SuppressWarnings("unchecked")
            Optional<PageResult<Product>> cachedResult = 
                (Optional<PageResult<Product>>) (Optional<?>) cacheRepository.get(cacheKey, PageResult.class);
            if (cachedResult.isPresent()) {
                log.debug("Search result found in cache");
                return cachedResult.get();
            }
        }
        
        // Execute search
        PageResult<Product> result = productRepository.search(criteria);
        
        // Cache result for common searches
        if (isCacheable(criteria)) {
            cacheRepository.put(cacheKey, result, CACHE_TTL_SEARCH);
        }
        
        return result;
    }
    
    /**
     * Autocomplete search for fast suggestions
     */
    public List<Product> autocomplete(String query, int limit) {
        log.debug("Autocomplete search: {}", query);
        
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        return productRepository.autocomplete(query.trim(), Math.min(limit, 10));
    }
    
    /**
     * Find similar products for recommendations
     */
    public List<Product> findSimilar(String productId, int limit) {
        log.debug("Finding similar products for: {}", productId);
        return productRepository.findSimilar(productId, Math.min(limit, 20));
    }
    
    /**
     * Get products by multiple IDs (bulk operation)
     */
    public List<Product> findByIds(List<String> ids) {
        log.debug("Finding products by ids: {}", ids.size());
        return productRepository.findByIds(ids);
    }
    
    /**
     * Save product and invalidate cache
     */
    public Product save(Product product) {
        log.debug("Saving product: {}", product.getSku());
        
        Product saved = productRepository.save(product);
        
        // Invalidate cache
        if (saved.getId() != null) {
            cacheRepository.evict(CACHE_PREFIX_PRODUCT + saved.getId());
        }
        
        return saved;
    }
    
    /**
     * Delete product and invalidate cache
     */
    public void deleteById(String id) {
        log.debug("Deleting product: {}", id);
        productRepository.deleteById(id);
        cacheRepository.evict(CACHE_PREFIX_PRODUCT + id);
    }
    
    // ========== Private Helper Methods ==========
    
    /**
     * Determine if search criteria should be cached
     * Cache only simple, common searches to avoid cache pollution
     */
    private boolean isCacheable(SearchCriteria criteria) {
        // Cache only if:
        // - Simple text search OR category/brand filter
        // - First few pages (0-2)
        // - Standard page size
        return criteria.getPage() <= 2 && 
               criteria.getSize() <= 50 &&
               (criteria.getQuery() != null || 
                criteria.getCategory() != null || 
                criteria.getBrand() != null);
    }
    
    /**
     * Generate consistent cache key from search criteria
     */
    private String generateSearchCacheKey(SearchCriteria criteria) {
        return CACHE_PREFIX_SEARCH + 
               criteria.hashCode();
    }
    
    // ========== Fallback Methods ==========
    
    /**
     * Fallback when circuit breaker is open for findById
     */
    @SuppressWarnings("unused")
    private Optional<Product> findByIdFallback(String id, Exception e) {
        log.error("Circuit breaker activated for findById: {}", id, e);
        return Optional.empty();
    }
    
    /**
     * Fallback when circuit breaker is open for search
     */
    @SuppressWarnings("unused")
    private PageResult<Product> searchFallback(SearchCriteria criteria, Exception e) {
        log.error("Circuit breaker activated for search", e);
        return PageResult.empty(criteria.getPage(), criteria.getSize());
    }
}
