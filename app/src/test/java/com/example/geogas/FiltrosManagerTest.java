package com.example.geogas;

import com.example.geogas.models.GasolineraAPI;
import com.example.geogas.utils.FavoritosManager;
import com.example.geogas.utils.FiltrosManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS PARA: FiltrosManager
 *
 * Esta clase prueba el sistema de filtrado de gasolineras, incluyendo:
 * - Filtros por ubicación (provincia, municipio)
 * - Filtros por tipo de combustible
 * - Filtros por servicios (24 horas)
 * - Filtros por precio máximo
 * - Combinación de múltiples filtros
 *
 * VINCULACIÓN CON REQUISITOS:
 * - OBJ-07: Filtrar gasolineras
 * - RF016: Filtros
 * - CU08: Filtrar Gasolineras
 * - CU15: Aplicar Filtros Avanzados (parcialmente)
 */
@RunWith(MockitoJUnitRunner.class)
public class FiltrosManagerTest {

    private FiltrosManager filtrosManager; // Objeto bajo prueba

    @Mock
    private FavoritosManager mockFavoritosManager; // Mock para pruebas de favoritos

    private List<GasolineraAPI> listaGasolineras; // Lista de prueba con gasolineras

    /**
     * Configuración inicial: crea una lista de gasolineras de ejemplo para las pruebas.
     */
    @Before
    public void setUp() {
        filtrosManager = new FiltrosManager();
        listaGasolineras = crearListaGasolinerasDePrueba();
    }

    /**
     * TEST: Filtrar gasolineras por provincia.
     *
     * Escenario: El usuario selecciona "Madrid" en el filtro de provincia.
     * Comportamiento esperado: Solo se muestran gasolineras de Madrid.
     */
    @Test
    public void testFiltrarPorProvincia() {
        // ARRANGE
        filtrosManager.setProvincia("Madrid");
        GasolineraAPI gasolineraMadrid = new GasolineraAPI();
        gasolineraMadrid.setProvincia("Madrid");
        gasolineraMadrid.setId("ES123");

        GasolineraAPI gasolineraBarcelona = new GasolineraAPI();
        gasolineraBarcelona.setProvincia("Barcelona"); // Esta NO debería pasar el filtro
        gasolineraBarcelona.setId("ES456");

        listaGasolineras.clear();
        listaGasolineras.add(gasolineraMadrid);
        listaGasolineras.add(gasolineraBarcelona);

        // ACT
        List<GasolineraAPI> resultado = filtrosManager.aplicarFiltros(listaGasolineras, mockFavoritosManager);

        // ASSERT
        assertEquals("Debería filtrar solo Madrid", 1, resultado.size());
        assertEquals("Madrid", resultado.get(0).getProvincia());
    }

    /**
     * TEST: Filtrar gasolineras por municipio.
     *
     * Escenario: El usuario selecciona "Alcobendas" en el filtro de municipio.
     * Comportamiento esperado: Solo se muestran gasolineras de Alcobendas.
     */
    @Test
    public void testFiltrarPorMunicipio() {
        // ARRANGE
        filtrosManager.setMunicipio("Alcobendas");
        GasolineraAPI gasolinera1 = new GasolineraAPI();
        gasolinera1.setMunicipio("Alcobendas");
        gasolinera1.setId("ES123");

        GasolineraAPI gasolinera2 = new GasolineraAPI();
        gasolinera2.setMunicipio("San Sebastián de los Reyes"); // NO debería pasar
        gasolinera2.setId("ES456");

        listaGasolineras.clear();
        listaGasolineras.add(gasolinera1);
        listaGasolineras.add(gasolinera2);

        // ACT
        List<GasolineraAPI> resultado = filtrosManager.aplicarFiltros(listaGasolineras, mockFavoritosManager);

        // ASSERT
        assertEquals("Debería filtrar solo Alcobendas", 1, resultado.size());
        assertEquals("Alcobendas", resultado.get(0).getMunicipio());
    }

    /**
     * TEST: Filtrar gasolineras por nombre/marca.
     *
     * Escenario: El usuario escribe "Repsol" en el buscador.
     * Comportamiento esperado: Solo se muestran gasolineras cuyo nombre comience con "Repsol".
     */
    @Test
    public void testFiltrarPorGasolinera() {
        // ARRANGE
        filtrosManager.setGasolinera("Repsol");
        GasolineraAPI gasolinera1 = new GasolineraAPI();
        gasolinera1.setRotulo("Repsol Alcobendas"); // Esta SÍ pasa (empieza con "Repsol")
        gasolinera1.setId("ES123");

        GasolineraAPI gasolinera2 = new GasolineraAPI();
        gasolinera2.setRotulo("Cepsa Madrid"); // Esta NO pasa
        gasolinera2.setId("ES456");

        listaGasolineras.clear();
        listaGasolineras.add(gasolinera1);
        listaGasolineras.add(gasolinera2);

        // ACT
        List<GasolineraAPI> resultado = filtrosManager.aplicarFiltros(listaGasolineras, mockFavoritosManager);

        // ASSERT
        assertEquals("Debería filtrar solo Repsol", 1, resultado.size());
        assertTrue("Debería contener 'Repsol'", resultado.get(0).getRotulo().contains("Repsol"));
    }

    /**
     * TEST: Filtrar gasolineras que tienen Gasolina 95.
     *
     * Escenario: El usuario activa el checkbox "Solo Gasolina 95".
     * Comportamiento esperado: Solo se muestran gasolineras con precio de Gasolina 95.
     */
    @Test
    public void testFiltrarPorGasolina95() {
        // ARRANGE
        filtrosManager.setSoloGasolina95(true);
        GasolineraAPI gasolinera1 = new GasolineraAPI();
        gasolinera1.setPrecioGasolina95("1.45"); // Tiene precio → SÍ pasa
        gasolinera1.setId("ES123");

        GasolineraAPI gasolinera2 = new GasolineraAPI();
        gasolinera2.setPrecioGasolina95(null); // Sin precio → NO pasa
        gasolinera2.setId("ES456");

        listaGasolineras.clear();
        listaGasolineras.add(gasolinera1);
        listaGasolineras.add(gasolinera2);

        // ACT
        List<GasolineraAPI> resultado = filtrosManager.aplicarFiltros(listaGasolineras, mockFavoritosManager);

        // ASSERT
        assertEquals("Debería filtrar solo gasolineras con Gasolina 95", 1, resultado.size());
        assertEquals("1.45", resultado.get(0).getPrecioGasolina95());
    }

    /**
     * TEST: Filtrar gasolineras con servicio 24 horas.
     *
     * Escenario: El usuario activa el checkbox "Solo 24 horas".
     * Comportamiento esperado: Solo se muestran gasolineras con horario "24H".
     */
    @Test
    public void testFiltrarPor24Horas() {
        // ARRANGE
        filtrosManager.setSolo24Horas(true);
        GasolineraAPI gasolinera1 = new GasolineraAPI();
        gasolinera1.setHorario("24H"); // SÍ pasa
        gasolinera1.setId("ES123");

        GasolineraAPI gasolinera2 = new GasolineraAPI();
        gasolinera2.setHorario("L-V: 8:00-22:00"); // NO pasa
        gasolinera2.setId("ES456");

        listaGasolineras.clear();
        listaGasolineras.add(gasolinera1);
        listaGasolineras.add(gasolinera2);

        // ACT
        List<GasolineraAPI> resultado = filtrosManager.aplicarFiltros(listaGasolineras, mockFavoritosManager);

        // ASSERT
        assertEquals("Debería filtrar solo 24H", 1, resultado.size());
        assertEquals("24H", resultado.get(0).getHorario());
    }

    /**
     * TEST: Filtrar gasolineras por precio máximo de Gasolina 95.
     *
     * Escenario: El usuario establece un precio máximo de 1.50€ para Gasolina 95.
     * Comportamiento esperado: Solo se muestran gasolineras con precio ≤ 1.50€.
     */
    @Test
    public void testFiltrarPorPrecioMaxGasolina95() {
        // ARRANGE
        filtrosManager.setPrecioMaxGasolina95(1.50);
        GasolineraAPI gasolinera1 = new GasolineraAPI();
        gasolinera1.setPrecioGasolina95("1.45"); // 1.45 ≤ 1.50 → SÍ pasa
        gasolinera1.setId("ES123");

        GasolineraAPI gasolinera2 = new GasolineraAPI();
        gasolinera2.setPrecioGasolina95("1.55"); // 1.55 > 1.50 → NO pasa
        gasolinera2.setId("ES456");

        listaGasolineras.clear();
        listaGasolineras.add(gasolinera1);
        listaGasolineras.add(gasolinera2);

        // ACT
        List<GasolineraAPI> resultado = filtrosManager.aplicarFiltros(listaGasolineras, mockFavoritosManager);

        // ASSERT
        assertEquals("Debería filtrar por precio <= 1.50", 1, resultado.size());
        assertEquals("1.45", resultado.get(0).getPrecioGasolina95());
    }

    /**
     * TEST: Verificar que NO hay filtros activos al inicio.
     *
     * Escenario: Recién iniciada la aplicación, sin filtros aplicados.
     * Comportamiento esperado: El método debe retornar false.
     */
    @Test
    public void testTieneFiltrosActivos_SinFiltros() {
        // ACT
        boolean resultado = filtrosManager.tieneFiltrosActivos();

        // ASSERT
        assertFalse("No debería tener filtros activos", resultado);
    }

    /**
     * TEST: Verificar que SÍ hay filtros activos después de configurar algunos.
     *
     * Escenario: El usuario ha seleccionado provincia y tipo de combustible.
     * Comportamiento esperado: El método debe retornar true.
     */
    @Test
    public void testTieneFiltrosActivos_ConFiltros() {
        // ARRANGE
        filtrosManager.setProvincia("Madrid");
        filtrosManager.setSoloGasolina95(true);

        // ACT
        boolean resultado = filtrosManager.tieneFiltrosActivos();

        // ASSERT
        assertTrue("Debería tener filtros activos", resultado);
    }

    /**
     * Método auxiliar: crea una lista de gasolineras de ejemplo para pruebas.
     * Esto evita repetir código en cada test.
     */
    private List<GasolineraAPI> crearListaGasolinerasDePrueba() {
        List<GasolineraAPI> gasolineras = new ArrayList<>();

        // Gasolinera 1: Repsol en Alcobendas, 24H
        GasolineraAPI g1 = new GasolineraAPI();
        g1.setId("ES001");
        g1.setRotulo("Repsol Test");
        g1.setProvincia("Madrid");
        g1.setMunicipio("Alcobendas");
        g1.setPrecioGasolina95("1.45");
        g1.setPrecioGasoleoA("1.35");
        g1.setHorario("24H");
        gasolineras.add(g1);

        // Gasolinera 2: Cepsa en Barcelona, horario limitado
        GasolineraAPI g2 = new GasolineraAPI();
        g2.setId("ES002");
        g2.setRotulo("Cepsa Test");
        g2.setProvincia("Barcelona");
        g2.setMunicipio("Barcelona");
        g2.setPrecioGasolina95("1.50");
        g2.setPrecioGasoleoA("1.40");
        g2.setHorario("L-V: 8:00-22:00");
        gasolineras.add(g2);

        return gasolineras;
    }
}