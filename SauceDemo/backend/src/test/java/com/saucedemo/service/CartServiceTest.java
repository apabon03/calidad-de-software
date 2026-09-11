package com.saucedemo.service;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.repository.CartItemRepository;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    @DisplayName("Retorna CartItems por su sessionId")
    void debeRetornarCartItemsPorSesionId() {
        // Arrange
        Product product1 = new Product("sauce-labs-tshirt", "Sauce Labs Bolt T-Shirt", "Get rolling with Sauce Labs.", 15.99, "/img/bolt-shirt-1200.png");
        Product product2 = new Product("sauce-labs-backpack", "Sauce Labs Backpack", "carry.allTheThings() with the sleek, streamlined Sly Pack.", 29.99, "/img/sauce-backpack-1200.png");
        Product product3 = new Product("sauce-labs-fleece-jacket", "Sauce Labs Fleece Jacket", "Its not every day that you put on a jacket.", 49.99, "/img/sauce-pullover-1200.png");

        List<CartItem> cartItems = List.of(
                new CartItem("session-001", product1, 1),
                new CartItem("session-001", product2, 2),
                new CartItem("session-001", product3, 1)
        );

        when(cartItemRepository.findBySessionId("session-001")).thenReturn(cartItems);

        // Act
        var resultado = cartService.getCart("session-001");

        // Assert
        assertThat(resultado)
                .as("No devuelve el numero correcto de items")
                .hasSize(3);

        assertThat(resultado)
                .extracting(CartItem::getSessionId)
                .contains("session-001");

        assertThat(resultado)
                .extracting(CartItem::getProduct)
                .extracting(Product::getName)
                .contains("Sauce Labs Bolt T-Shirt", "Sauce Labs Backpack", "Sauce Labs Fleece Jacket");

        verify(cartItemRepository).findBySessionId("session-001");
    }

    @Test
    @DisplayName("Debe retornar carrito vacío cuando sessionId no existe")
    void retornaCartItemsVacioConSessionIdNoEncontrado() {
        // Arrange
        when(cartItemRepository.findBySessionId(anyString())).thenReturn(List.of());

        // Act
        var resultado = cartService.getCart(anyString());

        // Assert
        assertThat(resultado)
                .as("La lista no esta vacía")
                .isEmpty();

        verify(cartItemRepository).findBySessionId(anyString());
    }
}
