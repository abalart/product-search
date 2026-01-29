package com.search.product.domain.port.outbound;

import com.search.product.domain.model.PageResult;
import com.search.product.domain.model.Product;
import com.search.product.domain.model.SearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Port (Interface) for Product Repository
 * Following Hexagonal Architecture - this is the outbound port
 * Infrastructure layer will implement this interface
 */
public interface ProductRepository {
    
    /**
     * Find product by ID
     */
    Optional<Product> findById(String id);
    
    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);
    
    /**
     * Search products with criteria and pagination
     * This is the main search method optimized for performance
     */
    PageResult<Product> search(SearchCriteria criteria);
    
    /**
     * Full-text search with autocomplete support
     */
    List<Product> autocomplete(String query, int limit);
    
    /**
     * Find products by IDs (bulk operation)
     */
    List<Product> findByIds(List<String> ids);
    
    /**
     * Find similar products (for recommendations)
     */
    List<Product> findSimilar(String productId, int limit);
    
    /**
     * Save or update product
     */
    Product save(Product product);
    
    /**
     * Save multiple products (bulk operation)
     */
    List<Product> saveAll(List<Product> products);
    
    /**
     * Delete product by ID
     */
    void deleteById(String id);
    
    /**
     * Count products matching criteria
     */
    Long count(SearchCriteria criteria);
    
    /**
     * Check if product exists by SKU
     */
    boolean existsBySku(String sku);
}
