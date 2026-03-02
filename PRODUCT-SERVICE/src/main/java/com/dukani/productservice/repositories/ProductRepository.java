package com.dukani.productservice.repositories;

import com.dukani.productservice.entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {
    Optional<Products> findByProductId(Long productId);

    Products findByProductNameIgnoreCase(String name);
    Products findByProductCodeIgnoreCase(String code);

    @Query("""
           SELECT p FROM Products p
           WHERE p.category.categoryId = :categoryId
        """)
    List<Products> findByCategory(@Param("categoryId")Long categoryId);
}
