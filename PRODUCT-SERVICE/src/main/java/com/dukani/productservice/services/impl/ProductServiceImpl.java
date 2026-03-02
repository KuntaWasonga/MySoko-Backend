package com.dukani.productservice.services.impl;

import com.dukani.productservice.dtos.ProductDTO;
import com.dukani.productservice.entities.ProductCategories;
import com.dukani.productservice.entities.Products;
import com.dukani.productservice.exceptions.ItemNotFoundException;
import com.dukani.productservice.repositories.ProductCategoryRepository;
import com.dukani.productservice.repositories.ProductRepository;
import com.dukani.productservice.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @Override
    public Products createProduct(ProductDTO request) {
        validateUniqueName(request.getName());
        validateUniqueProductCode(request.getName());
        ProductCategories category = validateCategoryExists(request.getCategory());

        Products newProduct = Products.builder()
                .productName(request.getName())
                .productCode(request.getCode().toUpperCase())
                .productDescription(request.getDescription())
                .category(category)
                .productPrice(request.getPrice())
                .productPrice(request.getPrice())
                .active(Boolean.TRUE)
                .createdBy(request.getCreatedBy())
                .dateCreated(LocalDateTime.now())
                .build();

        return productRepository.save(newProduct);
    }

    @Override
    public Products getProduct(Long productId) {
        return validateProductExists(productId);
    }

    @Override
    public List<Products> getProductsByCategory(Long id) {
        validateCategoryExists(id);

        return productRepository.findByCategory(id);
    }

    private void validateUniqueName(String name){
        Products existing = productRepository.findByProductNameIgnoreCase(name);

        if (existing != null) {
            throw new IllegalArgumentException("Product already exists with name " + name);
        }
    }

    private void validateUniqueProductCode(String code){
        Products existing = productRepository.findByProductCodeIgnoreCase(code);

        if (existing != null) {
            throw new IllegalArgumentException("Product already exists with code: " + code);
        }
    }

    private ProductCategories validateCategoryExists(Long categoryId){
        return categoryRepository.findByCategoryId(categoryId)
            .orElseThrow(() -> new ItemNotFoundException("Category with id: " + categoryId + " does not exist"));
    }

    private Products validateProductExists(Long productId){
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new ItemNotFoundException("Product with id: " + productId + " does not exist"));
    }
}
