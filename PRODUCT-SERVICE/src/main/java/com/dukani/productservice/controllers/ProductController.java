package com.dukani.productservice.controllers;

import com.dukani.productservice.dtos.ProductDTO;
import com.dukani.productservice.dtos.ProductResponse;
import com.dukani.productservice.dtos.StandardResponse;
import com.dukani.productservice.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*", maxAge = 3600)
@Log4j2
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<StandardResponse<?>> createProduct(@Valid @RequestBody ProductDTO data) {
        ProductResponse result = productService.createProduct(data);

        return new ResponseEntity<> (
            StandardResponse.builder()
                    .data(result.getId())
                    .message("Product created successfully")
                    .build(), HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<?>> getProduct(@PathVariable Long id) {
        ProductResponse result = productService.getProduct(id);

        return new ResponseEntity<> (
            StandardResponse.builder()
                    .data(result)
                    .message("Product retrieved successfully")
                    .build(), HttpStatus.OK
        );
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<StandardResponse<?>> getProductsByCategory(@PathVariable Long id) {
        List<ProductResponse> result = productService.getProductsByCategory(id);

        return new ResponseEntity<> (
                StandardResponse.builder()
                        .data(result)
                        .message("Products retrieved successfully")
                        .build(), HttpStatus.OK
        );
    }
}
