package com.example.geogas.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS PARA: FavoritosManager
 *
 * Esta clase prueba la gestión de gasolineras favoritas, incluyendo:
 * - Añadir/eliminar favoritos (toggle)
 * - Verificar si una gasolinera es favorita
 * - Persistencia en SharedPreferences
 *
 * VINCULACIÓN CON REQUISITOS:
 * - OBJ-05: Gestionar gasolineras favoritas
 * - RF012: Añadir/eliminar favoritos
 * - RF013: Persistencia local de favoritos
 * - RF014: Visualización diferenciada de favoritas
 * - CU05: Añadir a Favoritos
 * - CU06: Eliminar de Favoritos
 */
@RunWith(MockitoJUnitRunner.class) // Usa el runner de Mockito para inyectar mocks
public class FavoritosManagerTest {

    // Mocks que simulan las dependencias de Android
    @Mock
    private Context mockContext; // Simula el contexto de Android

    @Mock
    private SharedPreferences mockSharedPreferences; // Simula SharedPreferences

    @Mock
    private SharedPreferences.Editor mockEditor; // Simula el editor de SharedPreferences

    private FavoritosManager favoritosManager; // Objeto bajo prueba

    /**
     * Configuración inicial antes de cada test.
     * Se ejecuta automáticamente antes de cada método @Test.
     */
    @Before
    public void setUp() {
        // Configura el comportamiento de los mocks
        when(mockContext.getSharedPreferences(eq("GeoGasFavoritos"), anyInt()))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putStringSet(anyString(), any(Set.class))).thenReturn(mockEditor);

        // Crea la instancia real del objeto a probar
        favoritosManager = new FavoritosManager(mockContext);
    }

    /**
     * TEST: Añadir una gasolinera a favoritos cuando NO está en la lista.
     *
     * Escenario: El usuario presiona el botón "Añadir a favoritos" por primera vez.
     * Comportamiento esperado: La gasolinera se guarda en SharedPreferences.
     */
    @Test
    public void testToggleFavorito_AñadirFavorito() {
        // ARRANGE: Prepara el escenario de prueba
        String gasolineraId = "ES12345";
        Set<String> favoritosVacios = new HashSet<>(); // Lista vacía de favoritos
        when(mockSharedPreferences.getStringSet(eq("favoritos"), any(Set.class)))
                .thenReturn(favoritosVacios);

        // ACT: Ejecuta la acción a probar
        favoritosManager.toggleFavorito(gasolineraId);

        // ASSERT: Verifica el resultado esperado
        // Verifica que se guardó un conjunto que contiene el ID de la gasolinera
        verify(mockEditor).putStringSet(eq("favoritos"), argThat(set ->
                set.contains(gasolineraId) && set.size() == 1));
        verify(mockEditor).apply(); // Verifica que se aplicaron los cambios
    }

    /**
     * TEST: Eliminar una gasolinera de favoritos cuando YA está en la lista.
     *
     * Escenario: El usuario presiona el botón "Quitar de favoritos".
     * Comportamiento esperado: La gasolinera se elimina de SharedPreferences.
     */
    @Test
    public void testToggleFavorito_EliminarFavorito() {
        // ARRANGE
        String gasolineraId = "ES12345";
        Set<String> favoritosConElemento = new HashSet<>();
        favoritosConElemento.add(gasolineraId); // La gasolinera ya es favorita
        when(mockSharedPreferences.getStringSet(eq("favoritos"), any(Set.class)))
                .thenReturn(favoritosConElemento);

        // ACT
        favoritosManager.toggleFavorito(gasolineraId);

        // ASSERT
        // Verifica que se guardó un conjunto VACÍO (sin el ID)
        verify(mockEditor).putStringSet(eq("favoritos"), argThat(set ->
                !set.contains(gasolineraId) && set.isEmpty()));
        verify(mockEditor).apply();
    }

    /**
     * TEST: Verificar que una gasolinera ES favorita.
     *
     * Escenario: El sistema necesita saber si una gasolinera está marcada como favorita
     * para mostrar el icono correcto en el mapa.
     */
    @Test
    public void testEsFavorita_FavoritaExistente() {
        // ARRANGE
        String gasolineraId = "ES12345";
        Set<String> favoritos = new HashSet<>();
        favoritos.add(gasolineraId); // La gasolinera está en la lista
        when(mockSharedPreferences.getStringSet(eq("favoritos"), any(Set.class)))
                .thenReturn(favoritos);

        // ACT
        boolean resultado = favoritosManager.esFavorita(gasolineraId);

        // ASSERT
        assertTrue("La gasolinera debería ser favorita", resultado);
    }

    /**
     * TEST: Verificar que una gasolinera NO es favorita.
     *
     * Escenario: El sistema necesita saber que una gasolinera no está marcada
     * para mostrar el icono normal (no estrella).
     */
    @Test
    public void testEsFavorita_NoFavorita() {
        // ARRANGE
        String gasolineraId = "ES12345";
        Set<String> favoritos = new HashSet<>();
        favoritos.add("OTRA_ID"); // Solo hay otra gasolinera como favorita
        when(mockSharedPreferences.getStringSet(eq("favoritos"), any(Set.class)))
                .thenReturn(favoritos);

        // ACT
        boolean resultado = favoritosManager.esFavorita(gasolineraId);

        // ASSERT
        assertFalse("La gasolinera NO debería ser favorita", resultado);
    }

    /**
     * TEST: Obtener la lista completa de favoritos.
     *
     * Escenario: El sistema necesita cargar todos los IDs de gasolineras favoritas
     * para resaltarlas en el mapa al iniciar la aplicación.
     */
    @Test
    public void testGetFavoritos_MultipleFavoritos() {
        // ARRANGE
        Set<String> favoritosEsperados = new HashSet<>();
        favoritosEsperados.add("ES12345");
        favoritosEsperados.add("ES67890"); // Dos gasolineras favoritas
        when(mockSharedPreferences.getStringSet(eq("favoritos"), any(Set.class)))
                .thenReturn(favoritosEsperados);

        // ACT
        Set<String> resultado = favoritosManager.getFavoritos();

        // ASSERT
        assertEquals("Debería tener 2 favoritos", 2, resultado.size());
        assertTrue("Debería contener ES12345", resultado.contains("ES12345"));
        assertTrue("Debería contener ES67890", resultado.contains("ES67890"));
    }

    /**
     * TEST: Limpiar todos los favoritos.
     *
     * Escenario: Test auxiliar para verificar que se pueden eliminar todos los favoritos.
     * Esto podría usarse para una función de "limpiar favoritos" en el futuro.
     */
    @Test
    public void testLimpiarFavoritos() {
        // ACT
        favoritosManager.limpiarFavoritos();

        // ASSERT
        verify(mockEditor).putStringSet(eq("favoritos"), argThat(Set::isEmpty));
        verify(mockEditor).apply();
    }
}