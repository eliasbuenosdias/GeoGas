package com.eliasbuenosdias.geogas.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.eliasbuenosdias.geogas.api.MitecoApiService;
import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.utils.FavoritosManager;
import com.eliasbuenosdias.geogas.utils.FiltrosEstado;
import com.eliasbuenosdias.geogas.utils.FiltrosManager;
import com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras;
import com.eliasbuenosdias.geogas.utils.command.AplicarFiltroCommand;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.util.BoundingBox;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel que gestiona el estado de las gasolineras y filtros.
 * Sigue el principio de Clean Code de separar la lógica de negocio de la UI.
 */
public class GasStationViewModel extends ViewModel {

    private static final int MAX_VISIBLE_MARKERS = 200;
    private static final int MAX_HISTORIAL_UNDO = 10;

    private final MutableLiveData<List<GasolineraAPI>> todasLasGasolineras = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<GasolineraAPI>> gasolinerasVisibles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<LoadState> loadState = new MutableLiveData<>(new LoadState(LoadState.Status.LOADING, 0, ""));

    private List<GasolineraAPI> masterList = new ArrayList<>();
    private List<GasolineraAPI> filteredList = new ArrayList<>();
    private final MutableLiveData<List<String>> provincias = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> municipios = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> marcas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<BoundingBox> currentViewport = new MutableLiveData<>();
    private PuntuadorGasolineras puntuador;
    private FiltrosManager filtrosManager;
    private final Deque<AplicarFiltroCommand> historialUndo = new ArrayDeque<>();
    private int currentProgress = 0;

    public void setFiltrosManager(FiltrosManager filtrosManager) {
        this.filtrosManager = filtrosManager;
    }

    public LiveData<List<GasolineraAPI>> getGasolinerasVisibles() {
        return gasolinerasVisibles;
    }

    public LiveData<LoadState> getLoadState() {
        return loadState;
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

    public void setPuntuador(PuntuadorGasolineras puntuador) {
        this.puntuador = puntuador;
    }

    public void setViewport(BoundingBox viewport) {
        currentViewport.setValue(viewport);
        updateVisibleGasStations();
    }

    public void loadData(MitecoApiService apiService) {
        updateState(LoadState.Status.LOADING, 10, "status_loading");

        apiService.obtenerGasolineras().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateState(LoadState.Status.LOADING, 50, "status_processing");
                    parseJson(response.body());
                    updateState(LoadState.Status.LOADED, 100, "");
                } else {
                    updateState(LoadState.Status.ERROR, "status_error_server");
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                updateState(LoadState.Status.ERROR, "status_error_connection");
            }
        });
    }

    private void parseJson(JsonElement json) {
        try {
            Gson gson = new Gson();
            List<GasolineraAPI> lista = new ArrayList<>();

            if (json.isJsonArray()) {
                Type listType = new TypeToken<List<GasolineraAPI>>() {
                }.getType();
                lista = gson.fromJson(json, listType);
            } else if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                String[] fields = { "ListaEESSPrecio", "listaEESSPrecio", "data" };
                for (String field : fields) {
                    if (obj.has(field) && obj.get(field).isJsonArray()) {
                        Type listType = new TypeToken<List<GasolineraAPI>>() {
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
            updateState(LoadState.Status.ERROR, "status_error_parsing");
        }
    }

    private void updateVisibleGasStations() {
        List<GasolineraAPI> dataToFilter = filteredList;
        BoundingBox viewport = currentViewport.getValue();

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
            List<PuntuadorGasolineras.GasolineraPuntuada> puntuadas = puntuador
                    .ordenarPorPuntuacion(resultList);
            List<GasolineraAPI> result = new ArrayList<>();
            int limit = Math.min(MAX_VISIBLE_MARKERS, puntuadas.size());
            for (int i = 0; i < limit; i++)
                result.add(puntuadas.get(i).gasolinera);
            gasolinerasVisibles.postValue(result);
        } else {
            gasolinerasVisibles.postValue(resultList);
        }
    }

    private void actualizarListasAutocompletado(List<GasolineraAPI> lista) {
        Set<String> setProvincias = new HashSet<>();
        Set<String> setMunicipios = new HashSet<>();
        Set<String> setMarcas = new HashSet<>();

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

        Collections.sort(listP);
        Collections.sort(listM);
        Collections.sort(listR);

        provincias.postValue(listP);
        municipios.postValue(listM);
        marcas.postValue(listR);
    }

    public void applyFilters(FiltrosManager filtrosManager, FavoritosManager favoritosManager) {
        this.filtrosManager = filtrosManager;
        refiltrar(favoritosManager);
    }

    public void aplicarFiltros(FiltrosEstado estadoNuevo, FavoritosManager favoritosManager) {
        if (filtrosManager == null)
            return;
        AplicarFiltroCommand comando = new AplicarFiltroCommand(filtrosManager, estadoNuevo,
                () -> refiltrar(favoritosManager));
        comando.execute();
        historialUndo.push(comando);
        if (historialUndo.size() > MAX_HISTORIAL_UNDO) {
            historialUndo.removeLast();
        }
    }

    public boolean deshacerUltimoFiltro(FavoritosManager favoritosManager) {
        AplicarFiltroCommand comando = historialUndo.poll();
        if (comando == null)
            return false;
        comando.undo();
        return true;
    }

    public boolean puedeDeshacer() {
        return !historialUndo.isEmpty();
    }

    private void refiltrar(FavoritosManager favoritosManager) {
        if (filtrosManager == null || !filtrosManager.tieneFiltrosActivos()) {
            filteredList = new ArrayList<>(masterList);
        } else {
            filteredList = filtrosManager.aplicarFiltros(masterList, favoritosManager);
        }
        updateVisibleGasStations();
    }

    private void updateState(LoadState.Status status, String statusKey) {
        updateState(status, currentProgress, statusKey);
    }

    private void updateState(LoadState.Status status, int progress, String statusKey) {
        currentProgress = progress;
        loadState.setValue(new LoadState(status, progress, statusKey));
    }
}