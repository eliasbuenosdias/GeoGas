package com.eliasbuenosdias.geogas;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.models.GasolineraResponse;

/**
 * PRUEBAS UNITARIAS PARA: GasolineraResponse
 *
 * Esta clase prueba el contenedor de la lista de gasolineras.
 */
public class GasolineraResponseTest {

    private GasolineraResponse response;

    @Before
    public void setUp() {
        response = new GasolineraResponse();
    }

    @Test
    public void testSetAndGetListaEESSPrecio() {
        List<GasolineraAPI> lista = new ArrayList<>();

        GasolineraAPI gasolinera1 = new GasolineraAPI();
        gasolinera1.setId("123");
        gasolinera1.setRotulo("Gasolinera 1");
        lista.add(gasolinera1);

        GasolineraAPI gasolinera2 = new GasolineraAPI();
        gasolinera2.setId("456");
        gasolinera2.setRotulo("Gasolinera 2");
        lista.add(gasolinera2);

        response.setListaEESSPrecio(lista);

        assertEquals(2, response.getListaEESSPrecio().size());
        assertEquals("123", response.getListaEESSPrecio().get(0).getId());
        assertEquals("Gasolinera 1", response.getListaEESSPrecio().get(0).getRotulo());
        assertEquals("456", response.getListaEESSPrecio().get(1).getId());
        assertEquals("Gasolinera 2", response.getListaEESSPrecio().get(1).getRotulo());
    }

    @Test
    public void testListaVacia() {
        // Inicialmente debería ser null
        assertNull(response.getListaEESSPrecio());

        // Podemos setear una lista vacía
        response.setListaEESSPrecio(new ArrayList<>());
        assertNotNull(response.getListaEESSPrecio());
        assertTrue(response.getListaEESSPrecio().isEmpty());
    }

    @Test
    public void testListaNull() {
        response.setListaEESSPrecio(null);
        assertNull(response.getListaEESSPrecio());
    }
}