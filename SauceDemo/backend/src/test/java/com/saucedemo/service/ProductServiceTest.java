package com.saucedemo.service;

import com.saucedemo.model.Product;
import com.saucedemo.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

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
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("Devuelve todos los productos conservando el orden del repositorio")
        void devuelveTodosLosProductosEnOrden() {
            Product mochila = mochila();
            Product gafas = gafas();
            when(productRepository.findAll()).thenReturn(List.of(mochila, gafas));

            List<Product> resultado = productService.getAllProducts();

            assertThat(resultado)
                    .as("catálogo completo")
                    .containsExactly(mochila, gafas);

            assertThat(resultado)
                    .extracting(Product::getCode)
                    .containsExactly("mochila", "gafas");

            assertThat(resultado)
                    .extracting(Product::getPrice)
                    .containsExactly(119900.0, 49900.0);

            verify(productRepository).findAll();
            verifyNoMoreInteractions(productRepository);
        }

        @Test
        @DisplayName("Devuelve una lista vacía cuando el catálogo está vacío")
        void devuelveListaVaciaCuandoNoHayProductos() {
            when(productRepository.findAll()).thenReturn(List.of());

            List<Product> resultado = productService.getAllProducts();

            assertThat(resultado)
                    .as("catálogo sin productos")
                    .isNotNull()
                    .isEmpty();

            verify(productRepository).findAll();
        }

        @Test
        @DisplayName("Propaga la excepción cuando el repositorio falla")
        void propagaLaExcepcionCuandoElRepositorioFalla() {
            when(productRepository.findAll()).thenThrow(new RuntimeException("Base de datos no disponible"));

            assertThatThrownBy(() -> productService.getAllProducts())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Base de datos no disponible");

            verify(productRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("Devuelve el producto cuando el id existe")
        void devuelveElProductoCuandoElIdExiste() {
            Product mochila = mochila();
            when(productRepository.findById(1L)).thenReturn(Optional.of(mochila));

            Optional<Product> resultado = productService.getProductById(1L);

            assertThat(resultado)
                    .as("producto con id 1")
                    .containsSame(mochila);

            assertThat(resultado.get().getCode()).isEqualTo("mochila");
            assertThat(resultado.get().getName()).isEqualTo("Mochila Urban Trek");
            assertThat(resultado.get().getDetailDescription()).isEqualTo("Diseñada para acompañarte en tu ritmo diario.");
            assertThat(resultado.get().getPrice()).isEqualTo(119900.0);
            assertThat(resultado.get().getImageUrl()).isEqualTo("Images/mochila.png");

            verify(productRepository).findById(1L);
            verifyNoMoreInteractions(productRepository);
        }

        @Test
        @DisplayName("Devuelve un Optional vacío cuando el id no existe")
        void devuelveOptionalVacioCuandoElIdNoExiste() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<Product> resultado = productService.getProductById(99L);

            // El servicio no lanza excepción: deja que el controlador decida el 404.
            assertThat(resultado)
                    .as("producto con id inexistente")
                    .isEmpty();

            verify(productRepository).findById(99L);
        }

        @Test
        @DisplayName("Consulta el repositorio con el id recibido, sin transformarlo")
        void consultaElRepositorioConElIdRecibido() {
            when(productRepository.findById(7L)).thenReturn(Optional.empty());

            productService.getProductById(7L);

            verify(productRepository).findById(7L);
            verifyNoMoreInteractions(productRepository);
        }
    }
}
