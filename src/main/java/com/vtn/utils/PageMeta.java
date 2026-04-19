package com.vtn.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageMeta {
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
