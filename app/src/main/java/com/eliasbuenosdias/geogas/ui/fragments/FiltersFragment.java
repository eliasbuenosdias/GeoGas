package com.eliasbuenosdias.geogas.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.eliasbuenosdias.geogas.R;
import com.eliasbuenosdias.geogas.utils.FiltrosEstado;
import com.eliasbuenosdias.geogas.utils.FiltrosManager;
import com.eliasbuenosdias.geogas.viewmodels.GasStationViewModel;
import com.google.android.material.chip.Chip;

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
    private GasStationViewModel viewModel;

    private AutoCompleteTextView filterProvincia, filterMunicipio, filterGasolinera;
    private Chip filterGasolina95, filterGasolina98, filterDiesel, filterDieselPremium, filterGLP;
    private CheckBox filter24h, filterFavoritas;
    private EditText filterPrecioMaxGasolina95, filterPrecioMaxDiesel;
    private Button btnApplyFilters, btnClearFilters, btnUndoFilters;
    private TextView filterResultsCount;

    public interface FiltersListener {
        void onFiltersApplied(FiltrosEstado estado);

        void onFiltersCleared();

        void onUndoFilters();

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
        viewModel = new ViewModelProvider(requireActivity())
                .get(GasStationViewModel.class);
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item,
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
        btnUndoFilters = view.findViewById(R.id.btn_undo_filters);
        filterResultsCount = view.findViewById(R.id.filter_results_count);

        View filtersHeader = view.findViewById(R.id.filters_header);
        if (filtersHeader != null) {
            ViewCompat.setOnApplyWindowInsetsListener(filtersHeader, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        ImageButton btnCloseFilters = view.findViewById(R.id.btn_close_filters);
        if (btnCloseFilters != null) {
            btnCloseFilters.setOnClickListener(v -> {
                if (listener != null)
                    listener.onCloseFilters();
            });
        }
    }

    private void setupListeners() {
        // Mejorar la experiencia de los AutoCompleteTextView
        View.OnClickListener clickListener = v -> {
            if (v instanceof AutoCompleteTextView) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        };

        filterProvincia.setOnClickListener(clickListener);
        filterMunicipio.setOnClickListener(clickListener);
        filterGasolinera.setOnClickListener(clickListener);

        // Mostrar dropdown al recibir foco
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus && v instanceof AutoCompleteTextView) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        };
        filterProvincia.setOnFocusChangeListener(focusListener);
        filterMunicipio.setOnFocusChangeListener(focusListener);
        filterGasolinera.setOnFocusChangeListener(focusListener);

        if (btnApplyFilters != null) {
            btnApplyFilters.setOnClickListener(v -> {
                listener.onFiltersApplied(buildEstadoDesdeUI());
            });
        }

        if (btnClearFilters != null) {
            btnClearFilters.setOnClickListener(v -> {
                clearUI();
                if (listener != null)
                    listener.onFiltersCleared();
            });
        }

        if (btnUndoFilters != null) {
            btnUndoFilters.setOnClickListener(v -> {
                if (listener != null)
                    listener.onUndoFilters();
            });
        }
    }

    private FiltrosEstado buildEstadoDesdeUI() {
        return new FiltrosEstado.Builder()
                .provincia(filterProvincia.getText().toString().trim())
                .municipio(filterMunicipio.getText().toString().trim())
                .gasolinera(filterGasolinera.getText().toString().trim())
                .soloGasolina95(filterGasolina95.isChecked())
                .soloGasolina98(filterGasolina98.isChecked())
                .soloDiesel(filterDiesel.isChecked())
                .soloDieselPremium(filterDieselPremium.isChecked())
                .soloGLP(filterGLP.isChecked())
                .solo24Horas(filter24h.isChecked())
                .soloFavoritas(filterFavoritas.isChecked())
                .precioMaxGasolina95(parsePrecio(filterPrecioMaxGasolina95.getText().toString()))
                .precioMaxDiesel(parsePrecio(filterPrecioMaxDiesel.getText().toString()))
                .build();
    }

    public void refreshFromManager() {
        if (filtrosManager == null)
            return;
        filterProvincia.setText(filtrosManager.getProvincia() == null ? "" : filtrosManager.getProvincia());
        filterMunicipio.setText(filtrosManager.getMunicipio() == null ? "" : filtrosManager.getMunicipio());
        filterGasolinera.setText(filtrosManager.getGasolinera() == null ? "" : filtrosManager.getGasolinera());
        filterGasolina95.setChecked(filtrosManager.isSoloGasolina95());
        filterGasolina98.setChecked(filtrosManager.isSoloGasolina98());
        filterDiesel.setChecked(filtrosManager.isSoloDiesel());
        filterDieselPremium.setChecked(filtrosManager.isSoloDieselPremium());
        filterGLP.setChecked(filtrosManager.isSoloGLP());
        filter24h.setChecked(filtrosManager.isSolo24Horas());
        filterFavoritas.setChecked(filtrosManager.isSoloFavoritas());
        filterPrecioMaxGasolina95.setText(
                filtrosManager.getPrecioMaxGasolina95() == null ? "" : String.valueOf(filtrosManager.getPrecioMaxGasolina95()));
        filterPrecioMaxDiesel.setText(
                filtrosManager.getPrecioMaxDiesel() == null ? "" : String.valueOf(filtrosManager.getPrecioMaxDiesel()));
    }

    private Double parsePrecio(String text) {
        if (text == null || text.trim().isEmpty())
            return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
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
    }

    public void setResultsCount(int count) {
        if (filterResultsCount != null) {
            filterResultsCount.setText(getResources().getQuantityString(R.plurals.results_count, count, count));
            filterResultsCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.count_pop));
        }
    }
}
