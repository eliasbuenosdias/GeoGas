package com.eliasbuenosdias.geogas.ui.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Simplifica la gestión de permisos en Android.
 */
public class PermissionHelper {

    public static final int REQ_PERMISSIONS = 100;

    public static boolean hasLocationPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQ_PERMISSIONS);
    }
}
