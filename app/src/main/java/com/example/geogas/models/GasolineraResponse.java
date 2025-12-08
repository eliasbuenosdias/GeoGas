// GasolineraResponse.java
package com.example.geogas.models;

import java.util.List;
/**
 * Representa la respuesta de la API de Miteco que contiene la lista de estaciones de servicio.
 * Encapsula el objeto JSON devuelto por el servicio REST de precios de carburantes,
 * específicamente el campo "ListaEESSPrecio" que contiene todas las gasolineras.
 *
 * @author Elías Prieto Parrilla
 * @version 1.0
 */
public class GasolineraResponse {
    /**
     * Lista de estaciones de servicio con sus precios y datos de ubicación.
     */
    private List<GasolineraAPI> listaEESSPrecio;
    /**
     * Obtiene la lista de estaciones de servicio.
     *
     * @return la lista de gasolineras con sus datos completos
     */
    public List<GasolineraAPI> getListaEESSPrecio() {
        return listaEESSPrecio;
    }
    /**
     * Establece la lista de estaciones de servicio.
     *
     * @param listaEESSPrecio la lista de gasolineras a establecer
     */
    public void setListaEESSPrecio(List<GasolineraAPI> listaEESSPrecio) {
        this.listaEESSPrecio = listaEESSPrecio;
    }
}