package com.saucedemo.service;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.repository.CartItemRepository;
import com.saucedemo.repository.ProductRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartServiceImpl(
        CartItemRepository cartItemRepository,
        ProductRepository productRepository
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartItem> getCart(String sessionId) {
        return cartItemRepository.findBySessionId(sessionId);
    }

    @Override
    public CartItem addToCart(
        String sessionId,
        Long productId,
        Integer quantity
    ) {
        Product product = productRepository
            .findById(productId)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Producto no encontrado: " + productId
                )
            );

        return cartItemRepository
            .findBySessionIdAndProductId(sessionId, productId)
            .map(existingItem -> {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                return cartItemRepository.save(existingItem);
            })
            .orElseGet(() ->
                cartItemRepository.save(
                    new CartItem(sessionId, product, quantity)
                )
            );
    }

    @Override
    public CartItem updateQuantity(Long itemId, Integer quantity) {
        CartItem item = cartItemRepository
            .findById(itemId)
            .orElseThrow(() ->
                new NoSuchElementException(
                    "Item de carrito no encontrado: " + itemId
                )
            );
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    public void removeItem(Long itemId) {
        cartItemRepository.deleteById(itemId);
    }
}
