package com.search.product.infrastructure.rest.mapper;

import com.search.product.domain.model.PageResult;
import com.search.product.domain.model.Product;
import com.search.product.domain.model.SearchCriteria;
import com.search.product.infrastructure.rest.dto.PageResponse;
import com.search.product.infrastructure.rest.dto.ProductResponse;
import com.search.product.infrastructure.rest.dto.SearchRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Mapper for DTOs
 */
@Mapper(componentModel = "spring")
public interface RestMapper {
    
    /**
     * Convert Product to ProductResponse
     */
    @Mapping(target = "attributes", expression = "java(flattenAttributes(product))")
    ProductResponse toResponse(Product product);
    
    /**
     * Convert list of Products to ProductResponses
     */
    List<ProductResponse> toResponseList(List<Product> products);
    
    /**
     * Convert SearchRequest to SearchCriteria
     */
    @Mapping(target = "sortDirection", expression = "java(mapSortDirection(request.getSortDirection()))")
    @Mapping(target = "cursor", ignore = true)
    SearchCriteria toCriteria(SearchRequest request);
    
    /**
     * Convert PageResult to PageResponse
     */
    default <T, R> PageResponse<R> toPageResponse(PageResult<T> pageResult, List<R> content) {
        return PageResponse.<R>builder()
                .content(content)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .currentPage(pageResult.getCurrentPage())
                .pageSize(pageResult.getPageSize())
                .hasNext(pageResult.getHasNext())
                .hasPrevious(pageResult.getHasPrevious())
                .build();
    }
    
    /**
     * Flatten product attributes for simpler API response
     */
    default Map<String, Object> flattenAttributes(Product product) {
        if (product.getAttributes() == null) {
            return null;
        }
        
        Map<String, Object> flattened = new HashMap<>();
        var attrs = product.getAttributes();
        
        if (attrs.getColor() != null) flattened.put("color", attrs.getColor());
        if (attrs.getSize() != null) flattened.put("size", attrs.getSize());
        if (attrs.getWeight() != null) flattened.put("weight", attrs.getWeight());
        if (attrs.getWeightUnit() != null) flattened.put("weightUnit", attrs.getWeightUnit());
        if (attrs.getDimensions() != null) flattened.put("dimensions", attrs.getDimensions());
        if (attrs.getMaterial() != null) flattened.put("material", attrs.getMaterial());
        if (attrs.getManufacturer() != null) flattened.put("manufacturer", attrs.getManufacturer());
        if (attrs.getCountryOfOrigin() != null) flattened.put("countryOfOrigin", attrs.getCountryOfOrigin());
        if (attrs.getCustomAttributes() != null) flattened.putAll(attrs.getCustomAttributes());
        
        return flattened.isEmpty() ? null : flattened;
    }
    
    /**
     * Map sort direction string to enum
     */
    default SearchCriteria.SortDirection mapSortDirection(String direction) {
        if (direction == null) {
            return SearchCriteria.SortDirection.DESC;
        }
        
        return direction.equalsIgnoreCase("ASC") 
                ? SearchCriteria.SortDirection.ASC 
                : SearchCriteria.SortDirection.DESC;
    }
}
