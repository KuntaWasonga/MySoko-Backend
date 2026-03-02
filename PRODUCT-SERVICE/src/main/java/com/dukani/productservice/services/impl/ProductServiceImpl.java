package com.dukani.productservice.services.impl;

import com.dukani.productservice.dtos.ProductDTO;
import com.dukani.productservice.dtos.ProductResponse;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final Clock clock;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductDTO request) {
        ProductCategories category = validateCategoryExists(request.getCategory());

        log.debug("Creating product with code={}, name={}", request.getCode(), request.getName());

        Products newProduct = Products.builder()
                .productName(request.getName())
                .productCode(request.getCode() != null ? request.getCode().toUpperCase() : null)
                .productDescription(request.getDescription())
                .category(category)
                .productPrice(request.getPrice())
                .productCurrency(request.getCurrency())
                .active(Boolean.TRUE)
                .createdBy(request.getCreatedBy())
                .dateCreated(LocalDateTime.now(clock))
                .build();

        Products saved = productRepository.save(newProduct);
        log.info("Product created successfully. id={}, code={}", saved.getProductId(), saved.getProductCode());

        return new ProductResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Products product = validateProductExists(productId);
        return new ProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long id) {
        validateCategoryExists(id);

        return productRepository.findByCategory(id)
                .stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
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
