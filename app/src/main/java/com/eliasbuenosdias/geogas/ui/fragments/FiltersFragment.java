package com.eliasbuenosdias.geogas.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eliasbuenosdias.geogas.R;
import com.eliasbuenosdias.geogas.utils.FiltrosManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento que gestiona el panel lateral de filtros.
 * Aplica los principios de Clean Code separando la lógica de UI de filtros de
 * la actividad principal.
 */
public class FiltersFragment extends Fragment {

    private FiltrosManager filtrosManager;
    private FiltersListener listener;
    private com.eliasbuenosdias.geogas.viewmodels.GasStationViewModel viewModel;

    private AutoCompleteTextView filterProvincia, filterMunicipio, filterGasolinera;
    private CheckBox filterGasolina95, filterGasolina98, filterDiesel, filterDieselPremium, filterGLP;
    private CheckBox filter24h, filterFavoritas;
    private EditText filterPrecioMaxGasolina95, filterPrecioMaxDiesel;
    private Button btnApplyFilters, btnClearFilters;
    private TextView filterResultsCount;

    public interface FiltersListener {
        void onFiltersApplied();

        void onFiltersCleared();

        void onCloseFilters();
    }

    public void setFiltersListener(FiltersListener listener) {
        this.listener = listener;
    }

    public void setFiltrosManager(FiltrosManager filtrosManager) {
        this.filtrosManager = filtrosManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.filters_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity())
                .get(com.eliasbuenosdias.geogas.viewmodels.GasStationViewModel.class);
        initializeViews(view);
        setupListeners();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getProvincias().observe(getViewLifecycleOwner(), list -> setupAdapter(filterProvincia, list));
        viewModel.getMunicipios().observe(getViewLifecycleOwner(), list -> setupAdapter(filterMunicipio, list));
        viewModel.getMarcas().observe(getViewLifecycleOwner(), list -> setupAdapter(filterGasolinera, list));
        viewModel.getGasolinerasVisibles().observe(getViewLifecycleOwner(),
                list -> setResultsCount(list != null ? list.size() : 0));
    }

    private void setupAdapter(AutoCompleteTextView view, List<String> data) {
        if (getContext() == null || data == null)
            return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line,
                data);
        view.setAdapter(adapter);
    }

    private void initializeViews(View view) {
        filterProvincia = view.findViewById(R.id.filter_provincia);
        filterMunicipio = view.findViewById(R.id.filter_municipio);
        filterGasolinera = view.findViewById(R.id.filter_gasolinera);
        filterGasolina95 = view.findViewById(R.id.filter_gasolina95);
        filterGasolina98 = view.findViewById(R.id.filter_gasolina98);
        filterDiesel = view.findViewById(R.id.filter_diesel);
        filterDieselPremium = view.findViewById(R.id.filter_diesel_premium);
        filterGLP = view.findViewById(R.id.filter_glp);
        filter24h = view.findViewById(R.id.filter_24h);
        filterFavoritas = view.findViewById(R.id.filter_favoritas);
        filterPrecioMaxGasolina95 = view.findViewById(R.id.filter_precio_max_gasolina95);
        filterPrecioMaxDiesel = view.findViewById(R.id.filter_precio_max_diesel);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
        btnClearFilters = view.findViewById(R.id.btn_clear_filters);
        filterResultsCount = view.findViewById(R.id.filter_results_count);

        ImageButton btnCloseFilters = view.findViewById(R.id.btn_close_filters);
        if (btnCloseFilters != null) {
            btnCloseFilters.setOnClickListener(v -> {
                if (listener != null)
                    listener.onCloseFilters();
            });
        }
    }

    private void setupListeners() {
        if (btnApplyFilters != null) {
            btnApplyFilters.setOnClickListener(v -> {
                updateFiltrosFromUI();
                if (listener != null)
                    listener.onFiltersApplied();
            });
        }

        if (btnClearFilters != null) {
            btnClearFilters.setOnClickListener(v -> {
                clearUI();
                if (listener != null)
                    listener.onFiltersCleared();
            });
        }
    }

    private void updateFiltrosFromUI() {
        if (filtrosManager == null)
            return;

        filtrosManager.setProvincia(filterProvincia.getText().toString().trim());
        filtrosManager.setMunicipio(filterMunicipio.getText().toString().trim());
        filtrosManager.setGasolinera(filterGasolinera.getText().toString().trim());

        filtrosManager.setSoloGasolina95(filterGasolina95.isChecked());
        filtrosManager.setSoloGasolina98(filterGasolina98.isChecked());
        filtrosManager.setSoloDiesel(filterDiesel.isChecked());
        filtrosManager.setSoloDieselPremium(filterDieselPremium.isChecked());
        filtrosManager.setSoloGLP(filterGLP.isChecked());

        filtrosManager.setSolo24Horas(filter24h.isChecked());
        filtrosManager.setSoloFavoritas(filterFavoritas.isChecked());

        try {
            String p95 = filterPrecioMaxGasolina95.getText().toString();
            filtrosManager.setPrecioMaxGasolina95(p95.isEmpty() ? null : Double.parseDouble(p95));
        } catch (NumberFormatException e) {
            filtrosManager.setPrecioMaxGasolina95(null);
        }

        try {
            String pd = filterPrecioMaxDiesel.getText().toString();
            filtrosManager.setPrecioMaxDiesel(pd.isEmpty() ? null : Double.parseDouble(pd));
        } catch (NumberFormatException e) {
            filtrosManager.setPrecioMaxDiesel(null);
        }
    }

    private void clearUI() {
        filterProvincia.setText("");
        filterMunicipio.setText("");
        filterGasolinera.setText("");
        filterGasolina95.setChecked(false);
        filterGasolina98.setChecked(false);
        filterDiesel.setChecked(false);
        filterDieselPremium.setChecked(false);
        filterGLP.setChecked(false);
        filter24h.setChecked(false);
        filterFavoritas.setChecked(false);
        filterPrecioMaxGasolina95.setText("");
        filterPrecioMaxDiesel.setText("");

        if (filtrosManager != null) {
            filtrosManager.limpiarFiltros();
        }
    }

    public void setResultsCount(int count) {
        if (filterResultsCount != null) {
            filterResultsCount.setText(count + " gasolineras encontradas");
        }
    }
}
