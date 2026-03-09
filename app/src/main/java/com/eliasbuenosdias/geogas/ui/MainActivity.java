package com.eliasbuenosdias.geogas.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.eliasbuenosdias.geogas.utils.FiltrosManager;
import com.eliasbuenosdias.geogas.utils.IconosManager;
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

    private final Runnable viewportChangeRunnable = () -> {
        if (viewModel != null && mapHelper != null) {
            viewModel.setViewport(((MapView) findViewById(R.id.map)).getBoundingBox());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupOsmdroidConfig();
        setContentView(R.layout.activity_main);

        initializeManagers();
        initializeViewModel();
        initializeUI();
        setupFragments();
        checkPermissionsAndLoadData();
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
        viewModel.getProgress().observe(this,
                p -> splashHelper.updateStatus(p, viewModel.getStatusMessage().getValue()));
        viewModel.getIsLoading().observe(this, loading -> {
            if (!loading && viewModel.getProgress().getValue() >= 100)
                splashHelper.hide();
        });
    }

    private void initializeUI() {
        btnLocationContainer = findViewById(R.id.btn_location_container);
        splashHelper = new SplashHelper(findViewById(android.R.id.content), btnLocationContainer);

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

        View.OnClickListener filterToggleListener = v -> {
            Log.d("GeoGas", "Filtros click - Toggling panel");
            toggleFiltersPanel();
        };

        if (filterBtnContainer != null)
            filterBtnContainer.setOnClickListener(filterToggleListener);
        if (toolbarFiltersButton != null)
            toolbarFiltersButton.setOnClickListener(filterToggleListener);
    }

    private void triggerViewportUpdate() {
        handler.removeCallbacks(viewportChangeRunnable);
        handler.postDelayed(viewportChangeRunnable, 300);
    }

    private void setupFragments() {
        filtersContainer = findViewById(R.id.filters_panel_container);
        detailsContainer = findViewById(R.id.detail_panel_container);

        filtersFragment = (FiltersFragment) getSupportFragmentManager().findFragmentById(R.id.filters_panel_container);
        detailsFragment = (DetailsPanelFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_panel_container);

        setupFiltersListener();
        setupDetailsListener();
    }

    private void setupFiltersListener() {
        if (filtersFragment == null)
            return;
        filtersFragment.setFiltrosManager(filtrosManager);
        filtersFragment.setFiltersListener(new FiltersFragment.FiltersListener() {
            @Override
            public void onFiltersApplied() {
                viewModel.applyFilters(filtrosManager, favoritosManager);
                hideFiltersPanel();
                updateFilterButtonIcon();
            }

            @Override
            public void onFiltersCleared() {
                viewModel.applyFilters(filtrosManager, favoritosManager);
                updateFilterButtonIcon();
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
            width = (int) (320 * getResources().getDisplayMetrics().density); // Fallback size

        filtersContainer.setTranslationX(-width);
        filtersContainer.animate().translationX(0).setDuration(300).start();
    }

    private void hideFiltersPanel() {
        if (filtersContainer == null)
            return;
        int width = filtersContainer.getWidth();
        if (width <= 0)
            width = (int) (320 * getResources().getDisplayMetrics().density);

        filtersContainer.animate().translationX(-width)
                .setDuration(300).withEndAction(() -> filtersContainer.setVisibility(View.GONE)).start();
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
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
        } catch (SecurityException ignored) {
        }
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