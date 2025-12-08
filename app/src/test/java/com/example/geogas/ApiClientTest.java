package com.example.geogas;

import android.util.Log;
import org.junit.Test;
import org.mockito.MockedStatic;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.example.geogas.api.ApiClient;

/**
 * PRUEBAS UNITARIAS PARA: ApiClient
 *
 * Esta clase prueba el cliente HTTP singleton (Retrofit).
 */
public class ApiClientTest {

    @Test
    public void testGetClient_NoRetornaNull() {
        // Mock Log para evitar "Method not mocked"
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

            Retrofit retrofit = ApiClient.getClient();
            assertNotNull(retrofit);
        }
    }

    @Test
    public void testGetClient_MismaInstanciaSingleton() {
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

            Retrofit retrofit1 = ApiClient.getClient();
            Retrofit retrofit2 = ApiClient.getClient();

            // Verificar que es la misma instancia (singleton)
            assertSame(retrofit1, retrofit2);
        }
    }

    @Test
    public void testGetClient_ConfiguracionCorrecta() {
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

            Retrofit retrofit = ApiClient.getClient();

            // Verificar que tiene el converter factory correcto
            boolean tieneGsonConverter = false;
            for (retrofit2.Converter.Factory factory : retrofit.converterFactories()) {
                if (factory instanceof GsonConverterFactory) {
                    tieneGsonConverter = true;
                    break;
                }
            }

            assertTrue("Debería tener GsonConverterFactory", tieneGsonConverter);

            // Verificar la URL base (solo podemos verificar que no es null)
            assertNotNull(retrofit.baseUrl());
            assertNotNull(retrofit.baseUrl().toString());
        }
    }

    @Test
    public void testBaseUrlCorrecta() {
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.d(anyString(), anyString())).thenReturn(0);

            Retrofit retrofit = ApiClient.getClient();
            String baseUrl = retrofit.baseUrl().toString();

            // La URL debe terminar con /
            assertTrue("URL debe terminar con /", baseUrl.endsWith("/"));

            // Debe contener el dominio del ministerio
            assertTrue("URL debe contener minetur.gob.es",
                    baseUrl.contains("minetur.gob.es"));
        }
    }
}