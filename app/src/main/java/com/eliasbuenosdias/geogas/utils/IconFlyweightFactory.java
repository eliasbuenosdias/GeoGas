package com.eliasbuenosdias.geogas.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Caché compartido de Drawables (patrón Flyweight).
 * <p>
 * Los marcadores del mapa reutilizan un único Drawable por recurso en lugar de
 * crear una instancia por marcador, reduciendo el uso de memoria en mapas con
 * cientos de gasolineras (solo 6 iconos distintos posibles).
 * </p>
 */
public final class IconFlyweightFactory {

    private static final Map<Integer, Drawable> CACHE = new HashMap<>();

    private IconFlyweightFactory() {
    }

    /**
     * Obtiene el Drawable correspondiente al recurso, reutilizando la instancia en caché.
     *
     * @param context el contexto de la aplicación para cargar el recurso
     * @param resId   el ID del recurso drawable
     * @return el Drawable (compartido entre todos los consumidores del recurso)
     */
    public static Drawable get(Context context, int resId) {
        return CACHE.computeIfAbsent(resId, id -> ContextCompat.getDrawable(context, id));
    }
}