package com.eliasbuenosdias.geogas.utils;

import android.content.Context;

import com.eliasbuenosdias.geogas.R;

/**
 * Gestor de iconos adaptativos para marcadores de gasolineras en el mapa.
 * <p>
 * Selecciona el tamaño del icono según la densidad de gasolineras visibles
 * y el nivel de zoom del mapa, distinguiendo además entre favoritas y normales.
 * </p>
 */
public class IconosManager {

    private static final double ZOOM_ALTO = 14.0;
    private static final int UMBRAL_ALTA_DENSIDAD_ZOOM_ALTO = 15;
    private static final int UMBRAL_ALTA_DENSIDAD_ZOOM_BAJO = 25;
    private static final int UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_ALTO = 40;
    private static final int UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_BAJO = 80;

    private final Context context;
    private final FavoritosManager favoritosManager;

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
     *
     * @param esFavorita                 {@code true} si la gasolinera es favorita
     * @param totalGasolinerasEnViewport número total de gasolineras visibles en el mapa
     * @param zoom                       nivel de zoom actual del mapa
     * @return el ID del recurso drawable del icono apropiado
     */
    public int obtenerIconoGasolinera(boolean esFavorita, int totalGasolinerasEnViewport, double zoom) {
        int umbralAltaDensidad = (zoom > ZOOM_ALTO) ? UMBRAL_ALTA_DENSIDAD_ZOOM_ALTO : UMBRAL_ALTA_DENSIDAD_ZOOM_BAJO;
        int umbralMuyAltaDensidad = (zoom > ZOOM_ALTO) ? UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_ALTO : UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_BAJO;

        if (totalGasolinerasEnViewport > umbralMuyAltaDensidad) {
            return esFavorita ? R.drawable.ic_star_tiny : R.drawable.ic_gas_station_tiny;
        } else if (totalGasolinerasEnViewport > umbralAltaDensidad) {
            return esFavorita ? R.drawable.ic_star_small : R.drawable.ic_gas_station_small;
        } else {
            return esFavorita ? R.drawable.ic_star : R.drawable.ic_gas_station;
        }
    }

    /**
     * Obtiene información descriptiva sobre el tamaño de icono seleccionado.
     *
     * @param totalGasolinerasEnViewport número total de gasolineras visibles
     * @param zoom                       nivel de zoom actual del mapa
     * @return cadena descriptiva del tamaño del icono ("tiny 16dp", "small 24dp", o "normal 48dp")
     */
    public String obtenerInfoTamañoIcono(int totalGasolinerasEnViewport, double zoom) {
        int umbralAltaDensidad = (zoom > ZOOM_ALTO) ? UMBRAL_ALTA_DENSIDAD_ZOOM_ALTO : UMBRAL_ALTA_DENSIDAD_ZOOM_BAJO;
        int umbralMuyAltaDensidad = (zoom > ZOOM_ALTO) ? UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_ALTO : UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_BAJO;

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
     *
     * @return cadena con información de los umbrales configurados
     */
    public String obtenerInfoUmbrales() {
        return String.format("Umbrales zoom alto: >%d (small), >%d (tiny); zoom bajo: >%d (small), >%d (tiny)",
                UMBRAL_ALTA_DENSIDAD_ZOOM_ALTO, UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_ALTO,
                UMBRAL_ALTA_DENSIDAD_ZOOM_BAJO, UMBRAL_MUY_ALTA_DENSIDAD_ZOOM_BAJO);
    }
}