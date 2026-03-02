package com.dukani.productservice.dtos;


import com.dukani.productservice.entities.Products;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductResponse {
    private Long id;
    private String code;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private String currency;
    private boolean active;

    public ProductResponse(Products product) {
        this.id = product.getProductId();
        this.code = product.getProductCode();
        this.name = product.getProductName();
        this.category = product.getCategory().getCategoryName();
        this.description = product.getProductDescription();
        this.price = product.getProductPrice();
        this.currency = product.getProductCurrency();
        this.active = product.getActive();
    }
}
