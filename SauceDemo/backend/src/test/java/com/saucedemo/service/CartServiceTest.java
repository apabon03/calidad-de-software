package com.saucedemo.service;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.repository.CartItemRepository;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl")
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("Devuelve los items asociados al sessionId")
        void devuelveLosItemsDeLaSesion() {
            Product product1 = new Product("sauce-labs-tshirt", "Sauce Labs Bolt T-Shirt", "Get rolling with Sauce Labs.", 15.99, "/img/bolt-shirt-1200.png");
            Product product2 = new Product("sauce-labs-backpack", "Sauce Labs Backpack", "carry.allTheThings() with the sleek, streamlined Sly Pack.", 29.99, "/img/sauce-backpack-1200.png");
            Product product3 = new Product("sauce-labs-fleece-jacket", "Sauce Labs Fleece Jacket", "Its not every day that you put on a jacket.", 49.99, "/img/sauce-pullover-1200.png");

            List<CartItem> cartItems = List.of(
                    new CartItem("session-001", product1, 1),
                    new CartItem("session-001", product2, 2),
                    new CartItem("session-001", product3, 1)
            );

            when(cartItemRepository.findBySessionId("session-001")).thenReturn(cartItems);

            var resultado = cartService.getCart("session-001");

            assertThat(resultado)
                    .as("items del carrito de session-001")
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
        @DisplayName("Devuelve una lista vacía cuando la sesión no tiene items")
        void devuelveListaVaciaCuandoLaSesionNoTieneItems() {
            when(cartItemRepository.findBySessionId("session-inexistente")).thenReturn(List.of());

            var resultado = cartService.getCart("session-inexistente");

            assertThat(resultado)
                    .as("carrito de una sesión sin items")
                    .isEmpty();

            verify(cartItemRepository).findBySessionId("session-inexistente");
        }
    }

    @Nested
    @DisplayName("addToCart")
    class AddToCart {

        @Test
        @DisplayName("Crea un item nuevo cuando el producto aún no está en el carrito")
        void creaItemNuevoCuandoElProductoNoEstaEnElCarrito() {
            Product product = new Product("sauce-labs-backpack", "Sauce Labs Backpack", "Description", 29.99, "/img/backpack.png");
            product.setId(1L);
            CartItem savedItem = new CartItem("session-001", product, 2);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId("session-001", 1L)).thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

            CartItem resultado = cartService.addToCart("session-001", 1L, 2);

            assertThat(resultado).isSameAs(savedItem);
            verify(cartItemRepository).save(argThat(item ->
                    item.getSessionId().equals("session-001")
                            && item.getProduct() == product
                            && item.getQuantity().equals(2)));
        }

        @Test
        @DisplayName("Suma la cantidad cuando el producto ya está en el carrito")
        void sumaLaCantidadCuandoElProductoYaEstaEnElCarrito() {
            Product product = new Product("sauce-labs-backpack", "Sauce Labs Backpack", "Description", 29.99, "/img/backpack.png");
            product.setId(1L);
            CartItem existingItem = new CartItem("session-001", product, 2);

            when(productRepository.findById(1L)).thenReturn(Optional.of(product));
            when(cartItemRepository.findBySessionIdAndProductId("session-001", 1L)).thenReturn(Optional.of(existingItem));
            when(cartItemRepository.save(existingItem)).thenReturn(existingItem);

            CartItem resultado = cartService.addToCart("session-001", 1L, 3);

            assertThat(resultado).isSameAs(existingItem);
            assertThat(existingItem.getQuantity())
                    .as("cantidad acumulada: 2 existentes + 3 agregados")
                    .isEqualTo(5);
            verify(cartItemRepository).save(existingItem);
        }

        @Test
        @DisplayName("Lanza NoSuchElementException cuando el producto no existe")
        void lanzaExcepcionCuandoElProductoNoExiste() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addToCart("session-001", 99L, 1))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("Producto no encontrado: 99");

            verifyNoInteractions(cartItemRepository);
        }
    }

}
