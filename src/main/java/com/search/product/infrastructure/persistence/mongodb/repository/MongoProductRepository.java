package com.search.product.infrastructure.persistence.mongodb.repository;

import com.search.product.infrastructure.persistence.mongodb.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB Repository
 * Provides basic CRUD operations and custom queries
 */
@Repository
public interface MongoProductRepository extends MongoRepository<ProductDocument, String> {
    
    Optional<ProductDocument> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    /**
     * Find by category with pagination
     */
    Page<ProductDocument> findByCategoryAndActiveTrue(String category, Pageable pageable);
    
    /**
     * Find by brand with pagination
     */
    Page<ProductDocument> findByBrandAndActiveTrue(String brand, Pageable pageable);
    
    /**
     * Find by price range
     */
    @Query("{'price': {$gte: ?0, $lte: ?1}, 'active': true}")
    Page<ProductDocument> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    /**
     * Find by tags
     */
    Page<ProductDocument> findByTagsInAndActiveTrue(List<String> tags, Pageable pageable);
    
    /**
     * Find products with minimum rating
     */
    @Query("{'rating': {$gte: ?0}, 'active': true}")
    Page<ProductDocument> findByMinimumRating(Double minRating, Pageable pageable);
    
    /**
     * Find in-stock products
     */
    @Query("{'stock': {$gt: 0}, 'active': true}")
    Page<ProductDocument> findInStockProducts(Pageable pageable);
}
