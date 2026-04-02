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

    public void setGasolinera(GasolineraAPI gasolinera) {
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

        boolean isFav = favoritosManager.esFavorita(gasolinera.getId());
        if (isFav) {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            btnFavorite.setColorFilter(Color.parseColor("#FFD700"));
        } else {
            btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            btnFavorite.setColorFilter(Color.parseColor("#CCCCCC"));
        }
    }

    private void fillTables() {
        tableGasolina.removeAllViews();
        tableDiesel.removeAllViews();
        tableAlternativos.removeAllViews();
        tableAdicional.removeAllViews();

        // Gasolina
        addFilaPrecioSiExiste(tableGasolina, getString(R.string.fuel_gasoline_95_e5), gasolinera.getPrecioGasolina95());
        addFilaPrecioSiExiste(tableGasolina, getString(R.string.fuel_gasoline_95_e10),
                gasolinera.getPrecioGasolina95E10());
        addFilaPrecioSiExiste(tableGasolina, getString(R.string.fuel_gasoline_98_e5), gasolinera.getPrecioGasolina98());
        addFilaPrecioSiExiste(tableGasolina, getString(R.string.fuel_gasoline_98_e10),
                gasolinera.getPrecioGasolina98E10());

        // Diesel
        addFilaPrecioSiExiste(tableDiesel, getString(R.string.fuel_diesel_standard), gasolinera.getPrecioGasoleoA());
        addFilaPrecioSiExiste(tableDiesel, getString(R.string.fuel_diesel_premium),
                gasolinera.getPrecioGasoleoPremium());
        addFilaPrecioSiExiste(tableDiesel, getString(R.string.fuel_diesel_agro), gasolinera.getPrecioGasoleoB());

        // Alternativos
        addFilaPrecioSiExiste(tableAlternativos, getString(R.string.filters_glp), gasolinera.getPrecioGLP());
        addFilaPrecioSiExiste(tableAlternativos, getString(R.string.fuel_gnc), gasolinera.getPrecioGNC());
        addFilaPrecioSiExiste(tableAlternativos, getString(R.string.fuel_gnl), gasolinera.getPrecioGNL());
        addFilaPrecioSiExiste(tableAlternativos, getString(R.string.fuel_hydrogen), gasolinera.getPrecioHidrogeno());

        // Adicional
        addFilaInfoSiExiste(tableAdicional, getString(R.string.info_sale_type), gasolinera.getTipoVenta());
        addFilaInfoSiExiste(tableAdicional, getString(R.string.info_margin), gasolinera.getMargen());
        addFilaInfoSiExiste(tableAdicional, getString(R.string.info_updated), gasolinera.getFecha());
    }

    private void addFilaPrecioSiExiste(TableLayout table, String nombre, String precio) {
        if (precio != null && !precio.isEmpty()) {
            addFilaPrecio(table, nombre, precio + " €");
        }
    }

    private void addFilaInfoSiExiste(TableLayout table, String nombre, String valor) {
        if (valor != null && !valor.isEmpty()) {
            addFilaInfo(table, nombre, valor);
        }
    }

    private void addFilaPrecio(TableLayout table, String nombre, String precio) {
        TableRow row = new TableRow(getContext());
        TextView tvNombre = createTextView(nombre, false);
        TextView tvPrecio = createTextView(precio, true);
        tvPrecio.setTextColor(Color.parseColor("#388E3C"));
        tvPrecio.setGravity(Gravity.END);

        row.addView(tvNombre);
        row.addView(tvPrecio);
        table.addView(row);
    }

    private void addFilaInfo(TableLayout table, String nombre, String valor) {
        TableRow row = new TableRow(getContext());
        TextView tvNombre = createTextView(nombre, false);
        TextView tvValor = createTextView(valor, false);
        tvValor.setGravity(Gravity.END);

        row.addView(tvNombre);
        row.addView(tvValor);
        table.addView(row);
    }

    private TextView createTextView(String text, boolean bold) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary));
        tv.setPadding(8, 8, 8, 8);
        if (bold)
            tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

}
