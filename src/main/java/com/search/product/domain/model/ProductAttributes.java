package com.search.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Value Object for additional product attributes
 * Flexible structure to accommodate different product types
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributes {
    
    private String color;
    private String size;
    private Double weight;
    private String weightUnit;
    private Map<String, String> dimensions;
    private String material;
    private String manufacturer;
    private String countryOfOrigin;
    private Map<String, Object> customAttributes;
}
