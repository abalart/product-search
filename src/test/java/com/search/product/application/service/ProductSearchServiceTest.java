package com.search.product.application.service;

import com.search.product.domain.model.PageResult;
import com.search.product.domain.model.Product;
import com.search.product.domain.model.SearchCriteria;
import com.search.product.domain.port.outbound.CacheRepository;
import com.search.product.domain.port.outbound.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductSearchService
 */
@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CacheRepository cacheRepository;
    
    @InjectMocks
    private ProductSearchService searchService;
    
    private Product testProduct;
    
    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("test-id-1")
                .name("Test Product")
                .sku("TEST-SKU-001")
                .brand("TestBrand")
                .category("Electronics")
                .price(new BigDecimal("999.99"))
                .stock(10)
                .active(true)
                .rating(4.5)
                .build();
    }
    
    @Test
    void findById_WhenCacheHit_ShouldReturnFromCache() {
        // Given
        String productId = "test-id-1";
        when(cacheRepository.get(anyString(), eq(Product.class)))
                .thenReturn(Optional.of(testProduct));
        
        // When
        Optional<Product> result = searchService.findById(productId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testProduct);
        verify(cacheRepository).get(anyString(), eq(Product.class));
        verify(productRepository, never()).findById(anyString());
    }
    
    @Test
    void findById_WhenCacheMiss_ShouldQueryDatabaseAndCache() {
        // Given
        String productId = "test-id-1";
        when(cacheRepository.get(anyString(), eq(Product.class)))
                .thenReturn(Optional.empty());
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(testProduct));
        
        // When
        Optional<Product> result = searchService.findById(productId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testProduct);
        verify(cacheRepository).get(anyString(), eq(Product.class));
        verify(productRepository).findById(productId);
        verify(cacheRepository).put(anyString(), eq(testProduct), anyLong());
    }
    
    @Test
    void search_WithValidCriteria_ShouldReturnPageResult() {
        // Given
        SearchCriteria criteria = SearchCriteria.builder()
                .query("laptop")
                .page(0)
                .size(20)
                .build();
        
        PageResult<Product> expectedResult = PageResult.<Product>builder()
                .content(List.of(testProduct))
                .totalElements(1L)
                .totalPages(1)
                .currentPage(0)
                .pageSize(20)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        
        when(cacheRepository.get(anyString(), eq(PageResult.class)))
                .thenReturn(Optional.empty());
        when(productRepository.search(any(SearchCriteria.class)))
                .thenReturn(expectedResult);
        
        // When
        PageResult<Product> result = searchService.search(criteria);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        verify(productRepository).search(any(SearchCriteria.class));
    }
    
    @Test
    void autocomplete_WithValidQuery_ShouldReturnResults() {
        // Given
        String query = "lap";
        int limit = 10;
        when(productRepository.autocomplete(query, limit))
                .thenReturn(List.of(testProduct));
        
        // When
        List<Product> results = searchService.autocomplete(query, limit);
        
        // Then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isEqualTo(testProduct);
        verify(productRepository).autocomplete(query, limit);
    }
    
    @Test
    void autocomplete_WithEmptyQuery_ShouldReturnEmptyList() {
        // When
        List<Product> results = searchService.autocomplete("", 10);
        
        // Then
        assertThat(results).isEmpty();
        verify(productRepository, never()).autocomplete(anyString(), anyInt());
    }
    
    @Test
    void save_ShouldInvalidateCache() {
        // Given
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        
        // When
        Product saved = searchService.save(testProduct);
        
        // Then
        assertThat(saved).isEqualTo(testProduct);
        verify(productRepository).save(testProduct);
        verify(cacheRepository).evict(anyString());
    }
    
    @Test
    void deleteById_ShouldInvalidateCache() {
        // Given
        String productId = "test-id-1";
        doNothing().when(productRepository).deleteById(productId);
        doNothing().when(cacheRepository).evict(anyString());
        
        // When
        searchService.deleteById(productId);
        
        // Then
        verify(productRepository).deleteById(productId);
        verify(cacheRepository).evict(anyString());
    }
}
