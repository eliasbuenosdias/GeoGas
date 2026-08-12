# 🎨 Guía de Refactorización UI/UX para GeoGas 6
> **Basado en los principios de *Refactoring UI* de Adam Wathan & Steve Schoger**

Esta auditoría analiza la interfaz actual de **GeoGas 6** y ofrece recomendaciones concretas para transformar la experiencia visual y de usuario, pasando de un diseño funcional tradicional a una aplicación móvil moderna, pulida y atractiva.

---

## 📊 Resumen Ejecutivo de Oportunidades

| Área | Estado Actual | Propuesta *Refactoring UI* | Impacto |
| :--- | :--- | :--- | :--- |
| **Paleta de Colores** | Colores por defecto de Android mixed con Catppuccin/Nord. Sin colores semánticos de precio. | Sistema HSL con tonos neutros tintados y **Badges de precio semánticos** (Verde/Amarillo/Rojo). | 🔴 Alto |
| **Jerarquía Visual** | Tarjetas anidadas con excesiva sombra (`16dp` + `2dp`). Texto homogéneo con etiquetas repetitivas. | Eliminar tarjetas anidadas. Destacar el **precio** como héroe visual. Suavizar etiquetas secundarias. | 🔴 Alto |
| **Espaciado y Layout** | Anchos fijos en dp (`340dp`/`360dp`). Escala de espaciado irregular (`4dp, 8dp, 12dp, 16dp, 20dp`). | Sistema estricto de base 8 (`4dp, 8dp, 16dp, 24dp, 32dp`). Usar BottomSheets responsivas. | 🟡 Medio |
| **Componentes e Iconos** | Emojis como texto (`🌐`), drawables nativos antiguos (`btn_star_big_off`). | Iconografía vectorial SVG nativa, insignias de marcas de gasolinera y microinteracciones. | 🟡 Medio |
| **Estados Vacíos** | Sin ilustración ni guía clara cuando no hay resultados o falta GPS. | Empty states enriquecidos con sugerencias de acción y micro-copy claro. | 🟢 Continuo |

---

## 🎯 1. Jerarquía Visual: El Precio como Elemento Héroe (Capítulo 2)

### ❌ Problema Actual
En [panel_detalles_gasolinera.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/layout/panel_detalles_gasolinera.xml#L101-L350), la información está empaquetada en múltiples tarjetas anidadas (`MaterialCardView` de `2dp` dentro de una tarjeta madre de `16dp`). Los títulos de sección tienen la misma escala y el precio aparece como un dato plano en una `TableLayout`.

### 💡 Principio *Refactoring UI*: *"Not All Elements Are Equal"* & *"Emphasize by De-emphasizing"*
1. **El precio es el héroe**: El usuario abre GeoGas con un objetivo principal: **saber cuánto cuesta el combustible y si es barato**. El precio debe ser el elemento con mayor peso tipográfico y contraste.
2. **Elimina etiquetas innecesarias**: No escribas `Gasolina 95: 1,529 €/L`. Presenta la información limpia con badges de color.
3. **Desenfatizar lo secundario**: Reduce la opacidad y el peso tipográfico de datos como la dirección o el horario en lugar de aumentar todo en negrita.

### 🛠️ Ejemplo de Rediseño de Tarjeta de Detalles
```xml
<!-- ANTES: Múltiples secciones en tarjetas anidadas separadas -->
<!-- AHORA: Contenedor limpio con divisor sutil y Badges de Precio -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp">

    <!-- Cabecera de la Gasolinera con Badge de Marca -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:orientation="horizontal">

        <ImageView
            android:id="@+id/img_brand_logo"
            android:layout_width="44dp"
            android:layout_height="44dp"
            android:src="@drawable/ic_gas_station_pro" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginStart="12dp"
            android:orientation="vertical">

            <TextView
                android:id="@+id/tv_station_name"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/textColorPrimary"
                android:textSize="18sp"
                android:textFontWeight="700" />

            <TextView
                android:id="@+id/tv_station_address"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/textColorSecondary"
                android:textSize="13sp" />
        </LinearLayout>
    </LinearLayout>

    <!-- HÉROE: Precio de Combustible Destacado con Badge Semántico -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:background="@drawable/bg_price_hero_card"
        android:padding="16dp"
        android:gravity="center_vertical">

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Gasolina 95 E5"
                android:textColor="@color/textColorSecondary"
                android:textSize="12sp"
                android:textAllCaps="true"
                android:letterSpacing="0.05" />

            <TextView
                android:id="@+id/tv_main_price"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="1,489 €/L"
                android:textColor="@color/price_cheap_text"
                android:textSize="28sp"
                android:textFontWeight="800" />
        </LinearLayout>

        <!-- Badge de Clasificación de Precio -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="MÁS BARATA"
            android:textColor="@color/price_cheap_text"
            android:background="@drawable/bg_badge_cheap"
            android:paddingHorizontal="10dp"
            android:paddingVertical="4dp"
            android:textSize="11sp"
            android:textStyle="bold" />
    </LinearLayout>
</LinearLayout>
```

---

## 🎨 2. Sistema de Color y Tonos Neutros Tintados (Capítulo 5)

### ❌ Problema Actual
En [colors.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/values/colors.xml) y [colors.xml (night)](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/values-night/colors.xml), conviven residuos del template inicial de Android (`purple_200`, `teal_700`) junto con colores genéricos como `#F5F5F5` y `#333333`.

### 💡 Principio *Refactoring UI*: *"Greys Don't Have to Be Grey"* & *"Semantic Color Palettes"*
1. **Neutros tintados**: Un gris puro (`#808080`) resulta frío y artificial. Tintar los neutros con un toque del azul primario (ej. Slate Blue `#F1F5F9` en claro y `#1E293B` en oscuro) unifica la interfaz.
2. **Escala Semántica de Precios**:
   - 🟢 **Chollo / Económica**: Verde Esmeralda (`#10B981` / Fondo `#D1FAE5`)
   - 🟡 **Precio Medio**: Ámbar (`#F59E0B` / Fondo `#FEF3C7`)
   - 🔴 **Precio Elevado**: Carmín (`#EF4444` / Fondo `#FEE2E2`)

### 🛠️ Propuesta de Paleta `colors.xml` Refactorizada

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Brand Primary (Azul Gasolineras) -->
    <color name="brand_50">#EFF6FF</color>
    <color name="brand_100">#DBEAFE</color>
    <color name="brand_500">#3B82F6</color>
    <color name="brand_600">#2563EB</color>
    <color name="brand_700">#1D4ED8</color>

    <!-- Neutros Tintados (Slate/Cool Grey) -->
    <color name="neutral_50">#F8FAFC</color>
    <color name="neutral_100">#F1F5F9</color>
    <color name="neutral_200">#E2E8F0</color>
    <color name="neutral_500">#64748B</color>
    <color name="neutral_700">#334155</color>
    <color name="neutral_900">#0F172A</color>

    <!-- Estados Semánticos de Precio -->
    <color name="price_cheap">#10B981</color>        <!-- Emerald -->
    <color name="price_cheap_bg">#D1FAE5</color>
    <color name="price_medium">#F59E0B</color>       <!-- Amber -->
    <color name="price_medium_bg">#FEF3C7</color>
    <color name="price_expensive">#EF4444</color>    <!-- Rose -->
    <color name="price_expensive_bg">#FEE2E2</color>

    <!-- Asignaciones Semánticas -->
    <color name="colorPrimary">@color/brand_600</color>
    <color name="backgroundColor">@color/neutral_50</color>
    <color name="surfaceColor">@color/white</color>
    <color name="textColorPrimary">@color/neutral_900</color>
    <color name="textColorSecondary">@color/neutral_500</color>
</resources>
```

---

## 📐 3. Espaciado y Eliminación de Tarjetas Anidadas (Capítulo 3 y 6)

### ❌ Problema Actual
- En [filters_panel.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/layout/filters_panel.xml#L5), los paneles laterales o emergentes usan anchos fijos hardcodeados como `android:layout_width="360dp"`. Esto produce desajustes visuales en pantallas pequeñas o tablets.
- Elevaciones apiladas: Paneles flotantes con `16dp` de elevación que contienen tarjetas internas con `2dp` de elevación y sombras enfrentadas.

### 💡 Principio *Refactoring UI*: *"Start with Too Much White Space"* & *"Overlapping Elevation"*
1. **Evita la "Cajitis" (Boxiness)**: No encierres cada grupo de controles en una caja/tarjeta con sombra independiente. Usa espacio en blanco y divisores finos (`1dp` con 10% de opacidad) para agrupar visualmente.
2. **Sistema Estricto de Espaciado 8-pt**:
   - `4dp`: Micro-espaciado entre etiquetas e iconos.
   - `8dp`: Separación entre elementos hermanos estrechos.
   - `16dp`: Padding interno de contenedores y márgenes estándar.
   - `24dp`: Separación entre secciones independientes.
   - `32dp`: Espacio libre para respiración visual en cabeceras.

### 🛠️ Refactorización de Filtros en [filters_panel.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/layout/filters_panel.xml)
- Reemplazar el `AutoCompleteTextView` envuelto en cajas por un grupo de **Chips interactivos (Single / Multi Select)** para opciones frecuentes como *"Combustible: Gasolina 95 / Diesel / GLP"* o *"Abierto 24h"*.

```xml
<!-- Sección de Selección de Combustible con FilterChips -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="TIPO DE COMBUSTIBLE"
    android:textColor="@color/textColorSecondary"
    android:textSize="11sp"
    android:letterSpacing="0.08"
    android:textStyle="bold"
    android:layout_marginBottom="8dp" />

<com.google.android.material.chip.ChipGroup
    android:id="@+id/chip_group_fuel"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:singleSelection="true"
    app:chipSpacingHorizontal="8dp">

    <com.google.android.material.chip.Chip
        android:id="@+id/chip_g95"
        style="@style/Widget.MaterialComponents.Chip.Choice"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Gasolina 95"
        android:checked="true" />

    <com.google.android.material.chip.Chip
        android:id="@+id/chip_diesel"
        style="@style/Widget.MaterialComponents.Chip.Choice"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Diésel A" />

    <com.google.android.material.chip.Chip
        android:id="@+id/chip_g98"
        style="@style/Widget.MaterialComponents.Chip.Choice"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Gasolina 98" />
</com.google.android.material.chip.ChipGroup>
```

---

## ✨ 4. Detalle de Componentes y Toques de Acabado (Capítulo 8)

### ❌ Problema Actual
- En [toolbar_main.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/layout/toolbar_main.xml#L45), el selector de idioma utiliza el emoji `🌐` dentro de un `TextView`.
- En el panel de detalles, el botón de favoritos usa el drawable antiguo `@android:drawable/btn_star_big_off`.
- Las cabeceras usan un degradado plano (`header_gradient.xml`) sin bordes de acento ni separadores limpios.

### 💡 Principio *Refactoring UI*: *"Supercharge Default Elements"* & *"Accent Borders"*
1. **Iconografía Profesional Vectorial**: Sustituye los emojis por vectores SVG limpios. Los emojis se ven distintos entre versiones de Android y fabricantes (Samsung, Xiaomi, Google).
2. **Bordes de Acento**: Añadir un pequeño borde superior de 3dp en el color primario de la marca le da una apariencia de producto "premium" sin recargar el diseño.
3. **Indicadores de Estado Activo**: En el botón de filtro de la Toolbar, mostrar un **punto badge flotante (Dot Indicator)** cuando haya filtros activos aplicados.

### 🛠️ Comparativa de Componentes

| Componente | Implementación Actual | Propuesta Refactorizada |
| :--- | :--- | :--- |
| **Selector Idioma** | `TextView` con Emoji `"🌐"` | `MaterialButton` outlined con icono `ic_translate.xml` y texto `ES / EN`. |
| **Favoritos** | `ImageButton` nativo antiguo | `MaterialCheckBox` con icono de estrella vectorial y animación de rebote (*Scale Animation*). |
| **Botonera Flotante** | Fija con bordes estándar | Botón con pill de acento y sombra suave (*Elevation 4dp, CornerRadius 28dp*). |

---

## 🚀 Plan de Acción Recomendado por Fases

1. **Fase 1: Definición del Sistema de Diseño (1-2 días)**
   - Limpiar `colors.xml` e implementar los tonos de gris tintado y la escala semántica de precios.
   - Definir los estilos de texto estándar en `styles.xml` / `themes.xml`.

2. **Fase 2: Rediseño del Panel de Detalles (2-3 días)**
   - Reestructurar [panel_detalles_gasolinera.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/layout/panel_detalles_gasolinera.xml).
   - Eliminar tarjetas anidadas y destacar el precio principal con su badge de clasificación (Barata/Media/Cara).

3. **Fase 3: Modernización de Filtros y Toolbar (1-2 días)**
   - Sustituir la cabecera e icono de idioma en [toolbar_main.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/layout/toolbar_main.xml).
   - Convertir selectores desplegables de filtros en `ChipGroup` interactivos en [filters_panel.xml](file:///Users/elias/Documents/GeoGas%206/app/src/main/res/layout/filters_panel.xml).

4. **Fase 4: Microinteracciones y Polish (1 día)**
   - Añadir animaciones de estado en el botón de favoritos y feedback al aplicar filtros.
