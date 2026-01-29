package com.search.product.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.product.application.service.ProductSearchService;
import com.search.product.domain.model.PageResult;
import com.search.product.domain.model.Product;
import com.search.product.domain.model.SearchCriteria;
import com.search.product.infrastructure.rest.dto.SearchRequest;
import com.search.product.infrastructure.rest.mapper.RestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductSearchController
 */
@WebMvcTest(controllers = ProductSearchController.class)
class ProductSearchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ProductSearchService searchService;
    
    @MockBean
    private RestMapper restMapper;
    
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
    void search_WithValidRequest_ShouldReturnPageResponse() throws Exception {
        // Given
        SearchRequest request = SearchRequest.builder()
                .query("laptop")
                .page(0)
                .size(20)
                .build();
        
        PageResult<Product> pageResult = PageResult.<Product>builder()
                .content(List.of(testProduct))
                .totalElements(1L)
                .totalPages(1)
                .currentPage(0)
                .pageSize(20)
                .build();
        
        when(restMapper.toCriteria(any())).thenReturn(SearchCriteria.builder().build());
        when(searchService.search(any(SearchCriteria.class))).thenReturn(pageResult);
        when(restMapper.toResponseList(anyList())).thenReturn(List.of());
        when(restMapper.toPageResponse(any(), anyList())).thenCallRealMethod();
        
        // When & Then
        mockMvc.perform(post("/api/v1/products/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20));
    }
    
    @Test
    void getById_WhenProductExists_ShouldReturnProduct() throws Exception {
        // Given
        String productId = "test-id-1";
        when(searchService.findById(productId)).thenReturn(Optional.of(testProduct));
        when(restMapper.toResponse(testProduct)).thenCallRealMethod();
        
        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk());
    }
    
    @Test
    void getById_WhenProductNotFound_ShouldReturn404() throws Exception {
        // Given
        String productId = "non-existent-id";
        when(searchService.findById(productId)).thenReturn(Optional.empty());
        
        // When & Then
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void autocomplete_WithValidQuery_ShouldReturnResults() throws Exception {
        // Given
        String query = "lap";
        when(searchService.autocomplete(query, 10)).thenReturn(List.of(testProduct));
        when(restMapper.toResponseList(anyList())).thenReturn(List.of());
        
        // When & Then
        mockMvc.perform(get("/api/v1/products/autocomplete")
                        .param("query", query)
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }
    
    @Test
    void health_ShouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/products/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"status\": \"UP\"}"));
    }
}
