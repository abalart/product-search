package com.search.product.infrastructure.persistence.mongodb.mapper;

import com.search.product.domain.model.Product;
import com.search.product.domain.model.ProductAttributes;
import com.search.product.infrastructure.persistence.mongodb.entity.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct Mapper for efficient conversion between Domain and Document
 * MapStruct generates implementation at compile time (zero reflection overhead)
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    /**
     * Convert Document to Domain Entity
     */
    @Mapping(target = "attributes", source = "attributes")
    Product toDomain(ProductDocument document);
    
    /**
     * Convert Domain Entity to Document
     */
    @Mapping(target = "attributes", source = "attributes")
    ProductDocument toDocument(Product product);
    
    /**
     * Map embedded attributes
     */
    ProductAttributes toDomainAttributes(ProductDocument.ProductAttributesEmbedded embedded);
    
    /**
     * Map embedded attributes
     */
    ProductDocument.ProductAttributesEmbedded toDocumentAttributes(ProductAttributes attributes);
}
