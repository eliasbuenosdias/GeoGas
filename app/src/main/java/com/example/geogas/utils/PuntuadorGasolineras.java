
package com.example.geogas.utils;

import com.example.geogas.models.GasolineraAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculador de puntuaciones de relevancia para gasolineras.
 * <p>
 * Esta clase implementa el patrón Strategy para calcular puntuaciones de relevancia
 * de gasolineras basándose en múltiples criterios ponderados: precios competitivos,
 * variedad de combustibles y estado de favoritos. Las puntuaciones se utilizan para
 * priorizar qué gasolineras mostrar cuando hay alta densidad en el mapa.
 * </p>
 *
 * @author Elías Prieto Parrilla
 * @version 1.0
 */
public class PuntuadorGasolineras {

    private FavoritosManager favoritosManager;

    // Pesos configurables para los criterios
    private double pesoPrecios = 0.2;      // 60% para precios
    private double pesoVariedad = 0.4;     // 20% para variedad
    private double pesoFavoritos = 0.4;    // 20% para favoritos

    /**
     * Construye un nuevo puntuador de gasolineras.
     *
     * @param favoritosManager el gestor de favoritos para considerar preferencias del usuario
     */
    public PuntuadorGasolineras(FavoritosManager favoritosManager) {
        this.favoritosManager = favoritosManager;
    }

    /**
     * Clase interna que representa una gasolinera con su puntuación calculada.
     * <p>
     * Facilita el ordenamiento y selección de gasolineras por relevancia.
     * </p>
     */
    public static class GasolineraPuntuada {
        /**
         * The Gasolinera.
         */
        public GasolineraAPI gasolinera;
        /**
         * The Puntuacion.
         */
        public double puntuacion;

        /**
         * Construye una gasolinera puntuada.
         *
         * @param gasolinera la gasolinera
         * @param puntuacion la puntuación calculada
         */
        public GasolineraPuntuada(GasolineraAPI gasolinera, double puntuacion) {
            this.gasolinera = gasolinera;
            this.puntuacion = puntuacion;
        }
    }

    /**
     * Calcula la puntuación total de relevancia de una gasolinera.
     * <p>
     * La puntuación combina tres factores ponderados:
     * <ul>
     * <li>Precios: Las gasolineras con precios más bajos obtienen mayor puntuación</li>
     * <li>Variedad: Mayor variedad de combustibles aumenta la puntuación</li>
     * <li>Favoritos: Las gasolineras marcadas como favoritas reciben bonificación</li>
     * </ul>
     * </p>
     * <p>
     * La puntuación total está normalizada en el rango [0.0, 1.0].
     * </p>
     *
     * @param gasolinera la gasolinera a puntuar
     * @return la puntuación total calculada (0.0 a 1.0)
     */
    public double calcularPuntuacion(GasolineraAPI gasolinera) {
        double puntuacion = 0.0;

        puntuacion += calcularPuntuacionPrecios(gasolinera) * pesoPrecios;
        puntuacion += calcularPuntuacionVariedad(gasolinera) * pesoVariedad;
        puntuacion += calcularPuntuacionFavoritos(gasolinera) * pesoFavoritos;

        return puntuacion;
    }
    /**
     * Calcula la puntuación basada en precios de combustibles.
     * <p>
     * Los precios se normalizan asumiendo rangos típicos:
     * <ul>
     * <li>Gasolinas: 1.0€ - 2.0€ por litro</li>
     * <li>GLP: 0.5€ - 1.5€ por litro</li>
     * </ul>
     * </p>
     * <p>
     * Mayor puntuación para precios más bajos. La puntuación se promedia entre
     * todos los combustibles disponibles.
     * </p>
     *
     * @param gasolinera la gasolinera a evaluar
     * @return puntuación de precios normalizada (0.0 a 1.0)
     */
    private double calcularPuntuacionPrecios(GasolineraAPI gasolinera) {
        double puntuacionPrecios = 0.0;
        int combustiblesValidos = 0;

        try {
            // Gasolina 95 - Normalizado entre 0-1 (precios típicos 1.0-2.0)
            if (gasolinera.getPrecioGasolina95() != null && !gasolinera.getPrecioGasolina95().isEmpty()) {
                double precio = Double.parseDouble(gasolinera.getPrecioGasolina95().replace(",", "."));
                double puntuacion95 = Math.max(0, 2.0 - precio) / 2.0;
                puntuacionPrecios += puntuacion95;
                combustiblesValidos++;
            }

            // Diésel
            if (gasolinera.getPrecioGasoleoA() != null && !gasolinera.getPrecioGasoleoA().isEmpty()) {
                double precio = Double.parseDouble(gasolinera.getPrecioGasoleoA().replace(",", "."));
                double puntuacionDiesel = Math.max(0, 2.0 - precio) / 2.0;
                puntuacionPrecios += puntuacionDiesel;
                combustiblesValidos++;
            }

            // GLP (suele ser más barato)
            if (gasolinera.getPrecioGLP() != null && !gasolinera.getPrecioGLP().isEmpty()) {
                double precio = Double.parseDouble(gasolinera.getPrecioGLP().replace(",", "."));
                double puntuacionGLP = Math.max(0, 1.5 - precio) / 1.5;
                puntuacionPrecios += puntuacionGLP;
                combustiblesValidos++;
            }

        } catch (NumberFormatException e) {
            // Error parseando precios, no sumar a la puntuación
        }

        return combustiblesValidos > 0 ? puntuacionPrecios / combustiblesValidos : 0;
    }

    /**
     * Calcula la puntuación basada en variedad de combustibles disponibles.
     * <p>
     * Se consideran los combustibles principales: Gasolina 95, Gasolina 98,
     * Diesel y GLP. La puntuación se normaliza asumiendo un máximo de 3 tipos
     * de combustible como óptimo.
     * </p>
     *
     * @param gasolinera la gasolinera a evaluar
     * @return puntuación de variedad normalizada (0.0 a 1.0)
     */
    private double calcularPuntuacionVariedad(GasolineraAPI gasolinera) {
        int tiposCombustible = 0;

        if (gasolinera.getPrecioGasolina95() != null && !gasolinera.getPrecioGasolina95().isEmpty()) tiposCombustible++;
        if (gasolinera.getPrecioGasoleoA() != null && !gasolinera.getPrecioGasoleoA().isEmpty()) tiposCombustible++;
        if (gasolinera.getPrecioGLP() != null && !gasolinera.getPrecioGLP().isEmpty()) tiposCombustible++;
        if (gasolinera.getPrecioGasolina98() != null && !gasolinera.getPrecioGasolina98().isEmpty()) tiposCombustible++;

        // Normalizado: 0-1 donde 3+ combustibles = máxima puntuación
        return Math.min(1.0, tiposCombustible / 3.0);
    }
    /**
     * Calcula la puntuación por estado de favorita.
     * <p>
     * Las gasolineras marcadas como favoritas reciben la puntuación máxima (1.0),
     * mientras que las no favoritas reciben 0.0.
     * </p>
     *
     * @param gasolinera la gasolinera a evaluar
     * @return 1.0 si es favorita, 0.0 en caso contrario
     */
    private double calcularPuntuacionFavoritos(GasolineraAPI gasolinera) {
        return (gasolinera.getId() != null && favoritosManager.esFavorita(gasolinera.getId())) ? 1.0 : 0.0;
    }

    /**
     * Ordena una lista de gasolineras por puntuación de mayor a menor.
     * <p>
     * Calcula la puntuación de cada gasolinera y las ordena descendentemente.
     * Útil para seleccionar las gasolineras más relevantes cuando hay que
     * limitar el número de marcadores en el mapa.
     * </p>
     *
     * @param gasolineras la lista de gasolineras a ordenar
     * @return lista de gasolineras con sus puntuaciones, ordenadas de mayor a menor
     */
    public List<GasolineraPuntuada> ordenarPorPuntuacion(List<GasolineraAPI> gasolineras) {
        List<GasolineraPuntuada> gasolinerasPuntuadas = new ArrayList<>();

        for (GasolineraAPI gasolinera : gasolineras) {
            double puntuacion = calcularPuntuacion(gasolinera);
            gasolinerasPuntuadas.add(new GasolineraPuntuada(gasolinera, puntuacion));
        }

        // Ordenar de mayor a menor puntuación
        gasolinerasPuntuadas.sort((gp1, gp2) -> Double.compare(gp2.puntuacion, gp1.puntuacion));

        return gasolinerasPuntuadas;
    }

    /**
     * Obtiene el peso actual del criterio de precios.
     *
     * @return el peso de precios (0.0 a 1.0)
     */
// Getters y Setters para los pesos (por si queremos ajustarlos dinámicamente)
    public double getPesoPrecios() { return pesoPrecios; }

    /**
     * Establece el peso del criterio de precios.
     *
     * @param pesoPrecios el nuevo peso (0.0 a 1.0)
     */
    public void setPesoPrecios(double pesoPrecios) { this.pesoPrecios = pesoPrecios; }

    /**
     * Obtiene el peso actual del criterio de variedad.
     *
     * @return el peso de variedad (0.0 a 1.0)
     */
    public double getPesoVariedad() { return pesoVariedad; }

    /**
     * Establece el peso del criterio de variedad.
     *
     * @param pesoVariedad el nuevo peso (0.0 a 1.0)
     */
    public void setPesoVariedad(double pesoVariedad) { this.pesoVariedad = pesoVariedad; }

    /**
     * Obtiene el peso actual del criterio de favoritos.
     *
     * @return el peso de favoritos (0.0 a 1.0)
     */
    public double getPesoFavoritos() { return pesoFavoritos; }

    /**
     * Establece el peso del criterio de favoritos.
     *
     * @param pesoFavoritos el nuevo peso (0.0 a 1.0)
     */
    public void setPesoFavoritos(double pesoFavoritos) { this.pesoFavoritos = pesoFavoritos; }
}