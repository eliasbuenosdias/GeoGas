// MitecoApiService.java
package com.example.geogas.api;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
/**
 * Servicio de la API de Miteco para realizar peticiones HTTP a los endpoints de carburantes.
 * Define los métodos de acceso a los servicios REST del Ministerio para la Transición Ecológica.
 *
 * @author Elías Prieto Parrilla
 * @version 1.0
 */
public interface MitecoApiService {
    /**
     * Obtiene la lista completa de estaciones terrestres de servicio.
     * Realiza una petición GET al endpoint de estaciones terrestres y devuelve
     * los datos en formato JSON.
     *
     * @return llamada asíncrona que contiene el JSON con la información de todas las gasolineras
     */
    @GET("EstacionesTerrestres/")
    Call<JsonElement> obtenerGasolineras();
}