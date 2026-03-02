package com.dukani.productservice.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ProductDTO {

    @NotBlank(message = "Product name is required")
    private String name;

    private String code;
    private Long category;
    private String description;

    @NotNull(message = "Product price is required")
    @Min(1)
    private BigDecimal price;

    private String currency;

    @NotBlank(message = "Created by is required")
    private String createdBy;
}
