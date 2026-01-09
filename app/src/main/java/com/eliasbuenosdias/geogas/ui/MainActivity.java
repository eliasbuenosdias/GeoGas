package com.eliasbuenosdias.geogas.ui;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.eliasbuenosdias.geogas.R;
import com.eliasbuenosdias.geogas.api.ApiClient;
import com.eliasbuenosdias.geogas.api.MitecoApiService;
import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.utils.FavoritosManager;
import com.eliasbuenosdias.geogas.utils.FiltrosManager;
import com.eliasbuenosdias.geogas.utils.IconosManager;
import com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras;
import com.google.gson.JsonElement;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;

/**
 * Actividad principal de la aplicación GeoGas que gestiona el mapa interactivo de gasolineras.
 * Proporciona funcionalidades de visualización de estaciones de servicio, filtrado por múltiples criterios,
 * gestión de favoritos y navegación en el mapa con ubicación GPS.
 * Utiliza OSMDroid para la representación cartográfica y Retrofit para la obtención de datos de la API de Miteco.
 *
 * @author Elías Prieto Parrilla
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Código de solicitud para permisos de ubicación.
     */
    private static final int REQ_PERMISSIONS = 100;
    /**
     * Vista del mapa OSMDroid.
     */
    private MapView map;
    /**
     * Overlay para mostrar la ubicación actual del usuario en el mapa.
     */
    private MyLocationNewOverlay myLocationOverlay;
    /**
     * Panel deslizante que muestra los detalles de una gasolinera seleccionada.
     */
    private View detailPanel;
    /**
     * TextView para mostrar el nombre de la estación en el panel de detalles.
     */
    private TextView tvStationName, tvStationAddress;
    /**
     * Botón para añadir o quitar una gasolinera de favoritos.
     */
    private ImageButton btnClosePanel, btnFavorite;
    /**
     * Manager para gestionar las gasolineras favoritas del usuario.
     */
    private FavoritosManager favoritosManager;
    /**
     * Gasolinera actualmente mostrada en el panel de detalles.
     */
    private GasolineraAPI gasolineraActual;
    /**
     * Lista cache con todas las gasolineras obtenidas de la API.
     */
    private List<GasolineraAPI> todasLasGasolineras = new ArrayList<>();
    /**
     * Indica si los datos de gasolineras han sido cargados desde la API.
     */
    private boolean datosCargados = false;
    /**
     * Límite máximo de gasolineras visibles simultáneamente en el mapa para evitar saturación.
     */
    private static final int MAX_GASOLINERAS_VISIBLES = 20000; // Límite para no saturar el mapa
    /**
     * Último viewport del mapa para evitar recargas innecesarias cuando no cambia significativamente.
     */
    private org.osmdroid.util.BoundingBox ultimoViewport; // Para evitar recargas innecesarias
    /**
     * Handler para gestionar operaciones diferidas y actualizaciones asíncronas.
     */
    private Handler handler = new Handler();
    /**
     * Puntuador para ordenar gasolineras por relevancia según múltiples criterios.
     */
    private PuntuadorGasolineras puntuadorGasolineras;
    /**
     * Manager para gestionar los iconos adaptativos según densidad y zoom.
     */
    private IconosManager iconosManager;

    // Variables para el splash screen
    /**
     * Vista del splash screen mostrado durante la carga inicial.
     */
    private View splashScreen;
    /**
     * Indica si el mapa está listo para ser usado.
     */
    private boolean isMapReady = false;
    /**
     * Indica si los datos de gasolineras han sido cargados.
     */
    private boolean isDataLoaded = false;
    /**
     * Indica si los permisos han sido verificados.
     */
    private boolean isPermissionsChecked = false;
    /**
     * Barra de progreso del splash screen.
     */
    private ProgressBar splashProgressBar;
    /**
     * TextView que muestra el porcentaje de progreso en el splash screen.
     */
    private TextView splashProgressText;
    /**
     * TextView que muestra el mensaje de carga en el splash screen.
     */
    private TextView splashLoadingText;
    /**
     * Progreso actual del splash screen (0-100).
     */
    private int splashProgress = 0;
    /**
     * Handler específico para gestionar el splash screen.
     */
    private Handler splashHandler = new Handler();
    /**
     * Botón para centrar el mapa en la ubicación actual del usuario.
     */
    private ImageButton btnMyLocation;
    /**
     * Contenedor del botón de ubicación.
     */
    private LinearLayout btnLocationContainer;

    // Nuevas variables para filtros

    /**
     * Panel lateral que contiene los controles de filtrado.
     */
    private View filtersPanel;
    /**
     * Manager para gestionar todos los filtros de búsqueda de gasolineras.
     */
    private FiltrosManager filtrosManager;
    /**
     * Campo de autocompletado para filtrar por provincia y municipio
     */
    private AutoCompleteTextView filterProvincia, filterMunicipio;
    /**
     * Checkbox para filtrar gasolineras que tengan Gasolina 95...
     */
    private CheckBox filterGasolina95, filterGasolina98, filterDiesel, filterDieselPremium, filterGLP;
    /**
     * Checkbox para filtrar gasolineras abiertas 24 horas...
     */
    private CheckBox filter24h, filterFavoritas;
    /**
     * Campo de texto para establecer precio máximo de...
     */
    private EditText filterPrecioMaxGasolina95, filterPrecioMaxDiesel;
    /**
     * Botón para aplicar los filtros seleccionados, Botón limpiar todos los filtros.
     */
    private Button btnApplyFilters, btnClearFilters;
    /**
     * TextView que muestra el número de resultados encontrados con los filtros actuales.
     */
    private TextView filterResultsCount;
    /**
     * Campo de autocompletado para filtrar por nombre de gasolinera.
     */
    private AutoCompleteTextView filterGasolinera;
    /**
     * Contenedor del botón de filtros en la cabecera.
     */
    private LinearLayout btnFiltersHeaderContainer;
    /**
     * Botón de filtros en la cabecera.
     */
    private ImageButton btnFiltersHeader;
    /**
     * Contenedor del botón de filtros principal.
     */
    private LinearLayout btnFiltersContainerMain;
    /**
     * Botón de filtros principal.
     */
    private ImageButton btnFiltersMain;

    // Variables para la cabecera personalizada
    /**
     * Barra de herramientas personalizada de la aplicación.
     */
    private LinearLayout customToolbar;
    /**
     * Contenedor del botón de filtros en la toolbar.
     */
    private LinearLayout toolbarFiltersContainer;
    /**
     * Botón de filtros en la toolbar.
     */
    private ImageButton toolbarFiltersButton;
    /**
     * Spinner de carga en el splash screen.
     */
    private ProgressBar splashSpinner;
    /**
     * Añade las atribuciones legales obligatorias:
     * - OpenStreetMap (licencia ODbL)
     * - MITECO (datos del sector público español)
     */
    private void addOSMCopyright() {
        runOnUiThread(() -> {
            // Buscar el contenedor raíz
            android.view.ViewGroup rootView = (android.view.ViewGroup) findViewById(android.R.id.content);

            // Crear TextView con AMBAS atribuciones
            TextView copyright = new TextView(this);
            copyright.setText("© OpenStreetMap\nDatos: MITECO - Gobierno de España");
            copyright.setTextColor(Color.BLACK);
            copyright.setBackgroundColor(Color.argb(220, 255, 255, 255));
            copyright.setPadding(dpToPx(3), dpToPx(1), dpToPx(3), dpToPx(1));
            copyright.setTextSize(5); // Reducido a 8sp para que quepa
            copyright.setElevation(10);
            copyright.setMaxLines(2); // Permitir 2 líneas

            // Layout params
            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            params.setMargins(0, 0, dpToPx(0), dpToPx(0));

            copyright.setLayoutParams(params);
            rootView.addView(copyright);

            Log.d("GeoGas", "Atribuciones legales añadidas (OSM + MITECO)");
        });
    }


    // Clases auxiliares para organizar datos

    /**
     * Clase auxiliar interna para representar un item de precio con su nombre y valor.
     */
    private static class PrecioItem {
        /**
         * Nombre descriptivo del tipo de combustible.
         */
        String nombre;
        /**
         * Precio del combustible en formato texto.
         */
        String precio;

        /**
         * Constructor del item de precio.
         *
         * @param nombre nombre del tipo de combustible
         * @param precio precio del combustible
         */
        PrecioItem(String nombre, String precio) {
            this.nombre = nombre;
            this.precio = precio;
        }
    }
    /**
     * Clase auxiliar interna para representar un item de información con su nombre y valor.
     */
    private static class InfoItem {
        /**
         * Nombre del campo de información.
         */
        String nombre;
        /**
         * Valor del campo de información.
         */
        String valor;

        /**
         * Constructor del item de información.
         *
         * @param nombre nombre del campo
         * @param valor  valor del campo
         */
        InfoItem(String nombre, String valor) {
            this.nombre = nombre;
            this.valor = valor;
        }
    }

    // Métodos utilitarios
    /**
     * Añade un item de precio a la lista si el precio es válido y no está vacío.
     *
     * @param lista lista donde añadir el item
     * @param nombre nombre del tipo de combustible
     * @param precio precio del combustible
     */
    private void addPrecioSiExiste(List<PrecioItem> lista, String nombre, String precio) {
        if (precio != null && !precio.isEmpty() && !precio.equals("N/A")) {
            lista.add(new PrecioItem(nombre, precio + "€"));
        }
    }
    /**
     * Añade un item de información a la lista si el valor es válido y no está vacío.
     *
     * @param lista lista donde añadir el item
     * @param nombre nombre del campo de información
     * @param valor valor del campo
     */
    private void addInfoSiExiste(List<InfoItem> lista, String nombre, String valor) {
        if (valor != null && !valor.isEmpty() && !valor.equals("N/A")) {
            lista.add(new InfoItem(nombre, valor));
        }
    }
    /**
     * Añade una fila de precio a una tabla con formato visual específico.
     *
     * @param table tabla donde añadir la fila
     * @param nombre nombre del combustible
     * @param precio precio del combustible
     */
    private void addFilaPrecio(TableLayout table, String nombre, String precio) {
        TableRow row = new TableRow(this);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombre);
        tvNombre.setTextColor(Color.BLACK);
        tvNombre.setTextSize(14); // Tamaño en sp
        tvNombre.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        TextView tvPrecio = new TextView(this);
        tvPrecio.setText(precio);
        tvPrecio.setTextColor(Color.parseColor("#388E3C")); // Verde oscuro
        tvPrecio.setTypeface(tvPrecio.getTypeface(), Typeface.BOLD);
        tvPrecio.setGravity(Gravity.END);
        tvPrecio.setTextSize(14); // Tamaño en sp
        tvPrecio.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        row.addView(tvNombre);
        row.addView(tvPrecio);
        table.addView(row);
    }
    /**
     * Añade una fila de información a una tabla con formato visual específico.
     *
     * @param table tabla donde añadir la fila
     * @param nombre nombre del campo de información
     * @param valor valor del campo
     */
    private void addFilaInfo(TableLayout table, String nombre, String valor) {
        TableRow row = new TableRow(this);

        TextView tvNombre = new TextView(this);
        tvNombre.setText(nombre);
        tvNombre.setTextColor(Color.BLACK);
        tvNombre.setTextSize(14); // Tamaño en sp
        tvNombre.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        TextView tvValor = new TextView(this);
        tvValor.setText(valor);
        tvValor.setTextColor(Color.DKGRAY);
        tvValor.setGravity(Gravity.END);
        tvValor.setTextSize(14); // Tamaño en sp
        tvValor.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

        row.addView(tvNombre);
        row.addView(tvValor);
        table.addView(row);
    }
    /**
     * Añade una fila indicando que no hay datos disponibles.
     *
     * @param table tabla donde añadir la fila
     * @param mensaje mensaje a mostrar
     */
    private void addFilaSinDatos(TableLayout table, String mensaje) {
        TableRow row = new TableRow(this);

        TextView tvMensaje = new TextView(this);
        tvMensaje.setText(mensaje);
        tvMensaje.setTextColor(Color.GRAY);
        tvMensaje.setGravity(Gravity.CENTER);
        tvMensaje.setTextSize(14); // Tamaño en sp
        tvMensaje.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        tvMensaje.setLayoutParams(params);

        row.addView(tvMensaje);
        table.addView(row);
    }
    /**
     * Convierte unidades dp (density-independent pixels) a píxeles reales.
     *
     * @param dp valor en dp a convertir
     * @return valor equivalente en píxeles
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    /**
     * Runnable que se ejecuta cuando cambia el viewport del mapa.
     * Actualiza las gasolineras mostradas aplicando los filtros activos.
     */
    private Runnable viewportChangeRunnable = new Runnable() {
        @Override
        public void run() {
            if (filtrosManager != null && filtrosManager.tieneFiltrosActivos()) {
                // Si hay filtros activos, usar la lista filtrada
                List<GasolineraAPI> gasolinerasFiltradas = filtrosManager.aplicarFiltros(todasLasGasolineras,favoritosManager);
                mostrarGasolinerasParaViewportActual(gasolinerasFiltradas);
            } else {
                // Si no hay filtros, usar todas las gasolineras
                mostrarGasolinerasParaViewportActual(todasLasGasolineras);
            }
        }
    };
    /**
     * Inicializa la actividad, configurando el mapa, los managers, los paneles y solicitando permisos.
     *
     * @param savedInstanceState estado guardado de la instancia anterior
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración mejorada de OSMdroid
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_main);

        /**
         * Inicializa la barra de herramientas personalizada con sus controles.
         */
        customToolbar = findViewById(R.id.custom_toolbar);
        toolbarFiltersContainer = customToolbar.findViewById(R.id.toolbar_filters_container);
        toolbarFiltersButton = customToolbar.findViewById(R.id.toolbar_filters_button);
        // Configurar el botón de filtros en la cabecera
        if (toolbarFiltersButton != null) {
            toolbarFiltersButton.setOnClickListener(v -> toggleFiltersPanel());
            Log.d("GeoGas", "✅ Botón de filtros en cabecera configurado");
        }


        // Inicializar componentes en ORDEN CORRECTO
        initializeSplashScreen();

        // INICIALIZAR VISTAS - SOLO ubicación ahora
        btnLocationContainer = findViewById(R.id.btn_location_container);
        btnMyLocation = findViewById(R.id.btn_my_location);

        hideMapControls();
        initializeMap();
        initializeDetailPanel();
        setupUI();

        // Inicializar managers
        favoritosManager = new FavoritosManager(this);
        puntuadorGasolineras = new PuntuadorGasolineras(favoritosManager);
        iconosManager = new IconosManager(this, favoritosManager);
        filtrosManager = new FiltrosManager();

        // ✅ SOLO inicializar el panel de filtros - los autocompletes se configuran DENTRO
        initializeFiltersPanel();

        // SOLO verificar permisos - la carga de datos se hará después
        checkPermissionsAndLoadData();

        Log.d("GeoGas", "Iconos adaptativos: " + iconosManager.obtenerInfoUmbrales());
    }

    /**
     * Inicializa la toolbar personalizada.
     */
    private void initializeCustomToolbar() {
        customToolbar = findViewById(R.id.custom_toolbar);
        toolbarFiltersContainer = customToolbar.findViewById(R.id.toolbar_filters_container);
        toolbarFiltersButton = customToolbar.findViewById(R.id.toolbar_filters_button);

        // Configurar el botón de filtros en la cabecera
        if (toolbarFiltersButton != null) {
            toolbarFiltersButton.setOnClickListener(v -> toggleFiltersPanel());
            Log.d("GeoGas", "✅ Botón de filtros en cabecera configurado");
        } else {
            Log.e("GeoGas", "❌ toolbarFiltersButton es null");
        }
    }
    /**
     * Inicializa el panel de filtros.
     */
    private void initializeFiltersPanel() {
        filtersPanel = findViewById(R.id.filters_panel);

        // Inicializar vistas del panel de filtros
        initializeFilterViews();

        ImageButton btnCloseFilters = filtersPanel.findViewById(R.id.btn_close_filters);
        if (btnCloseFilters != null) {
            btnCloseFilters.setOnClickListener(v -> hideFiltersPanel());
        }

        btnApplyFilters.setOnClickListener(v -> applyFilters());
        btnClearFilters.setOnClickListener(v -> clearFilters());

        Log.d("GeoGas", "✅ Panel de filtros inicializado");
    }
    /**
     * Inicializa las vistas del panel de filtros.
     */
    private void initializeFilterViews() {
        filterProvincia = filtersPanel.findViewById(R.id.filter_provincia);
        filterMunicipio = filtersPanel.findViewById(R.id.filter_municipio);
        filterGasolinera = filtersPanel.findViewById(R.id.filter_gasolinera); // Actualizado
        filterGasolina95 = filtersPanel.findViewById(R.id.filter_gasolina95);
        filterGasolina98 = filtersPanel.findViewById(R.id.filter_gasolina98);
        filterDiesel = filtersPanel.findViewById(R.id.filter_diesel);
        filterDieselPremium = filtersPanel.findViewById(R.id.filter_diesel_premium);
        filterGLP = filtersPanel.findViewById(R.id.filter_glp);
        filter24h = filtersPanel.findViewById(R.id.filter_24h);
        filterFavoritas = filtersPanel.findViewById(R.id.filter_favoritas);
        filterPrecioMaxGasolina95 = filtersPanel.findViewById(R.id.filter_precio_max_gasolina95);
        filterPrecioMaxDiesel = filtersPanel.findViewById(R.id.filter_precio_max_diesel);
        btnApplyFilters = filtersPanel.findViewById(R.id.btn_apply_filters);
        btnClearFilters = filtersPanel.findViewById(R.id.btn_clear_filters);
        filterResultsCount = filtersPanel.findViewById(R.id.filter_results_count);
    }

    /**
     * Obtiene los nombres únicos de gasolineras.
     *
     * @return lista de nombres únicos ordenados
     */
    private List<String> obtenerNombresGasolinerasUnicos() {
        Set<String> nombresUnicos = new HashSet<>();
        if (todasLasGasolineras != null) {
            for (GasolineraAPI gasolinera : todasLasGasolineras) {
                if (gasolinera.getRotulo() != null && !gasolinera.getRotulo().isEmpty()) {
                    nombresUnicos.add(gasolinera.getRotulo());
                }
            }
        }
        List<String> lista = new ArrayList<>(nombresUnicos);
        Collections.sort(lista); // Ordenar alfabéticamente
        return lista;
    }
    /**
     * Configura el autocompletado para el filtro de gasolineras.
     */
    private void configurarAutoCompleteGasolinera() {
        AutoCompleteTextView autoCompleteGasolinera = findViewById(R.id.filter_gasolinera);

        // Obtener lista única de nombres de gasolineras
        final List<String> nombresGasolineras = obtenerNombresGasolinerasUnicos();

        // ✅ ADAPTADOR COMPATIBLE CON API 26
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line
        ) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> suggestions = new ArrayList<>();

                        if (constraint == null || constraint.length() == 0) {
                            // ✅ NO MOSTRAR NADA CUANDO ESTÁ VACÍO
                            suggestions.clear();
                        } else {
                            String filterPattern = constraint.toString().toLowerCase().trim();

                            // ✅ FILTRAR POR STARTSWITH (que comience con)
                            for (String item : nombresGasolineras) {
                                if (item.toLowerCase().startsWith(filterPattern)) {
                                    suggestions.add(item);
                                }
                                // ✅ LIMITAR A 10 SUGERENCIAS MÁXIMO
                                if (suggestions.size() >= 10) {
                                    break;
                                }
                            }
                        }

                        results.values = suggestions;
                        results.count = suggestions.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        // ✅ MÉTODO COMPATIBLE CON API 26
                        clear(); // Este método SÍ existe en ArrayAdapter

                        if (results.values != null) {
                            List<String> filteredList = (List<String>) results.values;
                            for (String item : filteredList) {
                                add(item); // Este método SÍ existe en ArrayAdapter
                            }
                        }

                        notifyDataSetChanged(); // ✅ Este método SÍ existe
                    }
                };
            }
        };

        autoCompleteGasolinera.setAdapter(adapter);
        autoCompleteGasolinera.setThreshold(1); // Mostrar desde el primer carácter

        // ✅ COMPORTAMIENTO CRÍTICO: TextWatcher para actualizar en tiempo real
        autoCompleteGasolinera.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String texto = s.toString().trim();

                // ✅ ACTUALIZAR FILTRO EN TIEMPO REAL
                filtrosManager.setGasolinera(texto);

                // ✅ MOSTRAR/OCULTAR DROPDOWN SEGÚN TEXTO
                if (texto.length() >= 1) {
                    // Forzar actualización del filtro y mostrar dropdown
                    adapter.getFilter().filter(texto, new Filter.FilterListener() {
                        @Override
                        public void onFilterComplete(int count) {
                            if (count > 0 && autoCompleteGasolinera.hasFocus()) {
                                autoCompleteGasolinera.showDropDown();
                            } else {
                                autoCompleteGasolinera.dismissDropDown();
                            }
                        }
                    });
                } else {
                    // ✅ OCULTAR CUANDO ESTÁ VACÍO
                    autoCompleteGasolinera.dismissDropDown();
                    filtrosManager.setGasolinera(""); // Limpiar filtro

                    // ✅ LIMPIAR EL ADAPTER CUANDO NO HAY TEXTO
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }

                // ✅ APLICAR FILTROS AUTOMÁTICAMENTE
                aplicarFiltrosAutomaticos();
            }
        });

        // ✅ LISTENER PARA CUANDO SE SELECCIONA UN ITEM
        autoCompleteGasolinera.setOnItemClickListener((parent, view, position, id) -> {
            String gasolineraSeleccionada = (String) parent.getItemAtPosition(position);
            // El texto ya se setea automáticamente, solo actualizamos el filtro
            filtrosManager.setGasolinera(gasolineraSeleccionada);
            aplicarFiltrosAutomaticos();
        });

        // ✅ LISTENER PARA EL FOCO - mostrar sugerencias si hay texto
        autoCompleteGasolinera.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String textoActual = autoCompleteGasolinera.getText().toString().trim();
                if (textoActual.length() >= 1) {
                    handler.postDelayed(() -> {
                        if (autoCompleteGasolinera.hasFocus()) {
                            autoCompleteGasolinera.showDropDown();
                        }
                    }, 50);
                }
            }
        });

        Log.d("GeoGas", "✅ AutoComplete Gasolinera configurado con " + nombresGasolineras.size() + " nombres únicos");
    }

    /**
     * Configura el autocompletado para el filtro de provincias.
     */
    private void configurarAutoCompleteProvincia() {
        AutoCompleteTextView autoCompleteProvincia = findViewById(R.id.filter_provincia);

        // Obtener lista única de provincias
        final List<String> provinciasUnicas = obtenerProvinciasUnicas();

        // Adaptador compatible con API 26
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line
        ) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> suggestions = new ArrayList<>();

                        if (constraint == null || constraint.length() == 0) {
                            suggestions.clear();
                        } else {
                            String filterPattern = constraint.toString().toLowerCase().trim();

                            for (String item : provinciasUnicas) {
                                if (item.toLowerCase().startsWith(filterPattern)) {
                                    suggestions.add(item);
                                }
                                if (suggestions.size() >= 10) break;
                            }
                        }

                        results.values = suggestions;
                        results.count = suggestions.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        if (results.values != null) {
                            List<String> filteredList = (List<String>) results.values;
                            for (String item : filteredList) {
                                add(item);
                            }
                        }
                        notifyDataSetChanged();
                    }
                };
            }
        };

        autoCompleteProvincia.setAdapter(adapter);
        autoCompleteProvincia.setThreshold(1);

        // TextWatcher para actualizar en tiempo real
        autoCompleteProvincia.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String texto = s.toString().trim();

                // Actualizar filtro en tiempo real
                filtrosManager.setProvincia(texto);

                // Mostrar/ocultar dropdown según texto
                if (texto.length() >= 1) {
                    adapter.getFilter().filter(texto, new Filter.FilterListener() {
                        @Override
                        public void onFilterComplete(int count) {
                            if (count > 0 && autoCompleteProvincia.hasFocus()) {
                                autoCompleteProvincia.showDropDown();
                            } else {
                                autoCompleteProvincia.dismissDropDown();
                            }
                        }
                    });
                } else {
                    autoCompleteProvincia.dismissDropDown();
                    filtrosManager.setProvincia("");
                    adapter.clear();
                    adapter.notifyDataSetChanged();


                }

                aplicarFiltrosAutomaticos();
            }
        });

        // Listener para cuando se selecciona un item
        autoCompleteProvincia.setOnItemClickListener((parent, view, position, id) -> {
            String provinciaSeleccionada = (String) parent.getItemAtPosition(position);
            filtrosManager.setProvincia(provinciaSeleccionada);


            aplicarFiltrosAutomaticos();
        });

        // Listener para el foco
        autoCompleteProvincia.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String textoActual = autoCompleteProvincia.getText().toString().trim();
                if (textoActual.length() >= 1) {
                    handler.postDelayed(() -> {
                        if (autoCompleteProvincia.hasFocus()) {
                            autoCompleteProvincia.showDropDown();
                        }
                    }, 50);
                }
            }
        });

        Log.d("GeoGas", "✅ AutoComplete Provincia configurado con " + provinciasUnicas.size() + " provincias");
    }
    /**
     * Obtiene municipios por provincia.
     *
     * @param provincia nombre de la provincia
     * @return lista de municipios
     */
    private List<String> obtenerMunicipiosPorProvincia(String provincia) {
        Set<String> municipios = new HashSet<>();
        for (GasolineraAPI gasolinera : todasLasGasolineras) {
            if (provincia.equals(gasolinera.getProvincia()) &&
                    gasolinera.getMunicipio() != null && !gasolinera.getMunicipio().isEmpty()) {
                municipios.add(gasolinera.getMunicipio());
            }
        }
        List<String> lista = new ArrayList<>(municipios);
        Collections.sort(lista);
        return lista;
    }
    /**
     * Configura el autocompletado para el filtro de municipios.
     */
    private void configurarAutoCompleteMunicipio() {
        AutoCompleteTextView autoCompleteMunicipio = findViewById(R.id.filter_municipio);

        // Obtener lista única de TODOS los municipios
        final List<String> municipiosCompletos = obtenerMunicipiosUnicos();

        // ✅ CAMBIADO: Igual que provincia - SIN pasar lista al constructor
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line  // Sin lista aquí
        ) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        List<String> suggestions = new ArrayList<>();

                        if (constraint == null || constraint.length() == 0) {
                            suggestions.clear();
                        } else {
                            String filterPattern = constraint.toString().toLowerCase().trim();

                            for (String item : municipiosCompletos) {
                                if (item.toLowerCase().startsWith(filterPattern)) {
                                    suggestions.add(item);
                                }
                                if (suggestions.size() >= 10) break;
                            }
                        }

                        results.values = suggestions;
                        results.count = suggestions.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        if (results.values != null) {
                            List<String> filteredList = (List<String>) results.values;
                            for (String item : filteredList) {
                                add(item);
                            }
                        }
                        notifyDataSetChanged();
                    }
                };
            }
        };

        autoCompleteMunicipio.setAdapter(adapter);
        autoCompleteMunicipio.setThreshold(1);

        // TextWatcher para actualizar en tiempo real - IGUAL QUE PROVINCIA
        autoCompleteMunicipio.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String texto = s.toString().trim();

                // Actualizar filtro en tiempo real
                filtrosManager.setMunicipio(texto);

                // Mostrar/ocultar dropdown según texto
                if (texto.length() >= 1) {
                    adapter.getFilter().filter(texto, new Filter.FilterListener() {
                        @Override
                        public void onFilterComplete(int count) {
                            if (count > 0 && autoCompleteMunicipio.hasFocus()) {
                                autoCompleteMunicipio.showDropDown();
                            } else {
                                autoCompleteMunicipio.dismissDropDown();
                            }
                        }
                    });
                } else {
                    autoCompleteMunicipio.dismissDropDown();
                    filtrosManager.setMunicipio("");
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }

                aplicarFiltrosAutomaticos();
            }
        });

        // Listener para cuando se selecciona un item
        autoCompleteMunicipio.setOnItemClickListener((parent, view, position, id) -> {
            String municipioSeleccionado = (String) parent.getItemAtPosition(position);
            filtrosManager.setMunicipio(municipioSeleccionado);
            aplicarFiltrosAutomaticos();
        });

        // Listener para el foco
        autoCompleteMunicipio.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String textoActual = autoCompleteMunicipio.getText().toString().trim();
                if (textoActual.length() >= 1) {
                    handler.postDelayed(() -> {
                        if (autoCompleteMunicipio.hasFocus()) {
                            autoCompleteMunicipio.showDropDown();
                        }
                    }, 50);
                }
            }
        });

        Log.d("GeoGas", "✅ AutoComplete Municipio configurado con " + municipiosCompletos.size() + " municipios");
    }
    /**
     * Aplica los filtros automáticamente.
     */
    private void aplicarFiltrosAutomaticos() {
        // Aplicar filtros automáticamente cuando cambia el texto
        List<GasolineraAPI> gasolinerasFiltradas = filtrosManager.aplicarFiltros(todasLasGasolineras, favoritosManager);

        // Actualizar contador
        if (filterResultsCount != null) {
            filterResultsCount.setText(gasolinerasFiltradas.size() + " gasolineras encontradas");
        }

        // Actualizar mapa inmediatamente
        actualizarMapaConFiltros(gasolinerasFiltradas);

        // Actualizar icono del botón de filtros
        updateFilterButtonIcon();
    }
    /**
     * Obtiene provincias únicas.
     *
     * @return lista de provincias únicas
     */
    private List<String> obtenerProvinciasUnicas() {
        Set<String> provincias = new HashSet<>();
        for (GasolineraAPI gasolinera : todasLasGasolineras) {
            if (gasolinera.getProvincia() != null && !gasolinera.getProvincia().isEmpty()) {
                provincias.add(gasolinera.getProvincia());
            }
        }
        List<String> lista = new ArrayList<>(provincias);
        Collections.sort(lista);
        return lista; // ✅ QUITAR el límite de 10
    }
    /**
     * Obtiene municipios únicos.
     *
     * @return lista de municipios únicos
     */
    private List<String> obtenerMunicipiosUnicos() {
        Set<String> municipios = new HashSet<>();
        if (todasLasGasolineras != null) {
            for (GasolineraAPI gasolinera : todasLasGasolineras) {
                if (gasolinera.getMunicipio() != null && !gasolinera.getMunicipio().isEmpty()) {
                    municipios.add(gasolinera.getMunicipio());
                }
            }
        }
        List<String> lista = new ArrayList<>(municipios);
        Collections.sort(lista);
        return lista; // ✅ Devuelve TODOS los municipios únicos
    }

    /**
     * Obtiene marcas únicas de gasolineras.
     *
     * @return lista de marcas únicas
     */
    private List<String> obtenerMarcasUnicasDeGasolineras() {
        Set<String> marcasUnicas = new HashSet<>();
        if (todasLasGasolineras != null) {
            for (GasolineraAPI gasolinera : todasLasGasolineras) {
                if (gasolinera.getRotulo() != null && !gasolinera.getRotulo().isEmpty()) {
                    marcasUnicas.add(gasolinera.getRotulo());
                }
            }
        }
        List<String> lista = new ArrayList<>(marcasUnicas);
        Collections.sort(lista);
        return lista; // ✅ QUITAR el límite de 10
    }
    /**
     * Alterna la visibilidad del panel de filtros.
     */
    private void toggleFiltersPanel() {
        if (filtersPanel.getVisibility() == View.VISIBLE) {
            hideFiltersPanel();
        } else {
            showFiltersPanel();
        }
    }
    /**
     * Muestra el panel de filtros.
     */
    private void showFiltersPanel() {
        filtersPanel.setVisibility(View.VISIBLE);

        // Animación de entrada
        filtersPanel.setTranslationX(-filtersPanel.getWidth());
        filtersPanel.animate().translationX(0).setDuration(300).start();

        // ✅ NUEVO: Quitar el foco automático de cualquier campo
        clearAutoCompleteFocus();
    }
    /**
     * Limpia el foco de los campos autocompletables.
     */
    private void clearAutoCompleteFocus() {
        // Quitar foco de provincia
        if (filterProvincia != null) {
            filterProvincia.clearFocus();
            filterProvincia.dismissDropDown(); // Cerrar dropdown si está abierto
        }

        // Quitar foco de municipio
        if (filterMunicipio != null) {
            filterMunicipio.clearFocus();
            filterMunicipio.dismissDropDown();
        }

        // Quitar foco de marca
        AutoCompleteTextView filterMarca = findViewById(R.id.filter_gasolinera);
        if (filterMarca != null) {
            filterMarca.clearFocus();
            filterMarca.dismissDropDown();
        }

        // Dar foco al panel principal para que ningún campo lo tenga
        filtersPanel.requestFocus();
    }
    /**
     * Oculta el panel de filtros.
     */
    private void hideFiltersPanel() {
        // Animación de salida
        filtersPanel.animate().translationX(-filtersPanel.getWidth())
                .setDuration(300)
                .withEndAction(() -> filtersPanel.setVisibility(View.GONE))
                .start();
    }
    /**
     * Aplica los filtros seleccionados.
     */
    private void applyFilters() {
        // ✅ ACTUALIZAR: Obtener valores actuales de todos los campos
        AutoCompleteTextView filterMarca = findViewById(R.id.filter_gasolinera);

        // Recoger valores ACTUALES de los filtros
        filtrosManager.setProvincia(filterProvincia.getText().toString().trim());
        filtrosManager.setMunicipio(filterMunicipio.getText().toString().trim());
        filtrosManager.setGasolinera(filterMarca.getText().toString().trim());

        // Combustibles
        filtrosManager.setSoloGasolina95(filterGasolina95.isChecked());
        filtrosManager.setSoloGasolina98(filterGasolina98.isChecked());
        filtrosManager.setSoloDiesel(filterDiesel.isChecked());
        filtrosManager.setSoloDieselPremium(filterDieselPremium.isChecked());
        filtrosManager.setSoloGLP(filterGLP.isChecked());

        // Servicios
        filtrosManager.setSolo24Horas(filter24h.isChecked());
        filtrosManager.setSoloFavoritas(filterFavoritas.isChecked());

        // Precios
        try {
            String precioGasolina95 = filterPrecioMaxGasolina95.getText().toString().trim();
            if (!precioGasolina95.isEmpty()) {
                filtrosManager.setPrecioMaxGasolina95(Double.parseDouble(precioGasolina95));
            } else {
                filtrosManager.setPrecioMaxGasolina95(null);
            }

            String precioDiesel = filterPrecioMaxDiesel.getText().toString().trim();
            if (!precioDiesel.isEmpty()) {
                filtrosManager.setPrecioMaxDiesel(Double.parseDouble(precioDiesel));
            } else {
                filtrosManager.setPrecioMaxDiesel(null);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show();
            Log.e("GeoGas", "Error parseando precios: " + e.getMessage());
        }

        // ✅ LOG para debug
        Log.d("GeoGas", "Aplicando filtros: " +
                "Provincia=" + filtrosManager.getProvincia() +
                ", Municipio=" + filtrosManager.getMunicipio() +
                ", Marca=" + filtrosManager.getGasolinera() +
                ", Gasolina95=" + filtrosManager.isSoloGasolina95());

        // Aplicar filtros y actualizar mapa
        List<GasolineraAPI> gasolinerasFiltradas = filtrosManager.aplicarFiltros(todasLasGasolineras, favoritosManager);

        // Actualizar contador
        filterResultsCount.setText(gasolinerasFiltradas.size() + " gasolineras encontradas");

        // ✅ NUEVO: Forzar la actualización del mapa
        actualizarMapaConFiltros(gasolinerasFiltradas);

        // Actualizar icono del botón
        updateFilterButtonIcon();

        // Ocultar panel después de aplicar
        hideFiltersPanel();
    }
    /**
     * Limpia todos los filtros.
     */
    private void clearFilters() {
        // Limpiar todos los campos
        filterProvincia.setText("");
        filterMunicipio.setText("");



        // ✅ NUEVO: Limpiar el campo de marca
        AutoCompleteTextView filterMarca = findViewById(R.id.filter_gasolinera);
        filterMarca.setText("");

        // ELIMINAR el código de chips:


        filterGasolina95.setChecked(false);
        filterGasolina98.setChecked(false);
        filterDiesel.setChecked(false);
        filterDieselPremium.setChecked(false);
        filterGLP.setChecked(false);
        filter24h.setChecked(false);
        filterFavoritas.setChecked(false);
        filterPrecioMaxGasolina95.setText("");
        filterPrecioMaxDiesel.setText("");

        // Limpiar filtros
        filtrosManager.limpiarFiltros();

        // ✅ NUEVO: Forzar la actualización del mapa con todas las gasolineras
        actualizarMapaConFiltros(todasLasGasolineras);

        // Actualizar contador
        filterResultsCount.setText(todasLasGasolineras.size() + " gasolineras encontradas");

        // Actualizar icono del botón
        updateFilterButtonIcon();
    }

    /**
     * Actualiza el mapa inmediatamente con una lista de gasolineras
     *
     * @param gasolineras lista de gasolineras a mostrar
     */
    private void actualizarMapaConFiltros(List<GasolineraAPI> gasolineras) {
        if (map == null) return;

        // Obtener el viewport actual
        org.osmdroid.util.BoundingBox viewportActual = map.getBoundingBox();

        // Filtrar las gasolineras para el viewport actual
        List<GasolineraAPI> gasolinerasViewport = filtrarGasolinerasParaViewport(gasolineras, viewportActual);

        // Mostrar en el mapa
        showRealGasolinerasOnMap(gasolinerasViewport);

        Log.d("GeoGas", "✅ Filtros aplicados - " + gasolinerasViewport.size() + " gasolineras en viewport");
    }
    /**
     * Actualiza el icono del botón de filtros.
     */
    private void updateFilterButtonIcon() {
        if (toolbarFiltersButton != null && toolbarFiltersContainer != null) {
            if (filtrosManager.tieneFiltrosActivos()) {
                // Filtros activos - cambiar icono y fondo
                toolbarFiltersButton.setImageResource(R.drawable.ic_filter_active_pro);
                toolbarFiltersContainer.setBackground(
                        ContextCompat.getDrawable(this, R.drawable.btn_circle_background_small_active));
            } else {
                // Filtros inactivos - icono y fondo normales
                toolbarFiltersButton.setImageResource(R.drawable.ic_filter_pro);
                toolbarFiltersContainer.setBackground(
                        ContextCompat.getDrawable(this, R.drawable.btn_circle_background_small));
            }
        }
    }
    /**
     * Inicializa el splash screen nativo
     */
    private void initializeSplashScreen() {
        splashScreen = findViewById(R.id.splashScreen);
        splashSpinner = splashScreen.findViewById(R.id.splashSpinner);
        splashProgressBar = splashScreen.findViewById(R.id.splashProgressBar);
        splashProgressText = splashScreen.findViewById(R.id.splashProgressText);
        splashLoadingText = splashScreen.findViewById(R.id.splashLoadingText);

        // Asegurarse de que el splash es visible al inicio
        splashScreen.setVisibility(View.VISIBLE);
        splashScreen.setAlpha(1.0f);

        // Inicializar barra de progreso
        splashProgressBar.setProgress(0);
        splashProgressText.setText("0%");
        splashLoadingText.setText("Inicializando...");
        // ✅ Asegurar que el spinner esté visible
        if (splashSpinner != null) {
            splashSpinner.setVisibility(View.VISIBLE);
        }
        Log.d("GeoGas", "✅ Splash screen con barra de progreso iniciado");
    }
    /**
     * Actualiza el progreso del splash screen con un mensaje
     *
     * @param progress progreso de 0 a 100
     * @param message mensaje a mostrar
     */
    private void updateSplashProgress(int progress, String message) {
        runOnUiThread(() -> {
            if (splashProgressBar != null && splashProgressText != null && splashLoadingText != null) {
                // Solo actualizar si el progreso es mayor
                if (progress > splashProgress) {
                    splashProgress = progress;
                    splashProgressBar.setProgress(progress);
                    splashProgressText.setText(progress + "%");
                    splashLoadingText.setText(message);

                    // ✅ NUEVO: Cuando llegamos al 100%, ocultar el spinner
                    if (progress >= 100 && splashSpinner != null) {
                        splashSpinner.setVisibility(View.GONE);
                        Log.d("GeoGas", "⭕ Spinner ocultado al 100%");
                    }

                    Log.d("GeoGas", "📊 Progreso: " + progress + "% - " + message);
                }
            }
        });
    }
    /**
     * Incrementa el progreso gradualmente con animación
     *
     * @param increment incremento del progreso
     * @param message mensaje a mostrar
     */
    private void incrementSplashProgress(int increment, String message) {
        splashHandler.postDelayed(() -> {
            int newProgress = Math.min(splashProgress + increment, 100);
            updateSplashProgress(newProgress, message);
        }, 100); // Pequeño delay para que se vea la animación
    }
    /**
     * Establece el progreso directamente
     *
     * @param progress progreso de 0 a 100
     * @param message mensaje a mostrar
     */
    private void setSplashProgress(int progress, String message) {
        splashHandler.post(() -> {
            splashProgress = progress;
            updateSplashProgress(progress, message);
        });
    }
    /**
     * Verifica permisos y carga datos
     */
    private void checkPermissionsAndLoadData() {
        //updateLoadingText("Verificando permisos...");
        setSplashProgress(10, "Verificando permisos...");

        // Solicitar permisos de ubicación
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQ_PERMISSIONS);
        } else {
            onPermissionsChecked();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) {
            boolean granted = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                setSplashProgress(20, "Permisos concedidos - inicializando...");
                onPermissionsChecked();
            } else {
                setSplashProgress(20, "Permisos limitados - continuando...");
                onPermissionsChecked();
            }
        }
    }
    /**
     * Callback cuando los permisos han sido verificados.
     */
    private void onPermissionsChecked() {
        isPermissionsChecked = true;
        incrementSplashProgress(10, "Configurando mapa...");

        // Cargar datos después de tener los permisos
        loadRealData();
        checkIfShouldHideSplash();
    }
    /**
     * Marca el mapa como listo
     */
    private void onMapReady() {
        isMapReady = true;
        incrementSplashProgress(15, "Mapa cargado - obteniendo datos...");
        Log.d("GeoGas", "✅ Mapa listo");
        checkIfShouldHideSplash();
    }
    /**
     * Marca los datos como cargados
     */
    private void onDataLoaded() {
        isDataLoaded = true;
        incrementSplashProgress(15, "Configurando filtros...");
        Log.d("GeoGas", "✅ Datos cargados - " + todasLasGasolineras.size() + " gasolineras");

        // Mostrar botón de filtros ahora que tenemos datos
        runOnUiThread(() -> {
            //btnFilters.setVisibility(View.VISIBLE);

            // ✅ CONFIGURAR AUTOC0MPLETES SOLO CUANDO HAY DATOS
            if (!todasLasGasolineras.isEmpty()) {
                configurarAutoCompleteProvincia();
                configurarAutoCompleteMunicipio();
                configurarAutoCompleteGasolinera();
                incrementSplashProgress(10, "Preparando interfaz...");
                Log.d("GeoGas", "✅ Autocompletes configurados con " + todasLasGasolineras.size() + " gasolineras");
            } else {
                Log.w("GeoGas", "⚠️ No hay datos para configurar autocompletes");
            }
        });

        checkIfShouldHideSplash();
    }

    /**
     * Verifica si puede ocultar el splash screen
     */
    private void checkIfShouldHideSplash() {
        Log.d("GeoGas", String.format("🔍 Estado carga: Permisos=%b, Mapa=%b, Datos=%b",
                isPermissionsChecked, isMapReady, isDataLoaded));

        if (isPermissionsChecked && isMapReady && isDataLoaded) {
            // Pequeño delay para que se aprecie el splash
            setSplashProgress(100, "¡GeoGas listo!");
            new Handler().postDelayed(() -> {
                hideSplashScreen();
            }, 800);
        } else {
            updateLoadingMessage();
            //updateLoadingText();
        }
    }
    /**
     * Actualiza el mensaje de carga según el estado.
     */
    private void updateLoadingMessage() {
        if (!isPermissionsChecked) {
            updateSplashProgress(splashProgress, "Esperando permisos...");
        } else if (!isMapReady && !isDataLoaded) {
            updateSplashProgress(splashProgress, "Inicializando componentes...");
        } else if (!isMapReady) {
            updateSplashProgress(splashProgress, "Cargando mapa...");
        } else if (!isDataLoaded) {
            updateSplashProgress(splashProgress, "Obteniendo datos de gasolineras...");
        }
    }
    /**
     * Actualiza el texto de carga automáticamente según el estado
     */
    private void updateLoadingText() {
        updateLoadingText(getCurrentLoadingMessage());
    }
    /**
     * Actualiza el texto de carga con un mensaje específico
     *
     * @param message mensaje a mostrar
     */
    private void updateLoadingText(String message) {
        runOnUiThread(() -> {
            if (splashLoadingText != null) {
                splashLoadingText.setText(message);
                Log.d("GeoGas", "📝 Splash: " + message);
            }
        });
    }
    /**
     * Determina el mensaje de carga según el estado actual
     */
    private String getCurrentLoadingMessage() {
        if (!isPermissionsChecked) {
            return "Verificando permisos...";
        } else if (!isMapReady && !isDataLoaded) {
            return "Inicializando mapa...";
        } else if (!isMapReady) {
            return "Cargando mapa...";
        } else if (!isDataLoaded) {
            return "Cargando gasolineras...";
        } else {
            return "¡Listo!";
        }
    }
    /**
     * Oculta el splash screen con animación de desvanecimiento
     */
    private void hideSplashScreen() {
        runOnUiThread(() -> {
            if (splashScreen == null || splashScreen.getVisibility() == View.GONE) {
                return;
            }

            Log.d("GeoGas", "🎬 Ocultando splash screen...");

            // ✅ Asegurar que el spinner se oculte
            if (splashSpinner != null) {
                splashSpinner.setVisibility(View.GONE);
            }

            // Animación de fade out nativa
            splashScreen.animate()
                    .alpha(0f)
                    .setDuration(600)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            updateSplashProgress(100, "¡Bienvenido a GeoGas!");
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            splashScreen.setVisibility(View.GONE);
                            Log.d("GeoGas", "✅ Splash screen ocultado");
                            showMapControls();
                            // Opcional: mostrar un mensaje breve
                            Toast.makeText(MainActivity.this, "GeoGas cargado", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {}

                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    })
                    .start();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
    }
    /**
     * Muestra los controles del mapa.
     */
    private void showMapControls() {
        runOnUiThread(() -> {
            // Solo mostrar botón de ubicación (filtros ahora está en la barra)
            if (btnLocationContainer != null) {
                btnLocationContainer.setVisibility(View.VISIBLE);
                btnLocationContainer.setAlpha(0f);
                btnLocationContainer.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .setStartDelay(200)
                        .start();
            }


            Log.d("GeoGas", "🎮 Controles del mapa mostrados (solo ubicación)");
        });
    }
    /**
     * Oculta los controles del mapa.
     */
    private void hideMapControls() {
        runOnUiThread(() -> {
            if (btnLocationContainer != null) {
                btnLocationContainer.setVisibility(View.GONE);
            }
        });
    }
    /**
     * Inicializa el panel de detalles.
     */
    private void initializeDetailPanel() {
        // detailPanel ahora es un CardView, no ScrollView
        detailPanel = findViewById(R.id.detail_panel);
        detailPanel.setVisibility(View.GONE);
        // Buscar vistas dentro del panel - ahora necesitamos buscar dentro del CardView
        View cardContent = detailPanel; // El CardView contiene todo

        btnClosePanel = cardContent.findViewById(R.id.btn_close_panel);
        btnFavorite = cardContent.findViewById(R.id.btn_favorite);

        tvStationName = cardContent.findViewById(R.id.tv_station_name);
        tvStationAddress = cardContent.findViewById(R.id.tv_station_address);

        btnClosePanel.setOnClickListener(v -> hideDetailPanel());
        btnFavorite.setOnClickListener(v -> toggleFavorito());

        inicializarSecciones();
    }
    /**
     * Inicializa las secciones del panel de detalles.
     */
    private void inicializarSecciones() {
        // Configurar títulos de secciones
        setupSectionTitle(R.id.section_info, "Información de la Estación");
        setupSectionTitle(R.id.section_gasolina, "Precios de Gasolina");
        setupSectionTitle(R.id.section_diesel, "Precios de Diésel");
        setupSectionTitle(R.id.section_alternativos, "Combustibles Alternativos");
        setupSectionTitle(R.id.section_adicional, "Información Adicional");
    }
    /**
     * Configura el título de una sección.
     *
     * @param sectionId ID de la sección
     * @param title título de la sección
     */
    private void setupSectionTitle(int sectionId, String title) {
        View section = detailPanel.findViewById(sectionId);
        if (section != null) {
            TextView titleView = section.findViewById(R.id.section_title);
            if (titleView != null) {
                titleView.setText(title);
            }
        }
    }
    /**
     * Muestra los detalles de una gasolinera.
     *
     * @param gasolinera gasolinera a mostrar
     */
    private void showGasolineraAPIDetails(GasolineraAPI gasolinera) {
        this.gasolineraActual = gasolinera;

        // 1. INFORMACIÓN PRINCIPAL
        updateBasicInfo(gasolinera);

        // 2. PRECIOS ORGANIZADOS POR CATEGORÍAS
        updatePreciosGasolina(gasolinera);
        updatePreciosDiesel(gasolinera);
        updateCombustiblesAlternativos(gasolinera);
        updateInformacionAdicional(gasolinera);

        // 3. ACTUALIZAR BOTÓN FAVORITO
        actualizarBotonFavorito();

        // 4. MOSTRAR PANEL
        showDetailPanel();
    }
    /**
     * Actualiza la información básica de la gasolinera.
     *
     * @param gasolinera gasolinera con los datos
     */
    private void updateBasicInfo(GasolineraAPI gasolinera) {
        // Nombre
        tvStationName.setText(gasolinera.getRotulo());

        // Dirección
        tvStationAddress.setText(gasolinera.getDireccion());

        // Ubicación completa
        String ubicacion = String.format("%s, %s (%s)",
                gasolinera.getLocalidad(),
                gasolinera.getMunicipio(),
                gasolinera.getProvincia());
        TextView tvLocation = detailPanel.findViewById(R.id.tv_location);
        tvLocation.setText(ubicacion);

        // Horario (si está disponible)
        LinearLayout horarioContainer = detailPanel.findViewById(R.id.horario_container);
        TextView tvHorario = detailPanel.findViewById(R.id.tv_horario);

        if (gasolinera.getHorario() != null && !gasolinera.getHorario().isEmpty()) {
            tvHorario.setText(gasolinera.getHorario());
            horarioContainer.setVisibility(View.VISIBLE);
        } else {
            horarioContainer.setVisibility(View.GONE);
        }
    }
    /**
     * Actualiza los precios de gasolina.
     *
     * @param gasolinera gasolinera con los precios
     */
    private void updatePreciosGasolina(GasolineraAPI gasolinera) {
        TableLayout table = detailPanel.findViewById(R.id.table_gasolina);
        table.removeAllViews();

        List<PrecioItem> precios = new ArrayList<>();

        addPrecioSiExiste(precios, "Gasolina 95 E5", gasolinera.getPrecioGasolina95());
        addPrecioSiExiste(precios, "Gasolina 95 E10", gasolinera.getPrecioGasolina95E10());
        addPrecioSiExiste(precios, "Gasolina 98 E5", gasolinera.getPrecioGasolina98());
        addPrecioSiExiste(precios, "Gasolina 98 E10", gasolinera.getPrecioGasolina98E10());

        if (precios.isEmpty()) {
            addFilaSinDatos(table, "No hay precios de gasolina disponibles");
        } else {
            for (PrecioItem precio : precios) {
                addFilaPrecio(table, precio.nombre, precio.precio);
            }
        }
    }
    /**
     * Actualiza los precios de diésel.
     *
     * @param gasolinera gasolinera con los precios
     */
    private void updatePreciosDiesel(GasolineraAPI gasolinera) {
        TableLayout table = detailPanel.findViewById(R.id.table_diesel);
        table.removeAllViews();

        List<PrecioItem> precios = new ArrayList<>();

        addPrecioSiExiste(precios, "Diésel Standard", gasolinera.getPrecioGasoleoA());
        addPrecioSiExiste(precios, "Diésel Premium", gasolinera.getPrecioGasoleoPremium());
        addPrecioSiExiste(precios, "Diésel Agrícola", gasolinera.getPrecioGasoleoB());
        addPrecioSiExiste(precios, "Diésel Calefacción)", gasolinera.getPrecioGasoleoC());

        if (precios.isEmpty()) {
            addFilaSinDatos(table, "No hay precios de diésel disponibles");
        } else {
            for (PrecioItem precio : precios) {
                addFilaPrecio(table, precio.nombre, precio.precio);
            }
        }
    }
    /**
     * Actualiza los combustibles alternativos.
     *
     * @param gasolinera gasolinera con los precios
     */
    private void updateCombustiblesAlternativos(GasolineraAPI gasolinera) {
        TableLayout table = detailPanel.findViewById(R.id.table_alternativos);
        table.removeAllViews();

        List<PrecioItem> precios = new ArrayList<>();

        addPrecioSiExiste(precios, "GLP", gasolinera.getPrecioGLP());
        addPrecioSiExiste(precios, "GNC", gasolinera.getPrecioGNC());
        addPrecioSiExiste(precios, "GNL", gasolinera.getPrecioGNL());
        addPrecioSiExiste(precios, "Hidrógeno", gasolinera.getPrecioHidrogeno());
        addPrecioSiExiste(precios, "Biodiésel", gasolinera.getPrecioBiodiesel());
        addPrecioSiExiste(precios, "Bioetanol", gasolinera.getPrecioBioetanol());

        if (precios.isEmpty()) {
            addFilaSinDatos(table, "No hay combustibles alternativos");
        } else {
            for (PrecioItem precio : precios) {
                addFilaPrecio(table, precio.nombre, precio.precio);
            }
        }
    }
    /**
     * Actualiza la información adicional.
     *
     * @param gasolinera gasolinera con los datos
     */
    private void updateInformacionAdicional(GasolineraAPI gasolinera) {
        TableLayout table = detailPanel.findViewById(R.id.table_adicional);
        table.removeAllViews();

        List<InfoItem> infoItems = new ArrayList<>();

        addInfoSiExiste(infoItems, "Fecha actualización", gasolinera.getFecha());
        addInfoSiExiste(infoItems, "Tipo venta", gasolinera.getTipoVenta());
        addInfoSiExiste(infoItems, "Margen", gasolinera.getMargen());
        addInfoSiExiste(infoItems, "Código Postal", gasolinera.getCodigoPostal());

        if (infoItems.isEmpty()) {
            addFilaSinDatos(table, "No hay información adicional");
        } else {
            for (InfoItem item : infoItems) {
                addFilaInfo(table, item.nombre, item.valor);
            }
        }
    }
    /**
     * Actualiza el botón de favorito.
     */
    private void actualizarBotonFavorito() {
        if (gasolineraActual != null && gasolineraActual.getId() != null) {
            boolean esFavorita = favoritosManager.esFavorita(gasolineraActual.getId());

            if (esFavorita) {
                // Cambiar a estrella amarilla rellena
                btnFavorite.setImageResource(R.drawable.ic_star_filled);
                btnFavorite.setContentDescription("Quitar de favoritos");
                // Opcional: cambiar el tint a amarillo
                btnFavorite.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_orange_light));
            } else {
                // Cambiar a estrella blanca/contorno
                btnFavorite.setImageResource(R.drawable.ic_star_outline);
                btnFavorite.setContentDescription("Añadir a favoritos");
                // Cambiar el tint a blanco
                btnFavorite.setColorFilter(ContextCompat.getColor(this, android.R.color.white));
            }
        }
    }
    /**
     * Alterna el estado de favorito de la gasolinera actual.
     */
    private void toggleFavorito() {
        if (gasolineraActual != null && gasolineraActual.getId() != null) {
            Log.d("Favoritos", "Antes de toggle - ID: " + gasolineraActual.getId());
            Log.d("Favoritos", "Favoritos actuales: " + favoritosManager.getFavoritos().toString());
            favoritosManager.toggleFavorito(gasolineraActual.getId());

            // Mostrar feedback al usuario
            boolean ahoraEsFavorita = favoritosManager.esFavorita(gasolineraActual.getId());
            Log.d("Favoritos", "Después de toggle - Es favorita: " + ahoraEsFavorita);
            Log.d("Favoritos", "Favoritos después: " + favoritosManager.getFavoritos().toString());


            if (ahoraEsFavorita) {
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            }

            // Actualizar el texto del botón
            actualizarBotonFavorito();

            actualizarMarcadoresFavoritos();
        } else {
            Toast.makeText(this, "Error: No se puede gestionar favoritos", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Actualiza todos los marcadores de gasolineras cuando cambia el estado de favoritos
     * Manteniendo el tamaño de icono según la densidad y zoom actual
     */
    private void actualizarMarcadoresFavoritos() {
        // Obtener zoom actual y contar gasolineras para determinar densidad
        double zoomActual = map.getZoomLevel();
        int totalGasolineras = 0;

        for (org.osmdroid.views.overlay.Overlay overlay : map.getOverlays()) {
            if (overlay instanceof Marker) {
                Object relatedObject = ((Marker) overlay).getRelatedObject();
                if (relatedObject instanceof GasolineraAPI) {
                    totalGasolineras++;
                }
            }
        }

        // Actualizar cada marcador con la densidad actual
        for (org.osmdroid.views.overlay.Overlay overlay : map.getOverlays()) {
            if (overlay instanceof Marker) {
                Marker marker = (Marker) overlay;
                Object relatedObject = marker.getRelatedObject();

                if (relatedObject instanceof GasolineraAPI) {
                    GasolineraAPI gasolinera = (GasolineraAPI) relatedObject;

                    if (gasolinera.getId() != null) {
                        boolean esFavorita = favoritosManager.esFavorita(gasolinera.getId());

                        // Usar la densidad y zoom actual para el icono
                        int iconoResId = iconosManager.obtenerIconoGasolinera(esFavorita, totalGasolineras, zoomActual);
                        marker.setIcon(ContextCompat.getDrawable(this, iconoResId));

                        if (esFavorita) {
                            marker.setTitle("★ " + gasolinera.getRotulo());
                        } else {
                            marker.setTitle(gasolinera.getRotulo());
                        }
                    }
                }
            }
        }

        map.invalidate();

        String modoIcono = iconosManager.obtenerInfoTamañoIcono(totalGasolineras, zoomActual);
        Log.d("GeoGas", String.format("✅ Favoritos actualizados - Densidad: %d, Zoom: %.1f, Iconos: %s",
                totalGasolineras, zoomActual, modoIcono));
    }

    /**
     * Muestra el panel de detalles.
     */
    private void showDetailPanel() {
        detailPanel.setVisibility(View.VISIBLE);
    }
    /**
     * Oculta el panel de detalles.
     */
    private void hideDetailPanel() {
        detailPanel.setVisibility(View.GONE);
    }
    /**
     * Inicializa el mapa OSMDroid.
     */
    private void initializeMap() {
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false); // Usaremos nuestros controles



        // Vista inicial (Madrid)
        GeoPoint startPoint = new GeoPoint(40.4168, -3.7038);
        map.getController().setZoom(15.0);
        map.getController().setCenter(startPoint);

        setupMapChangeListener();

        // Marcar el mapa como listo después de un breve delay para que se renderice
        new Handler().postDelayed(() -> {
            onMapReady();
            addCompassOverlay();
            addOSMCopyright();
        }, 1200);
    }

    /**
     * Añade la brújula al final de los overlays
     * para que quede por encima de los marcadores
     */
    private void addCompassOverlay() {
        if (map == null) return;

        // Crear y configurar la brújula
        CompassOverlay compassOverlay = new CompassOverlay(this,
                new InternalCompassOrientationProvider(this), map);
        compassOverlay.enableCompass();

        // Añadir al FINAL de la lista de overlays (encima de todo)
        map.getOverlays().add(compassOverlay);

        // Forzar redibujado
        map.invalidate();

        Log.d("GeoGas", "✅ Brújula añadida encima de los marcadores");
    }

    /**
     * Configura el listener para detectar cuando el usuario mueve o hace zoom en el mapa
     */
    private void setupMapChangeListener() {
        map.addMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                // Usar un pequeño delay para evitar muchas llamadas durante el scroll
                handler.removeCallbacks(viewportChangeRunnable);
                handler.postDelayed(viewportChangeRunnable, 300); // 300ms de delay
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                handler.removeCallbacks(viewportChangeRunnable);
                handler.postDelayed(viewportChangeRunnable, 300);
                return false;
            }
        });
    }
    /**
     * Configura la interfaz de usuario.
     */
    private void setupUI() {
        // Verificar que btnMyLocation no sea null
        if (btnMyLocation == null) {
            Log.e("GeoGas", "❌ btnMyLocation es null en setupUI(), intentando recuperar...");
            btnMyLocation = findViewById(R.id.btn_my_location);
        }

        if (btnMyLocation != null) {
            btnMyLocation.setOnClickListener(v -> centerOnMyLocation());
        } else {
            Log.e("GeoGas", "❌❌ btnMyLocation sigue siendo null después de intentar recuperarlo");
            Toast.makeText(this, "Error: Botón de ubicación no encontrado", Toast.LENGTH_SHORT).show();
        }

    }
    /**
     * Carga los datos reales de la API.
     */
    private void loadRealData() {

        setSplashProgress(30, "Conectando con servidor...");

        //updateLoadingText("Conectando con servidor...");
        MitecoApiService apiService = ApiClient.getClient().create(MitecoApiService.class);

        Call<JsonElement> call = apiService.obtenerGasolineras();
        call.enqueue(new retrofit2.Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {

                    incrementSplashProgress(20, "Datos recibidos - procesando...");
                    JsonElement jsonElement = response.body();

                    // Verifica si es array directo u objeto
                    if (jsonElement.isJsonArray()) {
                        Log.d("GeoGas", "✓ La API devuelve un ARRAY JSON directamente");
                        Log.d("GeoGas", "Primeros 500 caracteres: " + jsonElement.toString().substring(0, Math.min(500, jsonElement.toString().length())));

                        // Parsear como lista directa
                        parseJsonArray(jsonElement);
                    } else if (jsonElement.isJsonObject()) {
                        Log.d("GeoGas", "✓ La API devuelve un OBJETO JSON");
                        Log.d("GeoGas", "Claves del objeto: " + jsonElement.getAsJsonObject().keySet());

                        // Buscar el campo que contiene la lista
                        parseJsonObject(jsonElement.getAsJsonObject());
                    }

                    datosCargados = true;
                    mostrarGasolinerasParaViewportActual();
                    onDataLoaded();
                } else {
                    Log.e("GeoGas", "Error en respuesta: " + response.message());
                    //updateLoadingText("Error en servidor - continuando...");
                    incrementSplashProgress(20, "Error en servidor - continuando...");
                    onDataLoaded(); // Aún así marcar como cargado
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e("GeoGas", "Error de conexión: " + t.getMessage());
                incrementSplashProgress(20, "Sin conexión - usando datos locales");
                //updateLoadingText("Sin conexión - usando datos locales");
                onDataLoaded(); // Aún así marcar como cargado
            }
        });
    }
    /**
     * Muestra las gasolineras para el viewport actual
     * Versión básica sin cache espacial - solo evita repintado completo
     *
     * @param gasolineras lista de gasolineras a considerar
     */
    private void mostrarGasolinerasParaViewportActual(List<GasolineraAPI> gasolineras) {
        if (!datosCargados || map == null || gasolineras == null) {
            return;
        }

        org.osmdroid.util.BoundingBox viewportActual = map.getBoundingBox();

        // Evitar recargas innecesarias si el viewport no cambió significativamente
        if (ultimoViewport != null && viewportActual.equals(ultimoViewport)) {
            return;
        }

        ultimoViewport = viewportActual;

        // Filtrar gasolineras para el viewport actual (siempre cálculo fresco)
        List<GasolineraAPI> gasolinerasViewport = filtrarGasolinerasParaViewport(gasolineras, viewportActual);

        // Mostrar en el mapa con gestión incremental
        showRealGasolinerasOnMap(gasolinerasViewport);

        Log.d("GeoGas", "🔄 Viewport cambiado - " + gasolinerasViewport.size() + " gasolineras");
    }
    /**
     * Parsea un array JSON de gasolineras.
     *
     * @param jsonElement elemento JSON a parsear
     */
    private void parseJsonArray(JsonElement jsonElement) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<GasolineraAPI>>(){}.getType();
            List<GasolineraAPI> gasolineras = gson.fromJson(jsonElement, listType);

            if (gasolineras != null && !gasolineras.isEmpty()) {

                todasLasGasolineras.clear();
                todasLasGasolineras.addAll(gasolineras);

                Log.d("GeoGas", "✓ Cacheadas " + todasLasGasolineras.size() + " gasolineras totales");
                Toast.makeText(this, "Cargadas " + gasolineras.size() + " gasolineras", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("GeoGas", "Lista parseada está vacía");
            }
        } catch (Exception e) {
            Log.e("GeoGas", "Error parseando array: " + e.getMessage());
        }
    }
    /**
     * Filtra las gasolineras para mostrar solo las del viewport actual
     * Usa un algoritmo simple de distancia para no saturar el mapa
     */
    /**
     * Filtra las gasolineras para mostrar solo las del viewport actual
     * Ahora recibe la lista a filtrar como parámetro
     *
     * @param gasolineras lista completa de gasolineras
     * @param viewport área visible del mapa
     * @return lista filtrada de gasolineras
     */
    private List<GasolineraAPI> filtrarGasolinerasParaViewport(List<GasolineraAPI> gasolineras, org.osmdroid.util.BoundingBox viewport) {
        List<PuntuadorGasolineras.GasolineraPuntuada> gasolinerasPuntuadas = new ArrayList<>();

        if (gasolineras.isEmpty()) {
            return new ArrayList<>();
        }

        // Paso 1: Filtrar gasolineras dentro del viewport
        List<GasolineraAPI> gasolinerasEnViewport = new ArrayList<>();
        for (GasolineraAPI gasolinera : gasolineras) {
            if (gasolinera.getLatitud() != null && gasolinera.getLongitud() != null &&
                    !gasolinera.getLatitud().isEmpty() && !gasolinera.getLongitud().isEmpty()) {

                try {
                    double latitud = Double.parseDouble(gasolinera.getLatitud().replace(",", "."));
                    double longitud = Double.parseDouble(gasolinera.getLongitud().replace(",", "."));

                    if (viewport.contains(latitud, longitud)) {
                        gasolinerasEnViewport.add(gasolinera);
                    }
                } catch (NumberFormatException e) {
                    // Coordenadas inválidas, saltar
                }
            }
        }

        // Paso 2: Usar el puntuador para ordenar por relevancia
        gasolinerasPuntuadas = puntuadorGasolineras.ordenarPorPuntuacion(gasolinerasEnViewport);

        // Paso 3: Tomar las mejores hasta el límite
        List<GasolineraAPI> gasolinerasFiltradas = new ArrayList<>();
        int limite = Math.min(MAX_GASOLINERAS_VISIBLES, gasolinerasPuntuadas.size());

        for (int i = 0; i < limite; i++) {
            gasolinerasFiltradas.add(gasolinerasPuntuadas.get(i).gasolinera);
        }

        // Log informativo
        if (!gasolinerasPuntuadas.isEmpty()) {
            double mejorPuntuacion = gasolinerasPuntuadas.get(0).puntuacion;
            double peorPuntuacion = gasolinerasPuntuadas.get(Math.min(limite-1, gasolinerasPuntuadas.size()-1)).puntuacion;
            Log.d("GeoGas", String.format("Puntuaciones - Mejor: %.2f, Peor seleccionada: %.2f",
                    mejorPuntuacion, peorPuntuacion));
        }

        Log.d("GeoGas", "Seleccionadas " + gasolinerasFiltradas.size() + " gasolineras más relevantes de " +
                gasolinerasEnViewport.size() + " disponibles en el viewport");

        return gasolinerasFiltradas;
    }
    /**
     * Usa todas las gasolineras por defecto
     */
    private void mostrarGasolinerasParaViewportActual() {
        mostrarGasolinerasParaViewportActual(todasLasGasolineras);
    }
    /**
     * Parsea un objeto JSON buscando el array de gasolineras.
     *
     * @param jsonObject objeto JSON a parsear
     */
    private void parseJsonObject(com.google.gson.JsonObject jsonObject) {
        // Buscar el campo que contiene las gasolineras
        String[] possibleFieldNames = {"ListaEESSPrecio", "listaEESSPrecio", "data", "result", "estaciones", "gasolineras"};

        for (String fieldName : possibleFieldNames) {
            if (jsonObject.has(fieldName) && jsonObject.get(fieldName).isJsonArray()) {
                Log.d("GeoGas", "✓ Encontrado campo: " + fieldName);
                parseJsonArray(jsonObject.get(fieldName));
                return;
            }
        }

        // Si no encontramos el campo
        Log.e("GeoGas", "No se encontró el campo con la lista de gasolineras. Campos disponibles: " + jsonObject.keySet());
        //loadSampleData();
    }

    /**
     * Muestra las gasolineras en el mapa de forma optimizada, evitando el repintado completo
     * Solo actualiza los marcadores que cambiaron en lugar de eliminar y recrear todos
     *
     * @param gasolinerasAPI Lista de gasolineras a mostrar en el viewport actual
     */
    private void showRealGasolinerasOnMap(List<GasolineraAPI> gasolinerasAPI) {
        if (gasolinerasAPI == null || gasolinerasAPI.isEmpty()) {
            limpiarMarcadoresNoEspeciales();
            return;
        }

        // Obtener zoom actual para determinar densidad de iconos
        double zoomActual = map.getZoomLevel();
        int totalGasolineras = gasolinerasAPI.size();

        // 1. Crear un mapa temporal de las nuevas gasolineras indexadas por ID
        Map<String, GasolineraAPI> nuevasGasolinerasMap = new HashMap<>();
        for (GasolineraAPI gasolinera : gasolinerasAPI) {
            if (gasolinera.getId() != null) {
                nuevasGasolinerasMap.put(gasolinera.getId(), gasolinera);
            }
        }

        // 2. Lista para almacenar los marcadores que deben eliminarse
        List<Marker> marcadoresAEliminar = new ArrayList<>();

        // 3. Recorrer todos los overlays del mapa para identificar cambios
        for (org.osmdroid.views.overlay.Overlay overlay : map.getOverlays()) {
            if (overlay instanceof Marker) {
                Marker marker = (Marker) overlay;
                Object relatedObject = marker.getRelatedObject();

                if (relatedObject instanceof GasolineraAPI) {
                    GasolineraAPI gasolineraExistente = (GasolineraAPI) relatedObject;

                    if (gasolineraExistente.getId() != null) {
                        GasolineraAPI nuevaGasolinera = nuevasGasolinerasMap.get(gasolineraExistente.getId());

                        if (nuevaGasolinera != null) {
                            // El marcador existe en ambas listas - ACTUALIZAR SIEMPRE el icono por densidad
                            actualizarMarcadorPorDensidad(marker, nuevaGasolinera, totalGasolineras, zoomActual);
                            // Quitar del mapa temporal para evitar crear duplicado
                            nuevasGasolinerasMap.remove(gasolineraExistente.getId());
                        } else {
                            // El marcador ya no está en la nueva lista - MARCAR PARA ELIMINAR
                            marcadoresAEliminar.add(marker);
                        }
                    }
                }
            }
        }

        // 4. Eliminar marcadores que ya no están en el viewport actual
        for (Marker marker : marcadoresAEliminar) {
            map.getOverlays().remove(marker);
        }

        // 5. Añadir SOLO los marcadores nuevos (los que no existían previamente)
        for (GasolineraAPI nuevaGasolinera : nuevasGasolinerasMap.values()) {
            Marker nuevoMarcador = crearMarcadorConDensidad(nuevaGasolinera, totalGasolineras, zoomActual);
            if (nuevoMarcador != null) {
                map.getOverlays().add(nuevoMarcador);
            }
        }

        map.invalidate();

        // Log informativo
        String modoIcono = iconosManager.obtenerInfoTamañoIcono(totalGasolineras, zoomActual);
        Log.d("GeoGas", String.format("🔄 Densidad: %d gasolineras, Zoom: %.1f, Iconos: %s",
                totalGasolineras, zoomActual, modoIcono));
    }
    /**
     * Actualiza un marcador existente para que use el icono correspondiente
     * a la densidad y zoom actual - FORZANDO consistencia
     *
     * @param marker Marcador existente a actualizar
     * @param gasolinera Datos de la gasolinera
     * @param totalGasolineras Número total de gasolineras para determinar densidad
     * @param zoom Nivel de zoom actual del mapa
     */
    private void actualizarMarcadorPorDensidad(Marker marker, GasolineraAPI gasolinera,
                                               int totalGasolineras, double zoom) {
        try {
            // Siempre actualizamos el icono según la densidad y zoom actual
            boolean esFavorita = favoritosManager.esFavorita(gasolinera.getId());
            int iconoResId = iconosManager.obtenerIconoGasolinera(esFavorita, totalGasolineras, zoom);
            marker.setIcon(ContextCompat.getDrawable(this, iconoResId));

            // También actualizamos el título por si cambió el estado de favoritos
            if (esFavorita) {
                marker.setTitle("★ " + gasolinera.getRotulo());
            } else {
                marker.setTitle(gasolinera.getRotulo());
            }

            // Actualizar el snippet (precios) por si cambiaron
            StringBuilder snippet = new StringBuilder();
            if (gasolinera.getPrecioGasolina95() != null && !gasolinera.getPrecioGasolina95().isEmpty()) {
                snippet.append("95: ").append(gasolinera.getPrecioGasolina95()).append("€\n");
            }
            if (gasolinera.getPrecioGasoleoA() != null && !gasolinera.getPrecioGasoleoA().isEmpty()) {
                snippet.append("Diésel: ").append(gasolinera.getPrecioGasoleoA()).append("€");
            }
            marker.setSnippet(snippet.toString());

        } catch (Exception e) {
            Log.e("GeoGas", "Error actualizando marcador por densidad: " + gasolinera.getRotulo());
        }
    }
    /**
     * Crea un nuevo marcador para una gasolinera, usando el icono según
     * la densidad y zoom actual
     *
     * @param gasolinera Datos de la gasolinera
     * @param totalGasolineras Número total de gasolineras para determinar densidad
     * @param zoom Nivel de zoom actual del mapa
     * @return Nuevo marcador creado
     */
    private Marker crearMarcadorConDensidad(GasolineraAPI gasolinera, int totalGasolineras, double zoom) {
        try {
            double latitud = Double.parseDouble(gasolinera.getLatitud().replace(",", "."));
            double longitud = Double.parseDouble(gasolinera.getLongitud().replace(",", "."));

            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(latitud, longitud));

            // Usar el icono según la densidad y zoom actual
            boolean esFavorita = favoritosManager.esFavorita(gasolinera.getId());
            int iconoResId = iconosManager.obtenerIconoGasolinera(esFavorita, totalGasolineras, zoom);
            marker.setIcon(ContextCompat.getDrawable(this, iconoResId));

            if (esFavorita) {
                marker.setTitle("★ " + gasolinera.getRotulo());
            } else {
                marker.setTitle(gasolinera.getRotulo());
            }

            StringBuilder snippet = new StringBuilder();
            if (gasolinera.getPrecioGasolina95() != null && !gasolinera.getPrecioGasolina95().isEmpty()) {
                snippet.append("95: ").append(gasolinera.getPrecioGasolina95()).append("€\n");
            }
            if (gasolinera.getPrecioGasoleoA() != null && !gasolinera.getPrecioGasoleoA().isEmpty()) {
                snippet.append("Diésel: ").append(gasolinera.getPrecioGasoleoA()).append("€");
            }
            marker.setSnippet(snippet.toString());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            marker.setRelatedObject(gasolinera);

            marker.setOnMarkerClickListener((marker1, mapView) -> {
                showGasolineraAPIDetails((GasolineraAPI) marker1.getRelatedObject());
                return true;
            });

            return marker;

        } catch (NumberFormatException e) {
            Log.e("GeoGas", "Coordenadas inválidas para: " + gasolinera.getRotulo());
            return null;
        }
    }
    /**
     * Limpia solo los marcadores de gasolineras, manteniendo los overlays especiales
     * (ubicación del usuario, brújula, escala, etc.)
     * Se usa cuando no hay gasolineras para mostrar en el viewport actual
     */
    private void limpiarMarcadoresNoEspeciales() {
        // Lista para almacenar los overlays que deben conservarse
        List<org.osmdroid.views.overlay.Overlay> overlaysToKeep = new ArrayList<>();

        // Recorrer todos los overlays actuales
        for (org.osmdroid.views.overlay.Overlay overlay : map.getOverlays()) {
            // Conservar solo los overlays especiales (no marcadores de gasolineras)
            if (overlay instanceof MyLocationNewOverlay ||           // Ubicación del usuario
                    overlay instanceof CompassOverlay ||                 // Brújula
                    overlay instanceof org.osmdroid.views.overlay.ScaleBarOverlay) { // Escala
                overlaysToKeep.add(overlay);
            }
            // Los marcadores de gasolineras no se añaden a overlaysToKeep, por lo que se eliminarán
        }

        // Limpiar todos los overlays y restaurar solo los especiales
        map.getOverlays().clear();
        map.getOverlays().addAll(overlaysToKeep);
    }
    /**
     * Centra el mapa en la ubicación actual del usuario.
     */
    private void centerOnMyLocation() {
        if (!hasLocationPermissions()) {
            Toast.makeText(this, "Permisos de ubicación no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usar el LocationManager para obtener la última ubicación conocida
        android.location.LocationManager locationManager =
                (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            Location lastKnownLocation = null;

            if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
                lastKnownLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            }

            if (lastKnownLocation == null && locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
                lastKnownLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                GeoPoint userLocation = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                map.getController().animateTo(userLocation);
                Toast.makeText(this, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ubicación no disponible. Intenta moverte un poco.", Toast.LENGTH_SHORT).show();
            }

        } catch (SecurityException e) {
            Toast.makeText(this, "Error de permisos de ubicación", Toast.LENGTH_SHORT).show();
            Log.e("GeoGas", "Error de permisos en centerOnMyLocation: " + e.getMessage());
        }
    }
    /**
     * Verifica si la aplicación tiene permisos de ubicación.
     *
     * @return true si tiene permisos, false en caso contrario
     */
    private boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}