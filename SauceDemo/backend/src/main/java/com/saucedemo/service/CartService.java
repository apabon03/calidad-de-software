package com.saucedemo.service;

import com.saucedemo.model.CartItem;
import java.util.List;

public interface CartService {
    public List<CartItem> getCart(String sessionId);

    public CartItem addToCart(
        String sessionId,
        Long productId,
        Integer quantity
    );

    public CartItem updateQuantity(Long itemId, Integer quantity);

    public void removeItem(Long itemId);
}
