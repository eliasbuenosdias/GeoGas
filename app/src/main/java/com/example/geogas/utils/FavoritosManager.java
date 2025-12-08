package com.example.geogas.utils;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;
    /**
     * Gestor de gasolineras favoritas utilizando SharedPreferences.
     * <p>
     * Esta clase proporciona funcionalidad para gestionar las gasolineras marcadas
     * como favoritas por el usuario, almacenando los identificadores en SharedPreferences.
     * Implementa un sistema corregido que evita problemas de mutabilidad con StringSet.
     * </p>
     *
     * @author Elías Prieto Parrilla
     * @version 1.0
     */
    public class FavoritosManager {
        /**
         * Nombre del archivo de preferencias compartidas.
         */
        private static final String PREFS_NAME = "GeoGasFavoritos";
        /**
         * Clave para almacenar el conjunto de favoritos.
         */
        private static final String KEY_FAVORITOS = "favoritos";
        /**
         * Instancia de SharedPreferences para persistencia de datos.
         */
        private SharedPreferences sharedPreferences;
        /**
         * Construye un nuevo FavoritosManager.
         *
         * @param context el contexto de la aplicación para acceder a SharedPreferences
         */
        public FavoritosManager(Context context) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        /**
         * Alterna el estado de favorito de una gasolinera.
         * <p>
         * Si la gasolinera ya está marcada como favorita, la elimina. Si no lo está,
         * la añade. Crea siempre un nuevo HashSet para evitar problemas de SharedPreferences
         * con conjuntos mutables.
         * </p>
         *
         * @param gasolineraId el identificador único de la gasolinera
         */
        public void toggleFavorito(String gasolineraId) {
            // Crear un NUEVO HashSet cada vez (no reutilizar el obtenido)
            Set<String> favoritos = new HashSet<>(getFavoritos());

            if (favoritos.contains(gasolineraId)) {
                favoritos.remove(gasolineraId);
            } else {
                favoritos.add(gasolineraId);
            }

            // Guardar el NUEVO conjunto
            sharedPreferences.edit()
                    .putStringSet(KEY_FAVORITOS, new HashSet<>(favoritos)) // Siempre nuevo HashSet
                    .apply();
        }

        /**
         * Verifica si una gasolinera está marcada como favorita.
         *
         * @param gasolineraId el identificador único de la gasolinera
         * @return {@code true} si la gasolinera es favorita, {@code false} en caso contrario
         */
        public boolean esFavorita(String gasolineraId) {
            return getFavoritos().contains(gasolineraId);
        }

        /**
         * Obtiene todos los identificadores de gasolineras favoritas.
         * <p>
         * Devuelve siempre una nueva copia del conjunto para evitar problemas
         * de mutabilidad con SharedPreferences.
         * </p>
         *
         * @return un conjunto con todos los identificadores de gasolineras favoritas
         */
        public Set<String> getFavoritos() {
            // Siempre crear un nuevo HashSet a partir del obtenido
            return new HashSet<>(sharedPreferences.getStringSet(KEY_FAVORITOS, new HashSet<>()));
        }

        /**
         * Elimina todas las gasolineras favoritas.
         * <p>
         * Este método es útil para pruebas o para reiniciar la lista de favoritos.
         * </p>
         */
        public void limpiarFavoritos() {
            sharedPreferences.edit()
                    .putStringSet(KEY_FAVORITOS, new HashSet<>())
                    .apply();
        }
    }