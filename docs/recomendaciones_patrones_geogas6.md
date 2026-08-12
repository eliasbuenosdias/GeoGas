# Patrones GoF Recomendados para GeoGas 6

> Basado en *Sumérgete en los Patrones de Diseño* — Alexander Shvets
> Solo se recomienda lo que hay un **dolor real en el código** que lo justifique.

---

## ⭐ Alta Prioridad

### 1. Builder — Construcción de `FiltrosManager`
**Problema en el código actual**: [`FiltersFragment.java:161-191`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/ui/fragments/FiltersFragment.java#L161-L191)

```java
// ❌ Actualmente: 12 setters consecutivos sin garantía de consistencia
filtrosManager.setProvincia("Madrid");
filtrosManager.setMunicipio("Alcorcón");
filtrosManager.setSoloGasolina95(true);
filtrosManager.setSoloDiesel(false);
filtrosManager.setPrecioMaxGasolina95(1.75);
// ... 7 setters más
```

**Con Builder**:
```java
// ✅ Con Builder: fluido, legible y sin estado inconsistente intermedio
FiltrosManager filtros = new FiltrosManager.Builder()
    .provincia("Madrid")
    .municipio("Alcorcón")
    .soloGasolina95(true)
    .precioMaxGasolina95(1.75)
    .build(); // Valida y crea el objeto completo de una vez
```

**Por qué usarlo aquí**:
- `FiltrosManager` tiene **12 campos opcionales** → exactamente el caso descrito por Shvets ("constructores gigantes")
- Permite añadir validaciones en `build()` (e.g., precio negativo = error)
- El código de `updateFiltrosFromUI()` se vuelve una sola expresión encadenada

**Esfuerzo**: Bajo. Solo refactorizar `FiltrosManager`, sin cambiar la lógica de filtrado.

---

### 2. Command — Historial de filtros con Undo
**Problema en el código actual**: [`MainActivity.java:214-223`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/ui/MainActivity.java#L214-L223)

```java
// ❌ Actualmente: aplicar filtros no se puede deshacer
filtersFragment.setFiltersListener(new FiltersFragment.FiltersListener() {
    @Override
    public void onFiltersApplied() {
        viewModel.applyFilters(filtrosManager, favoritosManager);
        // No hay forma de volver al estado anterior
    }
});
```

**Con Command**:
```java
// Cada aplicación de filtro es un Command encapsulado
public class AplicarFiltroCommand implements FilterCommand {
    private final FiltrosManager snapshot; // Copia del estado anterior
    
    @Override public void execute() { viewModel.applyFilters(filtrosManager, favoritosManager); }
    @Override public void undo()    { viewModel.applyFilters(snapshot, favoritosManager); }
}

// Un botón "deshacer último filtro" se vuelve trivial
```

**Por qué usarlo aquí**:
- Los filtros son complejos (12 criterios) → deshacer accidentalmente es frustrante para el usuario
- Permite implementar historial de búsquedas guardadas ("mis búsquedas frecuentes")
- Shvets: *"¿Qué ejecuto luego o deshago? Encapúlalo → Command"*

**Esfuerzo**: Medio. Requiere una cola de commands en el ViewModel.

---

## 🔶 Prioridad Media

### 3. State — Comportamiento del mapa según estado de carga
**Problema en el código actual**: [`MainActivity.java:119-122`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/ui/MainActivity.java#L119-L122) + [`GasStationViewModel.java:72-97`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/viewmodels/GasStationViewModel.java#L72-L97)

```java
// ❌ Actualmente: estado de carga gestionado con booleans/ints dispersos
private final MutableLiveData<Boolean> isLoading = ...
private final MutableLiveData<Integer> progress = ...
private final MutableLiveData<String> statusMessage = ...

// Y en MainActivity hay lógica condicional acoplada al estado:
viewModel.getIsLoading().observe(this, loading -> {
    if (!loading && viewModel.getProgress().getValue() >= 100)
        splashHelper.hide();
});
```

**Con State**:
```java
// Estados explícitos del ViewModel
interface AppState {
    void onEnter(GasStationViewModel vm);
    void loadData(GasStationViewModel vm, MitecoApiService api);
}

class LoadingState implements AppState { ... }
class DataReadyState implements AppState { ... }
class ErrorState implements AppState { ... }
```

**Por qué usarlo aquí**:
- La app tiene 4 estados bien diferenciados: *Iniciando → Cargando → Datos listos → Error*
- Shvets: *"¿Comportamiento cambia radical según fase? → State"*
- Elimina los booleans `isLoading` + `progress` + `statusMessage` fusionándolos en un objeto de estado cohesivo
- Facilita añadir futuros estados (e.g., "Sin conexión", "Actualizando en segundo plano")

**Esfuerzo**: Medio-alto. Refactoriza la comunicación ViewModel ↔ UI.

---

### 4. Flyweight — Optimización de marcadores en el mapa
**Problema en el código actual**: [`MapHelper.java:63-81`](file:///Users/elias/Documents/GeoGas%206/app/src/main/java/com/eliasbuenosdias/geogas/ui/helpers/MapHelper.java#L63-L81)

```java
// ❌ Actualmente: cada marcador carga su Drawable individualmente
private void updateMarker(Marker m, GasolineraAPI g, int density, double zoom) {
    boolean fav = favoritosManager.esFavorita(g.getId());
    int icon = iconosManager.obtenerIconoGasolinera(fav, density, zoom);
    m.setIcon(ContextCompat.getDrawable(context, icon)); // Nuevo Drawable por marcador
}
// Con 200 marcadores: 200 objetos Drawable en RAM (solo 6 drawables únicos posibles)
```

**Con Flyweight**:
```java
// Cache de Drawables compartidos — solo 6 objetos en memoria para todos los marcadores
public class IconFlyweightFactory {
    private static final Map<Integer, Drawable> cache = new HashMap<>();
    
    public static Drawable get(Context ctx, int resId) {
        return cache.computeIfAbsent(resId, id -> ContextCompat.getDrawable(ctx, id));
    }
}

// En updateMarker():
m.setIcon(IconFlyweightFactory.get(context, icon)); // Reutiliza el mismo objeto
```

**Por qué usarlo aquí**:
- Solo hay **6 iconos posibles** (normal/small/tiny × favorita/normal) pero se crean hasta **200 Drawables distintos**
- En dispositivos de gama baja con muchas gasolineras, esto impacta la memoria del heap
- Shvets: *"¿Memoria agotada por millones de objetos? → Flyweight"*

**Esfuerzo**: Muy bajo. Una clase estática de 10 líneas, sin tocar la arquitectura.

---

## ❌ Patrones que NO recomiendo

| Patrón | Por qué no |
|--------|-----------|
| **Chain of Responsibility** | Los filtros ya tienen una cadena AND limpia en `cumpleFiltros()`. Añadir una cadena de objetos sería sobrediseño para este caso. |
| **Mediator** | Solo hay 3 componentes comunicándose (Activity, 2 Fragments). Un Mediator añadiría complejidad sin reducir dependencias perceptiblemente. |
| **Abstract Factory** | Solo hay una "familia" de iconos y una API. No hay variantes que justifiquen familias de fábricas. |
| **Visitor** | `GasolineraAPI` es un modelo de datos plano, no una jerarquía de tipos. El Visitor no aporta nada. |
| **Prototype** | No hay inicialización costosa ni necesidad de clonar gasolineras. |

---

## Hoja de ruta sugerida

```
Semana 1 — Flyweight (30 min, impacto inmediato en RAM)
Semana 2 — Builder para FiltrosManager (2h, mejora legibilidad)  
Semana 3 — Command para historial de filtros (4h, nueva feature de UX)
Semana 4 — State para el ciclo de vida de carga (4h, arquitectura más sólida)
```
