package com.eliasbuenosdias.geogas;

import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.utils.FavoritosManager;
import com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * PRUEBAS UNITARIAS PARA: PuntuadorGasolineras
 *
 * Esta clase prueba el algoritmo de puntuación para ordenar gasolineras por relevancia:
 * - Puntuación basada en precios (más barato = más puntos)
 * - Puntuación basada en variedad de combustibles
 * - Puntuación extra para gasolineras favoritas
 * - Ordenación de mayor a menor puntuación
 *
 * VINCULACIÓN CON REQUISITOS:
 * - RF030: Sistema de puntuación para relevancia
 * - RF031: Algoritmo de selección por precios, variedad y favoritos
 */
@RunWith(MockitoJUnitRunner.class)
public class PuntuadorGasolinerasTest {

    @Mock
    private FavoritosManager mockFavoritosManager; // Mock para verificar favoritos

    private PuntuadorGasolineras puntuador; // Objeto bajo prueba
    private List<GasolineraAPI> gasolineras; // Lista para pruebas

    @Before
    public void setUp() {
        // Inicializa el puntuador con el mock
        puntuador = new PuntuadorGasolineras(mockFavoritosManager);
        gasolineras = new ArrayList<>();
    }

    /**
     * TEST: Calcular puntuación para gasolinera COMPLETA (con precios y favorita).
     *
     * Escenario: Gasolinera con múltiples combustibles a buen precio y marcada como favorita.
     * Comportamiento esperado: Puntuación alta (cercana a 1.0).
     */
    @Test
    public void testCalcularPuntuacion_GasolineraCompleta() {
        // ARRANGE
        GasolineraAPI gasolinera = new GasolineraAPI();
        gasolinera.setId("ES123");
        gasolinera.setPrecioGasolina95("1.40"); // Precio bajo (bueno)
        gasolinera.setPrecioGasoleoA("1.30");   // Precio bajo (bueno)
        gasolinera.setPrecioGLP("0.80");        // Precio muy bajo (excelente)
        gasolinera.setPrecioGasolina98("1.60"); // Otro combustible disponible

        // Configura el mock: esta gasolinera ES favorita
        when(mockFavoritosManager.esFavorita("ES123")).thenReturn(true);

        // ACT
        double puntuacion = puntuador.calcularPuntuacion(gasolinera);

        // ASSERT
        assertTrue("La puntuación debería ser positiva", puntuacion > 0);
        assertTrue("La puntuación debería estar entre 0 y 1", puntuacion <= 1.0);
    }

    /**
     * TEST: Calcular puntuación para gasolinera SIN precios NI favorita.
     *
     * Escenario: Gasolinera sin información de precios y no marcada como favorita.
     * Comportamiento esperado: Puntuación 0.
     */
    @Test
    public void testCalcularPuntuacion_GasolineraSinPrecios() {
        // ARRANGE
        GasolineraAPI gasolinera = new GasolineraAPI();
        gasolinera.setId("ES456");
        // Sin precios establecidos

        // Configura el mock: esta gasolinera NO es favorita
        when(mockFavoritosManager.esFavorita("ES456")).thenReturn(false);

        // ACT
        double puntuacion = puntuador.calcularPuntuacion(gasolinera);

        // ASSERT
        assertEquals("Sin precios ni favoritos, puntuación debería ser 0",
                0.0, puntuacion, 0.001); // delta de 0.001 para comparación de doubles
    }

    /**
     * TEST: Calcular puntuación para gasolinera FAVORITA.
     *
     * Escenario: Gasolinera con precio normal pero marcada como favorita.
     * Comportamiento esperado: Puntuación significativa por ser favorita.
     */
    @Test
    public void testCalcularPuntuacion_GasolineraFavorita() {
        // ARRANGE
        GasolineraAPI gasolinera = new GasolineraAPI();
        gasolinera.setId("ES789");
        gasolinera.setPrecioGasolina95("1.50"); // Precio normal

        // Configura el mock: esta gasolinera ES favorita
        when(mockFavoritosManager.esFavorita("ES789")).thenReturn(true);

        // ACT
        double puntuacion = puntuador.calcularPuntuacion(gasolinera);

        // ASSERT
        assertTrue("Gasolinera favorita debería tener puntuación más alta",
                puntuacion > 0.3);
    }

    /**
     * TEST: Ordenar gasolineras por puntuación (de mayor a menor).
     *
     * Escenario: Dos gasolineras, una más barata que la otra.
     * Comportamiento esperado: La más barata aparece primero en la lista ordenada.
     */
    @Test
    public void testOrdenarPorPuntuacion() {
        // ARRANGE
        GasolineraAPI gasolinera1 = new GasolineraAPI();
        gasolinera1.setId("ES001");
        gasolinera1.setPrecioGasolina95("1.30"); // Más barata → mayor puntuación

        GasolineraAPI gasolinera2 = new GasolineraAPI();
        gasolinera2.setId("ES002");
        gasolinera2.setPrecioGasolina95("1.60"); // Más cara → menor puntuación

        gasolineras.add(gasolinera1);
        gasolineras.add(gasolinera2);

        // Ninguna es favorita para este test
        when(mockFavoritosManager.esFavorita("ES001")).thenReturn(false);
        when(mockFavoritosManager.esFavorita("ES002")).thenReturn(false);

        // ACT
        List<PuntuadorGasolineras.GasolineraPuntuada> resultado =
                puntuador.ordenarPorPuntuacion(gasolineras);

        // ASSERT
        assertEquals("Debería tener 2 elementos", 2, resultado.size());
        assertTrue("La más barata debería tener mayor puntuación",
                resultado.get(0).puntuacion > resultado.get(1).puntuacion);
        assertEquals("La primera debería ser la más barata",
                "1.30", resultado.get(0).gasolinera.getPrecioGasolina95());
    }

    /**
     * TEST: Configurar y verificar los pesos del algoritmo.
     *
     * Escenario: Cambiar los pesos del algoritmo de puntuación.
     * Comportamiento esperado: Los nuevos pesos se almacenan correctamente.
     */
    @Test
    public void testPesosConfigurables() {
        // ARRANGE
        double nuevoPesoPrecios = 0.5;
        double nuevoPesoVariedad = 0.3;
        double nuevoPesoFavoritos = 0.2;

        // ACT
        puntuador.setPesoPrecios(nuevoPesoPrecios);
        puntuador.setPesoVariedad(nuevoPesoVariedad);
        puntuador.setPesoFavoritos(nuevoPesoFavoritos);

        // ASSERT
        assertEquals("Peso precios debería ser 0.5",
                nuevoPesoPrecios, puntuador.getPesoPrecios(), 0.001);
        assertEquals("Peso variedad debería ser 0.3",
                nuevoPesoVariedad, puntuador.getPesoVariedad(), 0.001);
        assertEquals("Peso favoritos debería ser 0.2",
                nuevoPesoFavoritos, puntuador.getPesoFavoritos(), 0.001);
    }
}