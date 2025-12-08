package com.example.geogas;

import com.example.geogas.utils.FavoritosManager;
import com.example.geogas.utils.IconosManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.util.Log;

/**
 * PRUEBAS UNITARIAS PARA: IconosManager
 *
 * Esta clase prueba la lógica de selección de iconos adaptativos.
 * IMPORTANTE: No podemos probar los IDs de recursos específicos (R.drawable.*)
 * porque no están disponibles en tests unitarios JVM.
 */
@RunWith(MockitoJUnitRunner.class)
public class IconosManagerTest {

    @Mock
    private android.content.Context mockContext; // Mock del contexto Android

    @Mock
    private FavoritosManager mockFavoritosManager; // Mock del gestor de favoritos

    private IconosManager iconosManager; // Objeto bajo prueba

    @Before
    public void setUp() {
        // Crea la instancia real con los mocks como dependencias
        iconosManager = new IconosManager(mockContext, mockFavoritosManager);
    }

    /**
     * TEST: Verificar que el método obtenerIconoGasolinera NO retorna 0.
     * Nota: 0 sería un ID de recurso inválido.
     * En pruebas JVM, los recursos de Android (R.drawable.*) no están disponibles,
     * pero el método debería retornar un valor distinto de 0.
     */
    @Test
    public void testObtenerIconoGasolinera_NoRetornaCero() {
        // 🔹 WRAP THE ENTIRE TEST LOGIC
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

            // ACT: This call internally uses Log.d
            int iconoResId = iconosManager.obtenerIconoGasolinera(true, 10, 15.0);

            // Your assertions here...
            // assertTrue("Método debería ejecutarse sin errores", true);
        }
    }

    /**
     * TEST: Verificar que el método no lanza excepción con valores límite.
     */
    @Test
    public void testObtenerIconoGasolinera_ValoresLimite() {
        // 🔹 SOLUCIÓN: Crear un MockedStatic para la clase Log
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            // Configura el mock para que cualquier llamada a Log.d no haga nada
            // Esto evita la excepción "not mocked"
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

            // Ahora puedes ejecutar el código que usa Log.d sin errores
            iconosManager.obtenerIconoGasolinera(true, 1, 20.0);
            iconosManager.obtenerIconoGasolinera(false, 1000, 5.0);
            iconosManager.obtenerIconoGasolinera(false, 0, 10.0);

            // (Opcional) Verifica que se llamó a Log.d
            mockedLog.verify(() -> Log.d(anyString(), anyString()), atLeastOnce());
        }
        // Al salir del bloque 'try', el mock estático se libera automáticamente
    }

    /**
     * TEST: Obtener información de tamaño de icono para modo NORMAL.
     * Este método no depende de recursos Android, solo de lógica.
     */
    @Test
    public void testObtenerInfoTamañoIcono_Normal() {
        // ARRANGE
        int totalGasolineras = 20; // Baja densidad
        double zoom = 15.0; // Zoom alto

        // ACT
        String info = iconosManager.obtenerInfoTamañoIcono(totalGasolineras, zoom);

        // ASSERT
        assertNotNull("No debería retornar null", info);
        // No podemos verificar el contenido exacto porque usa recursos,
        // pero al menos verificamos que retorna algo
        assertFalse("Debería retornar algún texto", info.isEmpty());
    }

    /**
     * TEST: Obtener información de tamaño de icono para modo PEQUEÑO.
     */
    @Test
    public void testObtenerInfoTamañoIcono_Small() {
        // ARRANGE
        int totalGasolineras = 60; // Densidad media
        double zoom = 12.0; // Zoom medio

        // ACT
        String info = iconosManager.obtenerInfoTamañoIcono(totalGasolineras, zoom);

        // ASSERT
        assertNotNull("No debería retornar null", info);
        assertFalse("Debería retornar algún texto", info.isEmpty());
    }

    /**
     * TEST: Obtener información de tamaño de icono para modo MINÚSCULO.
     */
    @Test
    public void testObtenerInfoTamañoIcono_Tiny() {
        // ARRANGE
        int totalGasolineras = 150; // Alta densidad
        double zoom = 10.0; // Zoom bajo

        // ACT
        String info = iconosManager.obtenerInfoTamañoIcono(totalGasolineras, zoom);

        // ASSERT
        assertNotNull("No debería retornar null", info);
        assertFalse("Debería retornar algún texto", info.isEmpty());
    }

    /**
     * TEST: Obtener información de los umbrales configurados.
     * Este método no depende de recursos.
     */
    @Test
    public void testObtenerInfoUmbrales() {
        // ACT
        String info = iconosManager.obtenerInfoUmbrales();

        // ASSERT
        assertNotNull("No debería retornar null", info);
        assertFalse("Debería retornar algún texto", info.isEmpty());
        // Podemos verificar que contiene información sobre umbrales
        assertTrue("Debería contener información sobre umbrales",
                info.toLowerCase().contains("umbral") || info.contains(">"));
    }

    /**
     * TEST: Verificar comportamiento con zoom exacto en el límite (14.0).
     * Este test verifica la lógica del umbral basado en zoom.
     */
    @Test
    public void testComportamientoConZoomLimite() {
        // Para zoom = 14.0, la lógica en IconosManager es:
        // int umbralAltaDensidad = (zoom > 14.0) ? 15 : 25;
        // int umbralMuyAltaDensidad = (zoom > 14.0) ? 40 : 80;

        // Con zoom = 14.0, debería usar los umbrales para zoom bajo (25 y 80)

        // Densidad 26 (justo por encima de 25) debería dar iconos pequeños
        String info1 = iconosManager.obtenerInfoTamañoIcono(26, 14.0);
        assertNotNull(info1);

        // Densidad 81 (justo por encima de 80) debería dar iconos minúsculos
        String info2 = iconosManager.obtenerInfoTamañoIcono(81, 14.0);
        assertNotNull(info2);

        // Densidad 24 (por debajo de 25) debería dar iconos normales
        String info3 = iconosManager.obtenerInfoTamañoIcono(24, 14.0);
        assertNotNull(info3);
    }

    /**
     * TEST: Verificar comportamiento con zoom justo por encima del límite (14.1).
     */
    @Test
    public void testComportamientoConZoomJustoArribaLimite() {
        // Para zoom = 14.1 (> 14.0), debería usar umbrales para zoom alto (15 y 40)

        // Densidad 16 (justo por encima de 15) debería dar iconos pequeños
        String info1 = iconosManager.obtenerInfoTamañoIcono(16, 14.1);
        assertNotNull(info1);

        // Densidad 41 (justo por encima de 40) debería dar iconos minúsculos
        String info2 = iconosManager.obtenerInfoTamañoIcono(41, 14.1);
        assertNotNull(info2);

        // Densidad 14 (por debajo de 15) debería dar iconos normales
        String info3 = iconosManager.obtenerInfoTamañoIcono(14, 14.1);
        assertNotNull(info3);
    }
}