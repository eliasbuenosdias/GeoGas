package com.eliasbuenosdias.geogas.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * Gestiona la persistencia y aplicación del idioma de la aplicación.
 * Utiliza SharedPreferences para recordar la preferencia del usuario.
 */
public class LocaleManager {

    private static final String PREFS_NAME = "GeoGasLocale";
    private static final String KEY_LANGUAGE = "language";
    private static final String DEFAULT_LANGUAGE = "es";

    /** Idiomas disponibles en la aplicación. */
    public static final String[] AVAILABLE_LANGUAGES = { "es", "en" };
    public static final String[] LANGUAGE_LABELS = { "Español", "English" };

    /**
     * Obtiene el idioma guardado en preferencias.
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    /**
     * Guarda el idioma en preferencias.
     */
    public static void setLanguage(Context context, String language) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    /**
     * Aplica el idioma guardado al contexto dado.
     * Debe llamarse desde attachBaseContext().
     */
    public static Context applyLocale(Context context) {
        String lang = getLanguage(context);
        return updateResources(context, lang);
    }

    /**
     * Cambia el idioma actualizando preferencias y el contexto.
     */
    public static Context changeLocale(Context context, String language) {
        setLanguage(context, language);
        return updateResources(context, language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    /**
     * Devuelve el índice del idioma actual en AVAILABLE_LANGUAGES.
     */
    public static int getCurrentLanguageIndex(Context context) {
        String lang = getLanguage(context);
        for (int i = 0; i < AVAILABLE_LANGUAGES.length; i++) {
            if (AVAILABLE_LANGUAGES[i].equals(lang))
                return i;
        }
        return 0;
    }
}
