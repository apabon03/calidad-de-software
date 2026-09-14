package com.saucedemo.controller;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartController")
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(
                cartController
        ).build();
    }

    @Nested
    @DisplayName("GET /api/cart")
    class GetCart {

        @Test
        @DisplayName("Retorna 200 con los items del carrito")
        void retorna200ConLosItemsDelCarrito() throws Exception {
            Product product1 = new Product("sauce-labs-tshirt", "Sauce Labs Bolt T-Shirt", "Get rolling with Sauce Labs.", 15.99, "/img/bolt-shirt-1200.png");
            Product product2 = new Product("sauce-labs-backpack", "Sauce Labs Backpack", "carry.allTheThings() with the sleek, streamlined Sly Pack.", 29.99, "/img/sauce-backpack-1200.png");
            Product product3 = new Product("sauce-labs-fleece-jacket", "Sauce Labs Fleece Jacket", "Its not every day that you put on a jacket.", 49.99, "/img/sauce-pullover-1200.png");

            List<CartItem> cartItems = List.of(
                    new CartItem("session-001", product1, 1),
                    new CartItem("session-001", product2, 2),
                    new CartItem("session-001", product3, 1)
            );

            when(cartService.getCart("session-001")).thenReturn(cartItems);

            mockMvc
                    .perform(
                            get("/api/cart")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .param("sessionId", "session-001")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))

                    .andExpect(jsonPath("$[0].sessionId").value("session-001"))
                    .andExpect(jsonPath("$[0].product.code").value("sauce-labs-tshirt"))
                    .andExpect(jsonPath("$[0].product.name").value("Sauce Labs Bolt T-Shirt"))
                    .andExpect(jsonPath("$[0].product.description").value("Get rolling with Sauce Labs."))
                    .andExpect(jsonPath("$[0].product.price").value(15.99))
                    .andExpect(jsonPath("$[0].product.imageUrl").value("/img/bolt-shirt-1200.png"))
                    .andExpect(jsonPath("$[0].quantity").value(1))

                    .andExpect(jsonPath("$[1].sessionId").value("session-001"))
                    .andExpect(jsonPath("$[1].product.code").value("sauce-labs-backpack"))
                    .andExpect(jsonPath("$[1].product.name").value("Sauce Labs Backpack"))
                    .andExpect(jsonPath("$[1].product.description").value("carry.allTheThings() with the sleek, streamlined Sly Pack."))
                    .andExpect(jsonPath("$[1].product.price").value(29.99))
                    .andExpect(jsonPath("$[1].product.imageUrl").value("/img/sauce-backpack-1200.png"))
                    .andExpect(jsonPath("$[1].quantity").value(2))

                    .andExpect(jsonPath("$[2].sessionId").value("session-001"))
                    .andExpect(jsonPath("$[2].product.code").value("sauce-labs-fleece-jacket"))
                    .andExpect(jsonPath("$[2].product.name").value("Sauce Labs Fleece Jacket"))
                    .andExpect(jsonPath("$[2].product.description").value("Its not every day that you put on a jacket."))
                    .andExpect(jsonPath("$[2].product.price").value(49.99))
                    .andExpect(jsonPath("$[2].product.imageUrl").value("/img/sauce-pullover-1200.png"))
                    .andExpect(jsonPath("$[2].quantity").value(1));

            verify(cartService).getCart("session-001");
        }

        @Test
        @DisplayName("Retorna 200 con un arreglo vacío cuando la sesión no tiene items")
        void retorna200ConArregloVacio() throws Exception {
            when(cartService.getCart("session-001")).thenReturn(List.of());

            mockMvc
                    .perform(
                            get("/api/cart")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .param("sessionId", "session-001")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(cartService).getCart("session-001");
        }

        @Test
        @DisplayName("Retorna 400 cuando falta el parámetro sessionId")
        void retorna400CuandoFaltaElSessionId() throws Exception {
            mockMvc
                    .perform(
                            get("/api/cart")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/cart")
    class AddToCart {

        @Test
        @DisplayName("Retorna 200 con el item agregado")
        void retorna200ConElItemAgregado() throws Exception {
            Product product = new Product("sauce-labs-backpack", "Sauce Labs Backpack", "Description", 29.99, "/img/backpack.png");
            CartItem cartItem = new CartItem("session-001", product, 2);

            when(cartService.addToCart("session-001", 1L, 2)).thenReturn(cartItem);

            mockMvc
                    .perform(
                            post("/api/cart")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"sessionId\":\"session-001\",\"productId\":1,\"quantity\":2}")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionId").value("session-001"))
                    .andExpect(jsonPath("$.product.code").value("sauce-labs-backpack"))
                    .andExpect(jsonPath("$.quantity").value(2));

            verify(cartService).addToCart("session-001", 1L, 2);
        }

        @Test
        @DisplayName("Retorna 404 cuando el producto no existe")
        void retorna404CuandoElProductoNoExiste() throws Exception {
            when(cartService.addToCart("session-001", 99L, 1))
                    .thenThrow(new NoSuchElementException("Producto no encontrado: 99"));

            mockMvc
                    .perform(
                            post("/api/cart")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"sessionId\":\"session-001\",\"productId\":99,\"quantity\":1}")
                    )
                    .andExpect(status().isNotFound());

            verify(cartService).addToCart("session-001", 99L, 1);
        }
    }

    @Nested
    @DisplayName("DELETE /api/cart/{itemId}")
    class RemoveItem {

        @Test
        @DisplayName("Retorna 204 sin cuerpo tras eliminar el item")
        void retorna204SinCuerpo() throws Exception {
            mockMvc
                    .perform(
                            delete("/api/cart/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(cartService).removeItem(1L);
        }

        @Test
        @DisplayName("Retorna 204 aunque el item no exista")
        void retorna204AunqueElItemNoExista() throws Exception {
            // Contrato actual, distinto al de PUT: removeItem se apoya en deleteById, que no
            // falla con un id inexistente, así que el endpoint no distingue "lo eliminé" de
            // "nunca existió". Si algún día se decide responder 404, esta prueba debe fallar.
            mockMvc
                    .perform(
                            delete("/api/cart/999")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isNoContent());

            verify(cartService).removeItem(999L);
        }

        @Test
        @DisplayName("Retorna 400 cuando el itemId no es numérico")
        void retorna400CuandoElItemIdNoEsNumerico() throws Exception {
            mockMvc
                    .perform(
                            delete("/api/cart/abc")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(cartService);
        }
    }

    @Nested
    @DisplayName("PUT /api/cart/{itemId}")
    class UpdateQuantity {

        @Test
        @DisplayName("Retorna 200 con el item actualizado")
        void retorna200ConElItemActualizado() throws Exception {

            // Arrange
            CartItem cartItem = new CartItem();
            cartItem.setQuantity(5);

            when(cartService.updateQuantity(1L, 5))
                    .thenReturn(cartItem);

            // Act + Assert
            mockMvc
                    .perform(
                            put("/api/cart/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"quantity\":5}")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(5));

            verify(cartService).updateQuantity(1L, 5);
        }

        @Test
        @DisplayName("Retorna 404 cuando el item no existe")
        void retorna404CuandoElItemNoExiste() throws Exception {


            when(cartService.updateQuantity(1L, 5))
                    .thenThrow(
                            new NoSuchElementException(
                                    "Item de carrito no encontrado: 1"
                            )
                    );


            mockMvc
                    .perform(
                            put("/api/cart/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"quantity\":5}")
                    )
                    .andExpect(status().isNotFound());

            verify(cartService).updateQuantity(1L, 5);
        }
    }
}

