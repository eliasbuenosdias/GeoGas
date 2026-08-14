package com.eliasbuenosdias.geogas.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.eliasbuenosdias.geogas.R;
import com.eliasbuenosdias.geogas.api.ApiClient;
import com.eliasbuenosdias.geogas.api.MitecoApiService;
import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.ui.fragments.DetailsPanelFragment;
import com.eliasbuenosdias.geogas.ui.fragments.FiltersFragment;
import com.eliasbuenosdias.geogas.ui.helpers.MapHelper;
import com.eliasbuenosdias.geogas.ui.helpers.PermissionHelper;
import com.eliasbuenosdias.geogas.ui.helpers.SplashHelper;
import com.eliasbuenosdias.geogas.utils.FavoritosManager;
import com.eliasbuenosdias.geogas.utils.FiltrosEstado;
import com.eliasbuenosdias.geogas.utils.FiltrosManager;
import com.eliasbuenosdias.geogas.utils.IconosManager;
import com.eliasbuenosdias.geogas.utils.LocaleManager;
import com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras;
import com.eliasbuenosdias.geogas.viewmodels.GasStationViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

/**
 * Actividad principal que orquestra la UI, el mapa y la carga de datos.
 * Refactorizada según los principios de Clean Code.
 */
public class MainActivity extends AppCompatActivity {

    private GasStationViewModel viewModel;
    private FiltersFragment filtersFragment;
    private DetailsPanelFragment detailsFragment;
    private View filtersContainer, detailsContainer;

    private MapHelper mapHelper;
    private SplashHelper splashHelper;
    private ImageButton toolbarFiltersButton;
    private LinearLayout btnLocationContainer;

    private FavoritosManager favoritosManager;
    private FiltrosManager filtrosManager;
    private final Handler handler = new Handler();
    private static final String KEY_LOCALE_CHANGE = "locale_change_pending";
    private static final int PANEL_ANIMATION_DURATION_MS = 300;
    private static final int VIEWPORT_DEBOUNCE_DELAY_MS = 300;
    private static final int FALLBACK_PANEL_WIDTH_DP = 360;
    private static final int MAX_PANEL_WIDTH_DP = 400;
    private static final float PANEL_WIDTH_SCREEN_FACTOR = 0.9f;

    private final Runnable viewportChangeRunnable = () -> {
        if (viewModel != null && mapHelper != null) {
            viewModel.setViewport(((MapView) findViewById(R.id.map)).getBoundingBox());
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GeoGas);
        super.onCreate(savedInstanceState);
        setupOsmdroidConfig();
        setContentView(R.layout.activity_main);

        initializeManagers();
        initializeViewModel();
        initializeUI();
        setupFragments();

        // If the Activity was recreated due to a locale change, skip splash & data
        // reload
        boolean isLocaleChange = getPreferences(MODE_PRIVATE).getBoolean(KEY_LOCALE_CHANGE, false);
        if (isLocaleChange) {
            getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_LOCALE_CHANGE, false).apply();
            splashHelper.forceHide();
            // Re-trigger viewport to show existing data
            MapView mapView = findViewById(R.id.map);
            if (mapView != null) {
                viewModel.setViewport(mapView.getBoundingBox());
            }
        } else {
            checkPermissionsAndLoadData();
        }
    }

    private void setupOsmdroidConfig() {
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
    }

    private void initializeManagers() {
        favoritosManager = new FavoritosManager(this);
        filtrosManager = new FiltrosManager();
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(GasStationViewModel.class);
        viewModel.setPuntuador(new PuntuadorGasolineras(favoritosManager));

        viewModel.getGasolinerasVisibles().observe(this, list -> mapHelper.updateMarkers(list));
        viewModel.getLoadState().observe(this, state -> {
            splashHelper.updateStatus(state.getProgress(), resolveStatusMessage(state.getStatusKey()));
            if (state.isLoaded())
                splashHelper.hide();
        });
    }

    private String resolveStatusMessage(String key) {
        if (key == null)
            return "";
        switch (key) {
            case "status_loading":
                return getString(R.string.status_loading);
            case "status_processing":
                return getString(R.string.status_processing);
            case "status_error_server":
                return getString(R.string.status_error_server);
            case "status_error_parsing":
                return getString(R.string.status_error_parsing);
            case "status_error_connection":
                return getString(R.string.status_error_connection);
            default:
                return key; // fallback: show raw if unknown
        }
    }

    private void initializeUI() {
        btnLocationContainer = findViewById(R.id.btn_location_container);
        splashHelper = new SplashHelper(findViewById(android.R.id.content), btnLocationContainer);

        applySystemBarInsets();

        MapView mapView = findViewById(R.id.map);
        mapHelper = new MapHelper(mapView, new IconosManager(this, favoritosManager), favoritosManager);
        mapHelper.setOnMarkerClickListener(g -> {
            if (detailsFragment != null)
                detailsFragment.setGasolinera(g);
            showDetailPanel();
        });

        mapView.addMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent e) {
                triggerViewportUpdate();
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent e) {
                triggerViewportUpdate();
                return false;
            }
        });

        findViewById(R.id.btn_my_location).setOnClickListener(v -> centerOnMyLocation());
        View filterBtnContainer = findViewById(R.id.toolbar_filters_container);
        toolbarFiltersButton = findViewById(R.id.toolbar_filters_button);

        View langBtnContainer = findViewById(R.id.toolbar_lang_container);
        if (langBtnContainer != null) {
            langBtnContainer.setOnClickListener(v -> showLanguagePicker());
        }

        View.OnClickListener filterToggleListener = v -> {
            Log.d("GeoGas", "Filtros click - Toggling panel");
            toggleFiltersPanel();
        };

        if (filterBtnContainer != null)
            filterBtnContainer.setOnClickListener(filterToggleListener);
        if (toolbarFiltersButton != null)
            toolbarFiltersButton.setOnClickListener(filterToggleListener);
    }

    private void applySystemBarInsets() {
        View toolbar = findViewById(R.id.custom_toolbar);
        if (toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
                int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                v.setPadding(v.getPaddingLeft(), top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        if (btnLocationContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(btnLocationContainer, (v, insets) -> {
                int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                int spacingDp = Math.round(TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                lp.bottomMargin = bottom + spacingDp;
                v.setLayoutParams(lp);
                return insets;
            });
        }
    }

    private void triggerViewportUpdate() {
        handler.removeCallbacks(viewportChangeRunnable);
        handler.postDelayed(viewportChangeRunnable, VIEWPORT_DEBOUNCE_DELAY_MS);
    }

    private void setupFragments() {
        filtersContainer = findViewById(R.id.filters_panel_container);
        detailsContainer = findViewById(R.id.detail_panel_container);

        capPanelWidth(filtersContainer);
        capPanelWidth(detailsContainer);

        filtersFragment = (FiltersFragment) getSupportFragmentManager().findFragmentById(R.id.filters_panel_container);
        detailsFragment = (DetailsPanelFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_panel_container);

        setupFiltersListener();
        setupDetailsListener();
    }

    private void capPanelWidth(View container) {
        if (container == null)
            return;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int maxByFactor = (int) (screenWidth * PANEL_WIDTH_SCREEN_FACTOR);
        int maxByDp = (int) (MAX_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density);
        int width = Math.min(maxByFactor, maxByDp);
        ViewGroup.LayoutParams lp = container.getLayoutParams();
        if (lp != null && lp.width != width) {
            lp.width = width;
            container.setLayoutParams(lp);
        }
    }

    private void setupFiltersListener() {
        if (filtersFragment == null)
            return;
        viewModel.setFiltrosManager(filtrosManager);
        filtersFragment.setFiltrosManager(filtrosManager);
        filtersFragment.setFiltersListener(new FiltersFragment.FiltersListener() {
            @Override
            public void onFiltersApplied(FiltrosEstado estado) {
                viewModel.aplicarFiltros(estado, favoritosManager);
                hideFiltersPanel();
                updateFilterButtonIcon();
            }

            @Override
            public void onFiltersCleared() {
                viewModel.aplicarFiltros(FiltrosEstado.sinFiltros(), favoritosManager);
                updateFilterButtonIcon();
            }

            @Override
            public void onUndoFilters() {
                if (viewModel.deshacerUltimoFiltro(favoritosManager)) {
                    filtersFragment.refreshFromManager();
                    updateFilterButtonIcon();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.filters_nothing_to_undo),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCloseFilters() {
                hideFiltersPanel();
            }
        });
    }

    private void setupDetailsListener() {
        if (detailsFragment == null)
            return;
        detailsFragment.setFavoritosManager(favoritosManager);
        detailsFragment.setDetailsListener(new DetailsPanelFragment.DetailsListener() {
            @Override
            public void onCloseDetails() {
                hideDetailPanel();
            }

            @Override
            public void onFavoriteToggled(GasolineraAPI g) {
                favoritosManager.toggleFavorito(g.getId());
                viewModel.applyFilters(filtrosManager, favoritosManager);
                mapHelper.refreshMarkers();
            }
        });
    }

    private void toggleFiltersPanel() {
        if (filtersContainer.getVisibility() == View.VISIBLE)
            hideFiltersPanel();
        else
            showFiltersPanel();
    }

    private void showFiltersPanel() {
        if (filtersContainer == null)
            return;
        filtersContainer.setVisibility(View.VISIBLE);
        int width = filtersContainer.getWidth();
        if (width <= 0)
            width = (int) (FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density);

        filtersContainer.setTranslationX(-width);
        filtersContainer.animate().translationX(0).setDuration(PANEL_ANIMATION_DURATION_MS).start();
    }

    private void hideFiltersPanel() {
        if (filtersContainer == null)
            return;
        int width = filtersContainer.getWidth();
        if (width <= 0)
            width = (int) (FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density);

        filtersContainer.animate().translationX(-width)
                .setDuration(PANEL_ANIMATION_DURATION_MS).withEndAction(() -> filtersContainer.setVisibility(View.GONE)).start();
    }

    private void showDetailPanel() {
        detailsContainer.setVisibility(View.VISIBLE);
    }

    private void hideDetailPanel() {
        detailsContainer.setVisibility(View.GONE);
    }

    private void updateFilterButtonIcon() {
        int icon = filtrosManager.tieneFiltrosActivos() ? R.drawable.ic_filter_active_pro : R.drawable.ic_filter_pro;
        toolbarFiltersButton.setImageResource(icon);
    }

    private void checkPermissionsAndLoadData() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            PermissionHelper.requestLocationPermission(this);
        } else {
            loadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQ_PERMISSIONS && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadData();
        }
    }

    private void loadData() {
        viewModel.loadData(ApiClient.getClient().create(MitecoApiService.class));
    }

    private void centerOnMyLocation() {
        android.location.LocationManager lm = (android.location.LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        try {
            Location loc = lm.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            if (loc == null)
                loc = lm.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            if (loc != null)
                ((MapView) findViewById(R.id.map)).getController()
                        .animateTo(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
            else
                Toast.makeText(this, getString(R.string.error_location_not_found), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Log.w("GeoGas", "No permission to get location", e);
        }
    }

    private void showLanguagePicker() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_language_picker, null);
        android.widget.RadioGroup radioGroup = dialogView.findViewById(R.id.language_radio_group);
        android.widget.RadioButton radioEs = dialogView.findViewById(R.id.radio_es);
        android.widget.RadioButton radioEn = dialogView.findViewById(R.id.radio_en);

        String currentLang = LocaleManager.getLanguage(this);
        if ("en".equals(currentLang)) {
            radioEn.setChecked(true);
        } else {
            radioEs.setChecked(true);
        }

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedLang = (checkedId == R.id.radio_en) ? "en" : "es";
            if (!selectedLang.equals(currentLang)) {
                dialog.dismiss();
                LocaleManager.setLanguage(this, selectedLang);
                getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_LOCALE_CHANGE, true).apply();
                recreate();
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (findViewById(R.id.map) != null)
            ((MapView) findViewById(R.id.map)).onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (findViewById(R.id.map) != null)
            ((MapView) findViewById(R.id.map)).onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}