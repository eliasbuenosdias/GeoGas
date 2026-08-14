# Patrones de Diseño GoF en GeoGas 6

> Análisis basado en *Sumérgete en los Patrones de Diseño* — Alexander Shvets

---

## 🏭 Patrones Creacionales

### 1. Singleton — `ApiClient`
**Archivo**: [`ApiClient.java`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/api/ApiClient.java)

```java
private static Retrofit retrofit = null;

public static Retrofit getClient() {
    if (retrofit == null) {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    return retrofit;
}
```

**Diagnóstico**: Implementación clásica de Singleton *lazy* (inicialización diferida). Una única instancia de `Retrofit` es compartida por toda la app.

> [!NOTE]
> Es un Singleton no thread-safe (sin `synchronized`). Funciona en Android porque `getClient()` siempre se llama desde el hilo principal en el inicio.

---

### 2. Factory Method — `IconosManager`
**Archivo**: [`IconosManager.java`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/utils/IconosManager.java)

```java
// El método centraliza la "creación" de un recurso (icono) según condiciones
public int obtenerIconoGasolinera(boolean esFavorita, int totalGasolineras, double zoom) {
    if (totalGasolineras > umbralMuyAltaDensidad) {
        return esFavorita ? R.drawable.ic_star_tiny : R.drawable.ic_gas_station_tiny;
    } else if (totalGasolineras > umbralAltaDensidad) {
        return esFavorita ? R.drawable.ic_star_small : R.drawable.ic_gas_station_small;
    } else {
        return esFavorita ? R.drawable.ic_star : R.drawable.ic_gas_station;
    }
}
```

**Diagnóstico**: `obtenerIconoGasolinera` actúa como una *Static Factory Method*. El cliente (`MapHelper`) no decide qué icono usar; delega esa lógica al `IconosManager`. La "familia de productos" son los 6 posibles iconos (normal/small/tiny × favorita/normal).

---

## 🧱 Patrones Estructurales

### 3. Facade — `MapHelper`, `SplashHelper`, `PermissionHelper`
**Archivo**: [`MapHelper.java`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/ui/helpers/MapHelper.java)

```java
// MainActivity no habla directamente con osmdroid:
mapHelper = new MapHelper(mapView, iconosManager, favoritosManager);
mapHelper.setOnMarkerClickListener(g -> { ... });
mapHelper.updateMarkers(list);
mapHelper.refreshMarkers();
```

**Diagnóstico**: `MapHelper` es una **Fachada** sobre la complejidad de `osmdroid` (MapView, Overlay, Marker, CompassOverlay...). `MainActivity` solo conoce 4 métodos simples; toda la API de bajo nivel queda oculta.

Lo mismo aplica a `SplashHelper` (oculta animaciones y visibilidad de la splash screen) y `PermissionHelper` (oculta el sistema de permisos de Android).

---

## 🔄 Patrones de Comportamiento

### 4. Observer — `LiveData` en `GasStationViewModel`
**Archivo**: [`GasStationViewModel.java`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/viewmodels/GasStationViewModel.java)

```java
// ViewModel expone LiveData (el Sujeto/Observable):
private final MutableLiveData<List<GasolineraAPI>> gasolinerasVisibles = new MutableLiveData<>();

// MainActivity se suscribe como Observador:
viewModel.getGasolinerasVisibles().observe(this, list -> mapHelper.updateMarkers(list));
viewModel.getProgress().observe(this, p -> splashHelper.updateStatus(p, msg));
viewModel.getIsLoading().observe(this, loading -> { if (!loading) splashHelper.hide(); });
```

**Diagnóstico**: `LiveData` es la implementación oficial de Android del patrón **Observer**. `MutableLiveData` es el *Subject* concreto; las lambdas en `MainActivity` son los *Observers*. El ViewModel no conoce quién observa, solo notifica cambios de estado.

---

### 5. Strategy — `PuntuadorGasolineras`
**Archivo**: [`PuntuadorGasolineras.java`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/utils/PuntuadorGasolineras.java)

```java
// Algoritmo de puntuación con pesos configurables en tiempo de ejecución:
private double pesoPrecios = 0.2;
private double pesoVariedad = 0.4;
private double pesoFavoritos = 0.4;

public double calcularPuntuacion(GasolineraAPI g) {
    return calcularPuntuacionPrecios(g) * pesoPrecios
         + calcularPuntuacionVariedad(g) * pesoVariedad
         + calcularPuntuacionFavoritos(g) * pesoFavoritos;
}
```

**Diagnóstico**: El propio Javadoc de la clase lo declara explícitamente: *"implementa el patrón Strategy"*. Los pesos son modificables vía setters, permitiendo intercambiar la estrategia de ordenamiento sin cambiar el código cliente. En GeoGas, el contexto es `GasStationViewModel.updateVisibleGasStations()`.

> [!TIP]
> Para una aplicación más fiel al patrón, los pesos podrían extraerse a una interfaz `EstrategiaPuntuacion` y pasarse como objeto colaborador al `PuntuadorGasolineras`. Esto permitiría testear estrategias fácilmente.

---

### 6. Observer (UI Events) — Interfaces de Callback en Fragments
**Archivos**: [`MainActivity.java`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/ui/MainActivity.java) (L211, L236)

```java
filtersFragment.setFiltersListener(new FiltersFragment.FiltersListener() {
    @Override public void onFiltersApplied() { ... }
    @Override public void onFiltersCleared() { ... }
    @Override public void onCloseFilters() { ... }
});

detailsFragment.setDetailsListener(new DetailsPanelFragment.DetailsListener() {
    @Override public void onCloseDetails() { ... }
    @Override public void onFavoriteToggled(GasolineraAPI g) { ... }
});
```

**Diagnóstico**: Las interfaces `FiltersListener` y `DetailsListener` son el patrón **Observer** tradicional (sin LiveData), con los Fragments como *Subjects* y `MainActivity` como *Observer* concreto. Este es el estilo clásico de comunicación Fragment→Activity en Android.

---

### 7. Template Method — `cumpleFiltros` en `FiltrosManager`
**Archivo**: [`FiltrosManager.java`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/utils/FiltrosManager.java)

```java
// El "esqueleto" del algoritmo:
private boolean cumpleFiltros(GasolineraAPI g, FavoritosManager fm) {
    return cumpleFiltroUbicacion(g)      // paso 1
        && cumpleFiltroGasolinera(g)     // paso 2
        && cumpleFiltroCombustibles(g)   // paso 3
        && cumpleFiltroServicios(g, fm)  // paso 4
        && cumpleFiltroPrecios(g);       // paso 5
}
```

**Diagnóstico**: `cumpleFiltros` define la **estructura del algoritmo de filtrado** (orden y composición de los pasos), mientras cada método privado implementa un paso específico. Es un Template Method *inline* (sin herencia, mismo objeto), un estilo válido cuando no hay necesidad de subclases.

---

## ✨ Patrones implementados durante la refactorización (agosto 2026)

> Los patrones de abajo se añadieron tras aplicar el plan derivado de [`recomendaciones_patrones_geogas6.md`](recomendaciones_patrones_geogas6.md).

### 8. Builder — `FiltrosManager.Builder`
**Archivo**: [`FiltrosManager.java`](../app/src/main/java/com/eliasbuenosdias/geogas/utils/FiltrosManager.java) (clase interna `Builder`)

```java
new FiltrosManager.Builder(filtros)
    .provincia(...).municipio(...).soloGasolina95(...)
    .precioMaxGasolina95(...)
    .build(); // valida precios negativos y devuelve la instancia configurada
```

**Diagnóstico**: Evita la cascada de 12 setters. `build()` valida el estado final (lanza `IllegalArgumentException` si un precio máximo es negativo).

### 9. Command — `AplicarFiltroCommand` con historial de undo
**Archivos**: [`Command.java`](../app/src/main/java/com/eliasbuenosdias/geogas/utils/command/Command.java), [`AplicarFiltroCommand.java`](../app/src/main/java/com/eliasbuenosdias/geogas/utils/command/AplicarFiltroCommand.java), [`GasStationViewModel.java`](../app/src/main/java/com/eliasbuenosdias/geogas/viewmodels/GasStationViewModel.java)

```java
public class AplicarFiltroCommand implements Command {
    private final FiltrosEstado estadoAnterior; // snapshot capturado antes de ejecutar
    @Override public void execute() { ... }
    @Override public void undo()    { ... }
}
```

**Diagnóstico**: Cada aplicación de filtros encapsula su estado previo (`FiltrosEstado.snapshot()`). El ViewModel mantiene una cola `Deque` de comandos (máx. 10) y expone `aplicarFiltros()`, `deshacerUltimoFiltro()` y `puedeDeshacer()`. El botón "Deshacer último cambio" del panel de filtros lo consume.

### 10. State — `LoadState` para el ciclo de carga
**Archivo**: [`LoadState.java`](../app/src/main/java/com/eliasbuenosdias/geogas/viewmodels/LoadState.java)

```java
public final class LoadState {
    enum Status { LOADING, LOADED, ERROR }
    // + progress + statusKey, inmutables
}
```

**Diagnóstico**: Fusiona los antiguos `isLoading` + `progress` + `statusMessage` (booleans/ints dispersos) en un único objeto de estado cohesivo expuesto como `LiveData<LoadState>` (`getLoadState()`). `MainActivity` reacciona a `state.isLoaded()` para ocultar la splash.

### 11. Flyweight — `IconFlyweightFactory`
**Archivo**: [`IconFlyweightFactory.java`](../app/src/main/java/com/eliasbuenosdias/geogas/utils/IconFlyweightFactory.java)

```java
public static Drawable get(Context ctx, int resId) {
    return cache.computeIfAbsent(resId, id -> ContextCompat.getDrawable(ctx, id));
}
```

**Diagnóstico**: Cache de los 6 drawables de iconos posibles (normal/small/tiny × favorita/normal). `MapHelper.updateMarker()` reutiliza la misma instancia en lugar de crear un `Drawable` nuevo por marcador (hasta 200 en RAM).

---

## 📐 Arquitectura: MVVM (patrón arquitectónico)

Aunque MVVM no es un patrón GoF, es el patrón arquitectónico dominante del proyecto:

| Capa | Clase | Responsabilidad |
|------|-------|----------------|
| **Model** | `GasolineraAPI`, `GasolineraResponse` | Entidades de datos |
| **ViewModel** | `GasStationViewModel` | Estado + lógica de negocio |
| **View** | `MainActivity`, `FiltersFragment`, `DetailsPanelFragment` | UI pura |

---

## Resumen

| # | Patrón | Categoría | Clase(s) |
|---|--------|-----------|---------|
| 1 | **Singleton** | Creacional | `ApiClient` |
| 2 | **Factory Method** | Creacional | `IconosManager` |
| 3 | **Facade** | Estructural | `MapHelper`, `SplashHelper`, `PermissionHelper` |
| 4 | **Observer** (reactivo) | Comportamiento | `GasStationViewModel` + `LiveData` |
| 5 | **Strategy** | Comportamiento | `PuntuadorGasolineras` |
| 6 | **Observer** (callback) | Comportamiento | `FiltersListener`, `DetailsListener` |
| 7 | **Template Method** | Comportamiento | `FiltrosManager.cumpleFiltros` |
| 8 | **Builder** | Creacional | `FiltrosManager.Builder` |
| 9 | **Command** | Comportamiento | `AplicarFiltroCommand` + `GasStationViewModel` |
| 10 | **State** | Comportamiento | `LoadState` |
| 11 | **Flyweight** | Estructural | `IconFlyweightFactory` |
