package com.saucedemo.controller;

import com.saucedemo.model.Product;
import com.saucedemo.service.ProductService;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(
                productController
        ).build();
    }

    private Product mochila() {
        Product product = new Product("mochila", "Mochila Urban Trek", "Mochila resistente para el dia a dia.",
                "Diseñada para acompañarte en tu ritmo diario.", 119900.0, "Images/mochila.png");
        product.setId(1L);
        return product;
    }

    private Product gafas() {
        Product product = new Product("gafas", "Gafas Solar Shade UV", "Gafas de sol con proteccion UV.",
                "Protege tus ojos con un toque de estilo.", 49900.0, "Images/gafas.jpg");
        product.setId(2L);
        return product;
    }

    @Nested
    @DisplayName("GET /api/products")
    class GetAllProducts {

        @Test
        @DisplayName("Retorna 200 con el catálogo completo")
        void retorna200ConElCatalogoCompleto() throws Exception {
            when(productService.getAllProducts()).thenReturn(List.of(mochila(), gafas()));

            mockMvc
                    .perform(
                            get("/api/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))

                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].code").value("mochila"))
                    .andExpect(jsonPath("$[0].name").value("Mochila Urban Trek"))
                    .andExpect(jsonPath("$[0].description").value("Mochila resistente para el dia a dia."))
                    .andExpect(jsonPath("$[0].detailDescription").value("Diseñada para acompañarte en tu ritmo diario."))
                    .andExpect(jsonPath("$[0].price").value(119900.0))
                    .andExpect(jsonPath("$[0].imageUrl").value("Images/mochila.png"))

                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].code").value("gafas"))
                    .andExpect(jsonPath("$[1].name").value("Gafas Solar Shade UV"))
                    .andExpect(jsonPath("$[1].price").value(49900.0))
                    .andExpect(jsonPath("$[1].imageUrl").value("Images/gafas.jpg"));

            verify(productService).getAllProducts();
        }

        @Test
        @DisplayName("Retorna 200 con un arreglo vacío cuando no hay productos")
        void retorna200ConArregloVacio() throws Exception {
            when(productService.getAllProducts()).thenReturn(List.of());

            mockMvc
                    .perform(
                            get("/api/products")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(productService).getAllProducts();
        }
    }

    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductById {

        @Test
        @DisplayName("Retorna 200 con el producto solicitado")
        void retorna200ConElProductoSolicitado() throws Exception {
            when(productService.getProductById(1L)).thenReturn(Optional.of(mochila()));

            mockMvc
                    .perform(
                            get("/api/products/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.code").value("mochila"))
                    .andExpect(jsonPath("$.name").value("Mochila Urban Trek"))
                    .andExpect(jsonPath("$.description").value("Mochila resistente para el dia a dia."))
                    .andExpect(jsonPath("$.detailDescription").value("Diseñada para acompañarte en tu ritmo diario."))
                    .andExpect(jsonPath("$.price").value(119900.0))
                    .andExpect(jsonPath("$.imageUrl").value("Images/mochila.png"));

            verify(productService).getProductById(1L);
        }

        @Test
        @DisplayName("Retorna 404 sin cuerpo cuando el producto no existe")
        void retorna404SinCuerpoCuandoElProductoNoExiste() throws Exception {
            when(productService.getProductById(99L)).thenReturn(Optional.empty());

            mockMvc
                    .perform(
                            get("/api/products/99")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(""));

            verify(productService).getProductById(99L);
        }

        @Test
        @DisplayName("Retorna 400 cuando el id no es numérico")
        void retorna400CuandoElIdNoEsNumerico() throws Exception {
            mockMvc
                    .perform(
                            get("/api/products/abc")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(productService);
        }
    }
}
