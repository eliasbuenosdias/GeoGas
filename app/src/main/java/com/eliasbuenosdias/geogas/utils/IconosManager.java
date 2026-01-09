
package com.eliasbuenosdias.geogas.utils;

import android.content.Context;
import android.util.Log;

import com.eliasbuenosdias.geogas.R;


/**
 * Gestor de iconos adaptativos para marcadores de gasolineras en el mapa.
 * <p>
 * Esta clase implementa el patrón Factory para proporcionar iconos de diferentes
 * tamaños según la densidad de gasolineras visibles y el nivel de zoom del mapa.
 * Los iconos se ajustan dinámicamente para optimizar la visualización y evitar
 * saturación visual cuando hay muchas gasolineras en pantalla.
 * </p>
 *
 * @author Elías Prieto Parrilla
 * @version 1.0
 */
public class IconosManager {

    private Context context;

    private FavoritosManager favoritosManager;

    // Umbrales para cambiar entre modos de icono
    private static final int UMBRAL_ALTA_DENSIDAD = 50;  // Más de 30 gasolineras -> iconos pequeños
    private static final int UMBRAL_MUY_ALTA_DENSIDAD = 150; // Más de 80 -> iconos minúsculos

    /**
     * Construye un nuevo gestor de iconos.
     *
     * @param context          el contexto de la aplicación para acceder a recursos
     * @param favoritosManager el gestor de favoritos para distinguir iconos de favoritas
     */
    public IconosManager(Context context, FavoritosManager favoritosManager) {
        this.context = context;
        this.favoritosManager = favoritosManager;
    }

    /**
     * Obtiene el recurso de icono apropiado según la densidad de gasolineras y nivel de zoom.
     * <p>
     * El tamaño del icono se ajusta dinámicamente basándose en:
     * <ul>
     * <li>Número total de gasolineras en el viewport actual</li>
     * <li>Nivel de zoom del mapa</li>
     * <li>Estado de favorita de la gasolinera</li>
     * </ul>
     * </p>
     * <p>
     * Los umbrales se ajustan automáticamente según el zoom:
     * <ul>
     * <li>Zoom &lt; 14.0: umbrales más bajos para cambiar a iconos pequeños</li>
     * <li>Zoom &gt;= 14.0: umbrales más altos, permitiendo iconos grandes con más densidad</li>
     * </ul>
     * </p>
     *
     * @param esFavorita                 {@code true} si la gasolinera es favorita
     * @param totalGasolinerasEnViewport número total de gasolineras visibles en el mapa
     * @param zoom                       nivel de zoom actual del mapa
     * @return el ID del recurso drawable del icono apropiado
     */
    public int obtenerIconoGasolinera(boolean esFavorita, int totalGasolinerasEnViewport, double zoom) {
        // Lógica de densidad y zoom que ya teníamos
        int umbralAltaDensidad = (zoom > 14.0) ? 15 : 25;//40 : 70;
        int umbralMuyAltaDensidad = (zoom > 14.0) ? 40 : 80;//120 : 200;

        if (totalGasolinerasEnViewport > umbralMuyAltaDensidad) {
            // Modo de MUY alta densidad - iconos minúsculos
            Log.d("GeoGas", "🎯 Icono TINY - " + totalGasolinerasEnViewport + " gasolineras");
            return esFavorita ? R.drawable.ic_star_tiny : R.drawable.ic_gas_station_tiny;
        } else if (totalGasolinerasEnViewport > umbralAltaDensidad) {
            // Modo de alta densidad - iconos pequeños (ESTE SE VERÁ MÁS)
            Log.d("GeoGas", "🎯 Icono SMALL - " + totalGasolinerasEnViewport + " gasolineras");
            return esFavorita ? R.drawable.ic_star_small : R.drawable.ic_gas_station_small;
        } else {
            // Modo normal - iconos grandes
            Log.d("GeoGas", "🎯 Icono NORMAL - " + totalGasolinerasEnViewport + " gasolineras");
            return esFavorita ? R.drawable.ic_star : R.drawable.ic_gas_station;
        }
    }

    /**
     * Obtiene información descriptiva sobre el tamaño de icono seleccionado.
     * <p>
     * Útil para logging y depuración del sistema de iconos adaptativos.
     * </p>
     *
     * @param totalGasolinerasEnViewport número total de gasolineras visibles
     * @param zoom                       nivel de zoom actual del mapa
     * @return cadena descriptiva del tamaño del icono ("tiny 16dp", "small 24dp", o "normal 48dp")
     */
    public String obtenerInfoTamañoIcono(int totalGasolinerasEnViewport, double zoom) {
        int umbralAltaDensidad = (zoom > 14.0) ? 40 : 70;
        int umbralMuyAltaDensidad = (zoom > 14.0) ? 120 : 200;

        if (totalGasolinerasEnViewport > umbralMuyAltaDensidad) {
            return "tiny (16dp)";
        } else if (totalGasolinerasEnViewport > umbralAltaDensidad) {
            return "small (24dp)";
        } else {
            return "normal (48dp)";
        }
    }

    /**
     * Obtiene información sobre los umbrales de densidad configurados.
     * <p>
     * Proporciona los valores de los umbrales para cambio de tamaño de iconos.
     * Útil para debugging y ajuste de parámetros.
     * </p>
     *
     * @return cadena con información de los umbrales configurados
     */
    public String obtenerInfoUmbrales() {
        return String.format("Umbrales: >%d (small), >%d (tiny)",
                UMBRAL_ALTA_DENSIDAD, UMBRAL_MUY_ALTA_DENSIDAD);
    }
}
