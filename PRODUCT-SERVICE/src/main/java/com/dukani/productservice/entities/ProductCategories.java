package com.dukani.productservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "DUKANI_CATEGORIES")
@Getter @Setter
public class ProductCategories {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq_gen")
    @SequenceGenerator(name = "category_seq_gen", sequenceName = "DUKANI_CATEGORY_SEQ", allocationSize = 1)
    @Column(name = "CATEGORY_ID")
    private Long categoryId;

    @Column(name = "PRODUCT_NAME")
    private String categoryName;

    @Column(name = "CATEGORY_DESCRIPTION")
    private String categoryDescription;

    @Column(name = "date_created")
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column(name = "UPDATED_BY")
    private String updatedBy;
}
