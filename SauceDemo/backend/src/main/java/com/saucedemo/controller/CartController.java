package com.saucedemo.controller;

import com.saucedemo.dto.AddToCartRequest;
import com.saucedemo.dto.UpdateCartItemRequest;
import com.saucedemo.model.CartItem;
import com.saucedemo.service.CartService;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItem> getCart(@RequestParam String sessionId) {
        return cartService.getCart(sessionId);
    }

    @PostMapping
    public ResponseEntity<CartItem> addToCart(
        @RequestBody AddToCartRequest request
    ) {
        try {
            CartItem item = cartService.addToCart(
                request.getSessionId(),
                request.getProductId(),
                request.getQuantity()
            );
            return ResponseEntity.ok(item);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartItem> updateQuantity(
        @PathVariable Long itemId,
        @RequestBody UpdateCartItemRequest request
    ) {
        try {
            CartItem item = cartService.updateQuantity(
                itemId,
                request.getQuantity()
            );
            return ResponseEntity.ok(item);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartService.removeItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
