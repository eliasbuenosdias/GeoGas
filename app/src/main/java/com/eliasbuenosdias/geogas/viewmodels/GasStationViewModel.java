package com.eliasbuenosdias.geogas.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.utils.FavoritosManager;
import com.eliasbuenosdias.geogas.utils.FiltrosManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel que gestiona el estado de las gasolineras y filtros.
 * Sigue el principio de Clean Code de separar la lógica de negocio de la UI.
 */
public class GasStationViewModel extends ViewModel {

    private final MutableLiveData<List<GasolineraAPI>> todasLasGasolineras = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<GasolineraAPI>> gasolinerasVisibles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);

    private List<GasolineraAPI> masterList = new ArrayList<>();
    private List<GasolineraAPI> filteredList = new ArrayList<>();
    private final MutableLiveData<List<String>> provincias = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> municipios = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> marcas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>("");
    private final MutableLiveData<org.osmdroid.util.BoundingBox> currentViewport = new MutableLiveData<>();
    private com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras puntuador;

    public LiveData<List<GasolineraAPI>> getGasolinerasVisibles() {
        return gasolinerasVisibles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<List<String>> getProvincias() {
        return provincias;
    }

    public LiveData<List<String>> getMunicipios() {
        return municipios;
    }

    public LiveData<List<String>> getMarcas() {
        return marcas;
    }

    public void setPuntuador(com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras puntuador) {
        this.puntuador = puntuador;
    }

    public void setViewport(org.osmdroid.util.BoundingBox viewport) {
        currentViewport.setValue(viewport);
        updateVisibleGasStations();
    }

    public void loadData(com.eliasbuenosdias.geogas.api.MitecoApiService apiService) {
        isLoading.setValue(true);
        progress.setValue(10);
        statusMessage.setValue("Cargando gasolineras...");

        apiService.obtenerGasolineras().enqueue(new retrofit2.Callback<com.google.gson.JsonElement>() {
            @Override
            public void onResponse(retrofit2.Call<com.google.gson.JsonElement> call,
                    retrofit2.Response<com.google.gson.JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    progress.setValue(50);
                    statusMessage.setValue("Procesando datos...");
                    parseJson(response.body());
                    progress.setValue(100);
                    isLoading.setValue(false);
                } else {
                    statusMessage.setValue("Error en respuesta del servidor");
                    isLoading.setValue(false);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.google.gson.JsonElement> call, Throwable t) {
                statusMessage.setValue("Error de conexión: " + t.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    private void parseJson(com.google.gson.JsonElement json) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            List<GasolineraAPI> lista = new ArrayList<>();

            if (json.isJsonArray()) {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<GasolineraAPI>>() {
                }.getType();
                lista = gson.fromJson(json, listType);
            } else if (json.isJsonObject()) {
                com.google.gson.JsonObject obj = json.getAsJsonObject();
                String[] fields = { "ListaEESSPrecio", "listaEESSPrecio", "data" };
                for (String field : fields) {
                    if (obj.has(field) && obj.get(field).isJsonArray()) {
                        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<GasolineraAPI>>() {
                        }.getType();
                        lista = gson.fromJson(obj.get(field), listType);
                        break;
                    }
                }
            }

            if (lista != null) {
                masterList = new ArrayList<>(lista);
                filteredList = new ArrayList<>(lista);
                updateVisibleGasStations();
                actualizarListasAutocompletado(lista);
            }
        } catch (Exception e) {
            statusMessage.postValue("Error al procesar datos");
        }
    }

    private void updateVisibleGasStations() {
        List<GasolineraAPI> dataToFilter = filteredList;
        org.osmdroid.util.BoundingBox viewport = currentViewport.getValue();

        if (dataToFilter == null)
            return;
        if (viewport == null) {
            gasolinerasVisibles.postValue(dataToFilter);
            return;
        }

        List<GasolineraAPI> resultList = new ArrayList<>();
        for (GasolineraAPI g : dataToFilter) {
            try {
                double lat = Double.parseDouble(g.getLatitud().replace(",", "."));
                double lon = Double.parseDouble(g.getLongitud().replace(",", "."));
                if (viewport.contains(lat, lon))
                    resultList.add(g);
            } catch (Exception ignored) {
            }
        }

        if (puntuador != null) {
            List<com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras.GasolineraPuntuada> puntuadas = puntuador
                    .ordenarPorPuntuacion(resultList);
            List<GasolineraAPI> result = new ArrayList<>();
            int limit = Math.min(200, puntuadas.size());
            for (int i = 0; i < limit; i++)
                result.add(puntuadas.get(i).gasolinera);
            gasolinerasVisibles.postValue(result);
        } else {
            gasolinerasVisibles.postValue(resultList);
        }
    }

    private void actualizarListasAutocompletado(List<GasolineraAPI> lista) {
        java.util.Set<String> setProvincias = new java.util.HashSet<>();
        java.util.Set<String> setMunicipios = new java.util.HashSet<>();
        java.util.Set<String> setMarcas = new java.util.HashSet<>();

        for (GasolineraAPI g : lista) {
            if (g.getProvincia() != null && !g.getProvincia().isEmpty())
                setProvincias.add(g.getProvincia());
            if (g.getMunicipio() != null && !g.getMunicipio().isEmpty())
                setMunicipios.add(g.getMunicipio());
            if (g.getRotulo() != null && !g.getRotulo().isEmpty())
                setMarcas.add(g.getRotulo());
        }

        List<String> listP = new ArrayList<>(setProvincias);
        List<String> listM = new ArrayList<>(setMunicipios);
        List<String> listR = new ArrayList<>(setMarcas);

        java.util.Collections.sort(listP);
        java.util.Collections.sort(listM);
        java.util.Collections.sort(listR);

        provincias.postValue(listP);
        municipios.postValue(listM);
        marcas.postValue(listR);
    }

    public void applyFilters(FiltrosManager filtrosManager, FavoritosManager favoritosManager) {
        if (filtrosManager == null || !filtrosManager.tieneFiltrosActivos()) {
            filteredList = new ArrayList<>(masterList);
        } else {
            filteredList = filtrosManager.aplicarFiltros(masterList, favoritosManager);
        }
        updateVisibleGasStations();
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    public void setProgress(int p) {
        progress.setValue(p);
    }
}
