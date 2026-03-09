package com.eliasbuenosdias.geogas.ui.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.eliasbuenosdias.geogas.R;
import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.utils.FavoritosManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento que gestiona el panel de detalles de una gasolinera.
 * Encapsula la lógica de visualización de precios y datos adicionales.
 */
public class DetailsPanelFragment extends Fragment {

    private TextView tvStationName, tvStationAddress, tvLocation, tvHorario;
    private View horarioContainer;
    private ImageButton btnFavorite, btnClose;
    private TableLayout tableGasolina, tableDiesel, tableAlternativos, tableAdicional;

    private FavoritosManager favoritosManager;
    private DetailsListener listener;
    private GasolineraAPI gasolinera;

    public interface DetailsListener {
        void onCloseDetails();

        void onFavoriteToggled(GasolineraAPI gasolinera);
    }

    public void setDetailsListener(DetailsListener listener) {
        this.listener = listener;
    }

    public void setFavoritosManager(FavoritosManager favoritosManager) {
        this.favoritosManager = favoritosManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.panel_detalles_gasolinera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
    }

    private void initializeViews(View view) {
        tvStationName = view.findViewById(R.id.tv_station_name);
        tvStationAddress = view.findViewById(R.id.tv_station_address);
        tvLocation = view.findViewById(R.id.tv_location);
        tvHorario = view.findViewById(R.id.tv_horario);
        horarioContainer = view.findViewById(R.id.horario_container);
        btnFavorite = view.findViewById(R.id.btn_favorite);
        btnClose = view.findViewById(R.id.btn_close_panel);

        tableGasolina = view.findViewById(R.id.table_gasolina);
        tableDiesel = view.findViewById(R.id.table_diesel);
        tableAlternativos = view.findViewById(R.id.table_alternativos);
        tableAdicional = view.findViewById(R.id.table_adicional);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                if (listener != null)
                    listener.onCloseDetails();
            });
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                if (gasolinera != null && listener != null) {
                    listener.onFavoriteToggled(gasolinera);
                    updateFavoriteIcon();
                }
            });
        }
    }

    public void showGasolinera(GasolineraAPI gasolinera) {
        this.gasolinera = gasolinera;
        if (getView() == null)
            return;

        tvStationName.setText(gasolinera.getRotulo());
        tvStationAddress.setText(gasolinera.getDireccion());
        tvLocation.setText(gasolinera.getMunicipio() + ", " + gasolinera.getProvincia());

        if (gasolinera.getHorario() != null && !gasolinera.getHorario().isEmpty()) {
            tvHorario.setText(gasolinera.getHorario());
            horarioContainer.setVisibility(View.VISIBLE);
        } else {
            horarioContainer.setVisibility(View.GONE);
        }

        updateFavoriteIcon();
        fillTables();
    }

    private void updateFavoriteIcon() {
        if (gasolinera == null || favoritosManager == null || btnFavorite == null)
            return;

        boolean isFav = favoritosManager.esFavorita(gasolinera.getIDEESS());
        btnFavorite.setImageResource(isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    private void fillTables() {
        tableGasolina.removeAllViews();
        tableDiesel.removeAllViews();
        tableAlternativos.removeAllViews();
        tableAdicional.removeAllViews();

        // Lógica simplificada de llenado de tablas (extraída de MainActivity)
        // ... (Se implementaría la lógica de addFilaPrecio aquí o en un helper)
    }

    private void addFilaPrecio(TableLayout table, String nombre, String precio) {
        TableRow row = new TableRow(getContext());
        TextView tvNombre = new TextView(getContext());
        tvNombre.setText(nombre);
        tvNombre.setTextColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary));
        tvNombre.setPadding(8, 8, 8, 8);

        TextView tvPrecio = new TextView(getContext());
        tvPrecio.setText(precio);
        tvPrecio.setTextColor(Color.parseColor("#388E3C"));
        tvPrecio.setTypeface(null, Typeface.BOLD);
        tvPrecio.setGravity(Gravity.END);
        tvPrecio.setPadding(8, 8, 8, 8);

        row.addView(tvNombre);
        row.addView(tvPrecio);
        table.addView(row);
    }
}
