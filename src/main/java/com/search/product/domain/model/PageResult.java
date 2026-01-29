package com.search.product.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Page Result Value Object
 * Generic paginated response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    private List<T> content;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
    
    // For cursor-based pagination
    private String nextCursor;
    private String previousCursor;
    
    public static <T> PageResult<T> empty(Integer page, Integer size) {
        return PageResult.<T>builder()
                .content(List.of())
                .totalElements(0L)
                .totalPages(0)
                .currentPage(page)
                .pageSize(size)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
