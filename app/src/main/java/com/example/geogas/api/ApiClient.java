
package com.example.geogas.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente API para realizar peticiones HTTP a los servicios REST de carburantes del Ministerio.
 * Proporciona una instancia singleton de Retrofit configurada con la URL base y el convertidor JSON.
 *
 * @author Elías Prieto Parrilla
 * @version 1.0
 */
public class ApiClient {
    /**
     * URL base de los servicios REST de precios de carburantes del Ministerio.
     */
    private static final String BASE_URL = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/";
    /**
     * Instancia singleton de Retrofit.
     */
    private static Retrofit retrofit = null;
    /**
     * Obtiene la instancia singleton de Retrofit.
     * Si no existe una instancia previa, crea una nueva configurada con la URL base
     * y el convertidor Gson para procesar respuestas JSON.
     *
     * @return la instancia de Retrofit configurada para acceder a la API de carburantes
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}