package com.saucedemo.service;

import com.saucedemo.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    public List<Product> getAllProducts();

    public Optional<Product> getProductById(Long id);
}
