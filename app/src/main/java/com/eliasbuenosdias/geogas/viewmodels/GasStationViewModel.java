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

    private final MutableLiveData<List<GasolineraAPI>> toutesLasGasolineras = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<GasolineraAPI>> gasolinerasVisibles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);

    public LiveData<List<GasolineraAPI>> getGasolinerasVisibles() {
        return gasolinerasVisibles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public void setTodasLasGasolineras(List<GasolineraAPI> lista) {
        thisutesLasGasolineras.setValue(lista);
        gasolinerasVisibles.setValue(lista);
    }

    public void applyFilters(FiltrosManager filtrosManager, FavoritosManager favoritosManager) {
        List<GasolineraAPI> todas = toutesLasGasolineras.getValue();
        if (todas == null)
            return;

        if (filtrosManager == null || !filtrosManager.tieneFiltrosActivos()) {
            gasolinerasVisibles.setValue(todas);
        } else {
            gasolinerasVisibles.setValue(filtrosManager.aplicarFiltros(todas, favoritosManager));
        }
    }

    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    public void setProgress(int p) {
        progress.setValue(p);
    }
}
