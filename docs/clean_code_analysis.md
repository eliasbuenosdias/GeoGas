# 🧹 Análisis Clean Code — GeoGas 6

Basado en los principios de Robert C. Martin tras revisar los archivos reales del proyecto.

---

## ✅ Lo que ya está bien

El proyecto tiene una buena base. Antes de los problemas, hay que reconocer lo que funciona:

- **Separación de responsabilidades**: `MainActivity` delega en `MapHelper`, `SplashHelper`, `FiltrosManager`, etc. Excelente.
- **ViewModel bien usado**: `GasStationViewModel` separa la lógica de negocio de la UI.
- **Nombres descriptivos en español**: `calcularPuntuacionPrecios`, `cumpleFiltroUbicacion` son claros y revelan la intención.
- **Funciones pequeñas**: La mayoría de los métodos son cortos y hacen una sola cosa.

---

## 🔴 Problemas por prioridad

### 1. Comentarios que mienten (`PuntuadorGasolineras.java`, líneas 26–28)

```java
// ❌ Los comentarios dicen lo contrario de los valores reales
private double pesoPrecios = 0.2;   // 60% para precios  ← MIENTE
private double pesoVariedad = 0.4;  // 20% para variedad ← MIENTE
private double pesoFavoritos = 0.4; // 20% para favoritos ← MIENTE
```

**Regla Clean Code**: "Los comentarios que contradicen al código son peores que no tener comentarios."

**Fix**:
```java
// ✅ Eliminar el comentario o corregirlo
private double pesoPrecios = 0.2;   // 20% para precios
private double pesoVariedad = 0.4;  // 40% para variedad
private double pesoFavoritos = 0.4; // 40% para favoritos
```

---

### 2. Comentario de código en producción (`FiltrosManager.java`, línea 1)

```java
// ❌ Primera línea del archivo
// En FiltrosManager.java - REEMPLAZA completamente la clase:
```

Este es un comentario de instrucción de refactorización que se quedó en el archivo. Debe eliminarse.

---

### 3. Comentario inline mezclado con Javadoc (`PuntuadorGasolineras.java`, línea 212)

```java
// ❌ El comentario aparece DENTRO del bloque de getters, antes del Javadoc
// Getters y Setters para los pesos (por si queremos ajustarlos dinámicamente)
    public double getPesoPrecios() { return pesoPrecios; }
```

El comentario está mal ubicado (entre el getter y su Javadoc previo). Moverlo arriba de todos los getters o eliminarlo.

---

### 4. Magia numérica sin nombre (`GasStationViewModel.java`, línea 159)

```java
// ❌ ¿Por qué 200? ¿Es arbitrario? ¿Se puede configurar?
int limit = Math.min(200, puntuadas.size());
```

**Fix**: Extraer a constante con nombre que revele la intención:
```java
// ✅
private static final int MAX_VISIBLE_MARKERS = 200;
// ...
int limit = Math.min(MAX_VISIBLE_MARKERS, puntuadas.size());
```

---

### 5. Magic numbers sin nombre (`MapHelper.java`, líneas 50–51)

```java
// ❌ ¿Por qué 15.0? ¿Por qué Madrid? Magia pura
map.getController().setZoom(15.0);
map.getController().setCenter(new GeoPoint(40.4168, -3.7038));
```

**Fix**:
```java
// ✅
private static final double DEFAULT_ZOOM = 15.0;
private static final double MADRID_LAT = 40.4168;
private static final double MADRID_LON = -3.7038;
```

---

### 6. Magic number de animación duplicado (`MainActivity.java`, líneas 267, 277)

```java
// ❌ 300 aparece dos veces para el duration de animación, y también en el debounce
filtersContainer.animate().translationX(0).setDuration(300).start();
filtersContainer.animate().translationX(-width).setDuration(300).start();
handler.postDelayed(viewportChangeRunnable, 300); // ← mismo número, distinto propósito
```

**Fix**:
```java
// ✅
private static final int PANEL_ANIMATION_DURATION_MS = 300;
private static final int VIEWPORT_DEBOUNCE_DELAY_MS = 300;
```

---

### 7. Fallback hardcodeado con comentario informal (`MainActivity.java`, línea 264)

```java
// ❌ "Fallback size" en un comentario inline
width = (int) (360 * getResources().getDisplayMetrics().density); // Fallback size
```

**Fix**: Extraer a constante:
```java
// ✅
private static final int FALLBACK_PANEL_WIDTH_DP = 360;
// ...
width = (int) (FALLBACK_PANEL_WIDTH_DP * getResources().getDisplayMetrics().density);
```

---

### 8. Indentación inconsistente en `GasolineraAPI.java`

El modelo tiene **indentación de 4 espacios adicionales** en todo el archivo (el `package` y la clase tienen 4 espacios de indent), lo que no es Java estándar. Esto ocurrió probablemente al copiar/pegar. Hay que reformatear con `Ctrl+Alt+L` en Android Studio.

```java
// ❌ Todo el archivo tiene indentación extra
    package com.eliasbuenosdias.geogas.models;  ← tiene 4 espacios de sobra
    public class GasolineraAPI {
```

---

### 9. Excepción silenciada sin justificación (`MainActivity.java`, línea 328)

```java
// ❌ SecurityException ignorada silenciosamente
} catch (SecurityException ignored) {
}
```

Al menos loguear el error para facilitar el debugging:
```java
// ✅
} catch (SecurityException e) {
    Log.w("GeoGas", "No permission to get location", e);
}
```

---

### 10. Getters/Setters exponen estado mutable del ViewModel

`GasStationViewModel` expone `setLoading()` y `setProgress()` como métodos públicos, pero el ViewModel debería ser la única fuente de verdad y no dejar que externos cambien su estado directamente. Si no se usan externamente, **eliminarlos**.

```java
// ❌ ¿Quién llama a estos desde fuera?
public void setLoading(boolean loading) { ... }
public void setProgress(int p) { ... }
```

---

### 11. Tipo raw en import del ViewModel (`GasStationViewModel.java`, línea 32)

```java
// ❌ Importación inline con nombre completo en campo
private com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras puntuador;
```

Debería moverse al bloque de imports en la parte superior del archivo:
```java
// ✅ En imports
import com.eliasbuenosdias.geogas.utils.PuntuadorGasolineras;
// ...
private PuntuadorGasolineras puntuador;
```

Lo mismo aplica a `org.osmdroid.util.BoundingBox` en la línea 31 y los usos inline en `loadData()` y `parseJson()`.

---

## 📋 Checklist de acciones concretas

| # | Archivo | Acción | Impacto |
|---|---------|--------|---------|
| 1 | `PuntuadorGasolineras.java:26-28` | Corregir comentarios que mienten en los pesos | 🔴 Alto |
| 2 | `FiltrosManager.java:1` | Eliminar comentario de instrucción de refactorización | 🟡 Medio |
| 3 | `GasStationViewModel.java:159` | Extraer `200` → `MAX_VISIBLE_MARKERS` | 🟡 Medio |
| 4 | `MapHelper.java:50-51` | Extraer zoom y coordenadas a constantes nombradas | 🟡 Medio |
| 5 | `MainActivity.java:267,277,192` | Extraer `300` → `PANEL_ANIMATION_DURATION_MS` y `VIEWPORT_DEBOUNCE_DELAY_MS` | 🟡 Medio |
| 6 | `MainActivity.java:264` | Extraer `360` → `FALLBACK_PANEL_WIDTH_DP` | 🟡 Medio |
| 7 | `GasolineraAPI.java` | Reformatear indentación extra (Ctrl+Alt+L) | 🟢 Bajo |
| 8 | `MainActivity.java:328` | Loguear `SecurityException` en lugar de ignorarla | 🟢 Bajo |
| 9 | `GasStationViewModel.java:204-210` | Eliminar `setLoading()`/`setProgress()` si no se usan externamente | 🟢 Bajo |
| 10 | `GasStationViewModel.java:31-32` | Mover imports inline al bloque de imports | 🟢 Bajo |

---

## 🏆 Veredicto general

El código está **bastante bien estructurado** para ser una app Android personal. La refactorización ya fue hecha con buena intención. Los problemas restantes son principalmente:
- **Comentarios desactualizados o incorrectos** (el peor anti-patrón de Clean Code)
- **Magic numbers** que se pueden nombrar con constantes
- **Residuos de refactorización** (el comentario de instrucción en FiltrosManager)

El mayor win inmediato: **corregir los comentarios que mienten** en `PuntuadorGasolineras.java`.
