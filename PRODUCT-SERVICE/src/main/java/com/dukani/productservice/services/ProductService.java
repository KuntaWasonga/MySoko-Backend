package com.dukani.productservice.services;

import com.dukani.productservice.dtos.ProductDTO;
import com.dukani.productservice.dtos.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductDTO request);
    ProductResponse getProduct(Long productId);

    List<ProductResponse> getProductsByCategory(Long id);
}
