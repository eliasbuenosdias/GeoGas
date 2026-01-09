package com.eliasbuenosdias.geogas;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.eliasbuenosdias.geogas.models.GasolineraAPI;

/**
 * PRUEBAS UNITARIAS PARA: GasolineraAPI
 *
 * Esta clase prueba el modelo principal de datos de gasolineras.
 * Cubre getters/setters y manejo de valores nulos/vacíos.
 */
public class GasolineraAPITest {

    private GasolineraAPI gasolinera;

    @Before
    public void setUp() {
        gasolinera = new GasolineraAPI();
    }

    @Test
    public void testSettersAndGetters_Identificacion() {
        // Test de campos básicos de identificación
        gasolinera.setId("12345");
        gasolinera.setRotulo("Repsol Test");
        gasolinera.setDireccion("Calle Ejemplo 123");
        gasolinera.setCodigoPostal("28001");
        gasolinera.setLocalidad("Madrid");
        gasolinera.setMunicipio("Madrid");
        gasolinera.setProvincia("Madrid");

        assertEquals("12345", gasolinera.getId());
        assertEquals("Repsol Test", gasolinera.getRotulo());
        assertEquals("Calle Ejemplo 123", gasolinera.getDireccion());
        assertEquals("28001", gasolinera.getCodigoPostal());
        assertEquals("Madrid", gasolinera.getLocalidad());
        assertEquals("Madrid", gasolinera.getMunicipio());
        assertEquals("Madrid", gasolinera.getProvincia());
    }

    @Test
    public void testSettersAndGetters_Coordenadas() {
        gasolinera.setLatitud("40.4168");
        gasolinera.setLongitud("-3.7038");

        assertEquals("40.4168", gasolinera.getLatitud());
        assertEquals("-3.7038", gasolinera.getLongitud());
    }

    @Test
    public void testSettersAndGetters_PreciosPrincipales() {
        gasolinera.setPrecioGasolina95("1.45");
        gasolinera.setPrecioGasoleoA("1.35");
        gasolinera.setPrecioGasoleoPremium("1.50");
        gasolinera.setPrecioGLP("0.80");

        assertEquals("1.45", gasolinera.getPrecioGasolina95());
        assertEquals("1.35", gasolinera.getPrecioGasoleoA());
        assertEquals("1.50", gasolinera.getPrecioGasoleoPremium());
        assertEquals("0.80", gasolinera.getPrecioGLP());
    }

    @Test
    public void testCamposNulosPorDefecto() {
        // Por defecto deberían ser null
        assertNull(gasolinera.getRotulo());
        assertNull(gasolinera.getPrecioGasolina95());
        assertNull(gasolinera.getPrecioGasoleoA());
        assertNull(gasolinera.getHorario());
    }

    @Test
    public void testCamposPuedenSerVacios() {
        gasolinera.setRotulo("");
        gasolinera.setPrecioGasolina95("");
        gasolinera.setHorario("");

        assertEquals("", gasolinera.getRotulo());
        assertEquals("", gasolinera.getPrecioGasolina95());
        assertEquals("", gasolinera.getHorario());
    }
}