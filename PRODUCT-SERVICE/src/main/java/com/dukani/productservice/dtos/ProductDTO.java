package com.dukani.productservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductDTO {
    private String name;
    private String code;
    private Long category;
    private String description;
    private BigDecimal price;
    private String currency;
    private String createdBy;
}
