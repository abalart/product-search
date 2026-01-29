package com.search.product.infrastructure.persistence.mongodb;

import com.search.product.domain.model.PageResult;
import com.search.product.domain.model.Product;
import com.search.product.domain.model.SearchCriteria;
import com.search.product.domain.port.outbound.ProductRepository;
import com.search.product.infrastructure.persistence.mongodb.entity.ProductDocument;
import com.search.product.infrastructure.persistence.mongodb.mapper.ProductMapper;
import com.search.product.infrastructure.persistence.mongodb.repository.MongoProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB Implementation of ProductRepository
 * Uses MongoTemplate for complex queries and optimizations
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MongoProductRepositoryAdapter implements ProductRepository {

    private final MongoProductRepository mongoRepository;
    private final MongoTemplate mongoTemplate;
    private final ProductMapper mapper;

    @Override
    public Optional<Product> findById(String id) {
        return mongoRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return mongoRepository.findBySku(sku)
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<Product> search(SearchCriteria criteria) {
        log.debug("Executing optimized search with criteria: {}", criteria);

        // Build dynamic query
        Query query = buildSearchQuery(criteria);

        // Apply sorting
        query.with(buildSort(criteria));

        // Count total (only if needed for pagination)
        long total = mongoTemplate.count(query, ProductDocument.class);

        // Apply pagination with projection for memory efficiency
        query.skip((long) criteria.getPage() * criteria.getSize());
        query.limit(criteria.getSize());

        // Execute query with field projection (load only necessary fields)
        List<ProductDocument> documents = mongoTemplate.find(query, ProductDocument.class);

        // Map to domain
        List<Product> products = documents.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        // Build page result
        return buildPageResult(products, total, criteria);
    }

    @Override
    public List<Product> autocomplete(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        // Use text index for full-text search
        TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                .matchingAny(query.split("\\s+"));

        Query searchQuery = TextQuery.queryText(textCriteria)
                .sortByScore()
                .with(PageRequest.of(0, limit));

        searchQuery.addCriteria(Criteria.where("active").is(true));

        // Project only necessary fields for autocomplete
        searchQuery.fields()
                .include("id", "name", "sku", "price", "imageUrls");

        List<ProductDocument> documents = mongoTemplate.find(searchQuery, ProductDocument.class);

        return documents.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByIds(List<String> ids) {
        List<ProductDocument> documents = mongoRepository.findAllById(ids);
        return documents.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findSimilar(String productId, int limit) {
        Optional<ProductDocument> productOpt = mongoRepository.findById(productId);

        if (productOpt.isEmpty()) {
            return List.of();
        }

        ProductDocument product = productOpt.get();

        // Find similar products based on category and tags
        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();

        if (product.getCategory() != null) {
            criteriaList.add(Criteria.where("category").is(product.getCategory()));
        }

        if (product.getTags() != null && !product.getTags().isEmpty()) {
            criteriaList.add(Criteria.where("tags").in(product.getTags()));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    criteriaList.toArray(new Criteria[0])));
        }

        query.addCriteria(Criteria.where("id").ne(productId));
        query.addCriteria(Criteria.where("active").is(true));
        query.with(Sort.by(Sort.Direction.DESC, "rating"));
        query.limit(limit);

        List<ProductDocument> documents = mongoTemplate.find(query, ProductDocument.class);

        return documents.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Product save(Product product) {
        ProductDocument document = mapper.toDocument(product);
        ProductDocument saved = mongoRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Product> saveAll(List<Product> products) {
        List<ProductDocument> documents = products.stream()
                .map(mapper::toDocument)
                .collect(Collectors.toList());

        List<ProductDocument> saved = mongoRepository.saveAll(documents);

        return saved.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    @Override
    public Long count(SearchCriteria criteria) {
        Query query = buildSearchQuery(criteria);
        return mongoTemplate.count(query, ProductDocument.class);
    }

    @Override
    public boolean existsBySku(String sku) {
        return mongoRepository.existsBySku(sku);
    }

    // ========== Private Helper Methods ==========

    /**
     * Build dynamic MongoDB query from search criteria
     * Optimized to use indexes efficiently
     */
    private Query buildSearchQuery(SearchCriteria criteria) {
        Query query = new Query();

        List<Criteria> criteriaList = new ArrayList<>();

        // Text search (uses text index)
        if (criteria.getQuery() != null && !criteria.getQuery().trim().isEmpty()) {
            TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                    .matching(criteria.getQuery().trim());
            query.addCriteria(textCriteria);
        }

        // Category filter (indexed)
        if (criteria.getCategory() != null) {
            criteriaList.add(Criteria.where("category").is(criteria.getCategory()));
        }

        // Brand filter (indexed)
        if (criteria.getBrand() != null) {
            criteriaList.add(Criteria.where("brand").is(criteria.getBrand()));
        }

        // Tags filter (indexed)
        if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
            criteriaList.add(Criteria.where("tags").in(criteria.getTags()));
        }

        // Price range filter (compound indexed with rating)
        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            criteriaList.add(Criteria.where("price")
                    .gte(criteria.getMinPrice())
                    .lte(criteria.getMaxPrice()));
        } else if (criteria.getMinPrice() != null) {
            criteriaList.add(Criteria.where("price").gte(criteria.getMinPrice()));
        } else if (criteria.getMaxPrice() != null) {
            criteriaList.add(Criteria.where("price").lte(criteria.getMaxPrice()));
        }

        // Rating filter (indexed)
        if (criteria.getMinRating() != null) {
            criteriaList.add(Criteria.where("rating").gte(criteria.getMinRating()));
        }

        // Stock filter (compound indexed with active)
        if (criteria.getInStockOnly() != null && criteria.getInStockOnly()) {
            criteriaList.add(Criteria.where("stock").gt(0));
        }

        // Active filter (indexed in multiple compounds)
        if (criteria.getActiveOnly() != null && criteria.getActiveOnly()) {
            criteriaList.add(Criteria.where("active").is(true));
        }

        // Combine all criteria
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(
                    criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    /**
     * Build sort from criteria
     */
    private Sort buildSort(SearchCriteria criteria) {
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "createdAt";
        Sort.Direction direction = criteria.getSortDirection() == SearchCriteria.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }

    /**
     * Build paginated result
     */
    private PageResult<Product> buildPageResult(List<Product> products, long total, SearchCriteria criteria) {
        int totalPages = (int) Math.ceil((double) total / criteria.getSize());

        return PageResult.<Product>builder()
                .content(products)
                .totalElements(total)
                .totalPages(totalPages)
                .currentPage(criteria.getPage())
                .pageSize(criteria.getSize())
                .hasNext(criteria.getPage() < totalPages - 1)
                .hasPrevious(criteria.getPage() > 0)
                .build();
    }
}
