package com.dukani.productservice.repositories;

import com.dukani.productservice.entities.ProductCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategories, Long> {
    Optional<ProductCategories> findByCategoryId(Long categoryId);

}
