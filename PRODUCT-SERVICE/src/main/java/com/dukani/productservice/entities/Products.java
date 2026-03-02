package com.dukani.productservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Table(name = "DUKANI_PRODUCTS")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq_gen")
    @SequenceGenerator(name = "product_seq_gen", sequenceName = "DUKANI_PRODUCTS_SEQ", allocationSize = 1)
    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @Column(name = "PRODUCT_CODE")
    private String productCode;

    @Column(name = "PRODUCT_DESCRIPTION")
    private String productDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_CATEGORY")
    private ProductCategories category;

    @Column(name = "PRODUCT_PRICE")
    private BigDecimal productPrice;

    @Column(name = "PRODUCT_CURRENCY")
    private String productCurrency;

    @Column(name = "IS_ACTIVE")
    private Boolean active;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column(name = "UPDATED_BY")
    private String updatedBy;
}
