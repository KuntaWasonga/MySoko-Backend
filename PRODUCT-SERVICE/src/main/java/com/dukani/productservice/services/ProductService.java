package com.dukani.productservice.services;

import com.dukani.productservice.dtos.ProductDTO;
import com.dukani.productservice.entities.Products;

import java.util.List;

public interface ProductService {
    Products createProduct(ProductDTO request);
    Products getProduct(Long productId);

    List<Products> getProductsByCategory(Long id);
}
