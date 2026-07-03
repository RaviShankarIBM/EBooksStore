package com.ebookstore.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String description;
    private BigDecimal price;
    private String genre;
    private boolean available;
}
