package com.eliasbuenosdias.geogas.ui.helpers;

import android.content.Context;
import com.eliasbuenosdias.geogas.R;
import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import com.eliasbuenosdias.geogas.utils.FavoritosManager;
import com.eliasbuenosdias.geogas.utils.IconFlyweightFactory;
import com.eliasbuenosdias.geogas.utils.IconosManager;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsula la gestión del mapa y marcadores.
 */
public class MapHelper {

    private static final double DEFAULT_ZOOM = 15.0;
    private static final double MADRID_LAT = 40.4168;
    private static final double MADRID_LON = -3.7038;

    private final MapView map;
    private final Context context;
    private final IconosManager iconosManager;
    private final FavoritosManager favoritosManager;
    private OnMarkerClickListener markerListener;
    private int lastGasStationCount = 0;
    private double lastZoomLevel = DEFAULT_ZOOM;

    public interface OnMarkerClickListener {
        void onMarkerClick(GasolineraAPI gasolinera);
    }

    public MapHelper(MapView map, IconosManager iconosManager, FavoritosManager favoritosManager) {
        this.map = map;
        this.context = map.getContext();
        this.iconosManager = iconosManager;
        this.favoritosManager = favoritosManager;
        setupMap();
    }

    private void setupMap() {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false); // Eliminamos botones +/-
        map.getController().setZoom(DEFAULT_ZOOM);
        map.getController().setCenter(new GeoPoint(MADRID_LAT, MADRID_LON));

        CompassOverlay compassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context),
                map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerListener = listener;
    }

    public void updateMarkers(List<GasolineraAPI> gasolineras) {
        Map<String, Marker> existingMarkers = getExistingMarkers();
        lastGasStationCount = gasolineras.size();
        lastZoomLevel = map.getZoomLevel();

        for (GasolineraAPI g : gasolineras) {
            Marker m = existingMarkers.remove(g.getId());
            if (m == null) {
                m = createMarker(g, lastGasStationCount, lastZoomLevel);
                map.getOverlays().add(m);
            } else {
                updateMarker(m, g, lastGasStationCount, lastZoomLevel);
            }
        }

        for (Marker m : existingMarkers.values()) {
            map.getOverlays().remove(m);
        }
        map.invalidate();
    }

    private Map<String, Marker> getExistingMarkers() {
        Map<String, Marker> markers = new HashMap<>();
        for (org.osmdroid.views.overlay.Overlay o : map.getOverlays()) {
            if (o instanceof Marker) {
                Marker m = (Marker) o;
                if (m.getRelatedObject() instanceof GasolineraAPI) {
                    markers.put(((GasolineraAPI) m.getRelatedObject()).getId(), m);
                }
            }
        }
        return markers;
    }

    private Marker createMarker(GasolineraAPI g, int density, double zoom) {
        Marker m = new Marker(map);
        m.setRelatedObject(g);
        m.setPosition(new GeoPoint(Double.parseDouble(g.getLatitud().replace(",", ".")),
                Double.parseDouble(g.getLongitud().replace(",", "."))));
        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        m.setOnMarkerClickListener((marker, mapView) -> {
            if (markerListener != null) {
                markerListener.onMarkerClick((GasolineraAPI) marker.getRelatedObject());
            }
            return true;
        });
        updateMarker(m, g, density, zoom);
        return m;
    }

    private void updateMarker(Marker m, GasolineraAPI g, int density, double zoom) {
        boolean fav = favoritosManager.esFavorita(g.getId());
        int icon = iconosManager.obtenerIconoGasolinera(fav, density, zoom);
        m.setIcon(IconFlyweightFactory.get(context, icon));
        m.setTitle((fav ? "★ " : "") + g.getRotulo());
    }

    public void refreshMarkers() {
        double zoom = map.getZoomLevel();
        for (org.osmdroid.views.overlay.Overlay o : map.getOverlays()) {
            if (o instanceof Marker && ((Marker) o).getRelatedObject() instanceof GasolineraAPI) {
                updateMarker((Marker) o, (GasolineraAPI) ((Marker) o).getRelatedObject(), lastGasStationCount, zoom);
            }
        }
        map.invalidate();
    }
}
