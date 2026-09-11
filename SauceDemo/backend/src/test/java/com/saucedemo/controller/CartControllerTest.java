package com.saucedemo.controller;

import com.saucedemo.model.CartItem;
import com.saucedemo.model.Product;
import com.saucedemo.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Inicializa MockMvc registrando manualmente el controlador
        this.mockMvc = MockMvcBuilders.standaloneSetup(
                cartController
        ).build();
    }

    @Test
    @DisplayName("Debe retornar 200 con la lista de CartItems")
    void devuelveCartItemsExitosamente() throws Exception {
        // Arrange
        Product product1 = new Product("sauce-labs-tshirt", "Sauce Labs Bolt T-Shirt", "Get rolling with Sauce Labs.", 15.99, "/img/bolt-shirt-1200.png");
        Product product2 = new Product("sauce-labs-backpack", "Sauce Labs Backpack", "carry.allTheThings() with the sleek, streamlined Sly Pack.", 29.99, "/img/sauce-backpack-1200.png");
        Product product3 = new Product("sauce-labs-fleece-jacket", "Sauce Labs Fleece Jacket", "Its not every day that you put on a jacket.", 49.99, "/img/sauce-pullover-1200.png");

        List<CartItem> cartItems = List.of(
                new CartItem("session-001", product1, 1),
                new CartItem("session-001", product2, 2),
                new CartItem("session-001", product3, 1)
        );

        when(cartService.getCart("session-001")).thenReturn(cartItems);

        // Act && Assert
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
    @DisplayName("Debe retornar 200 con lista vacía por sessionId no encontrado")
    void devuelveListaVacia() throws Exception{
        // Arrange
        when(cartService.getCart("session-001")).thenReturn(List.of());

        // Act && Assert
        mockMvc
                .perform(
                        get("/api/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("sessionId","session-001")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(cartService).getCart("session-001");
    }

    @Test
    @DisplayName("Debe retornar 400 por que no se suministra un sessionId")
    void devuelveBadRequestConSessionIdNoEnviada() throws Exception{
        mockMvc
                .perform(
                        get("/api/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
