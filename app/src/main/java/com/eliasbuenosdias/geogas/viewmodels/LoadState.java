package com.eliasbuenosdias.geogas.viewmodels;

/**
 * Estado cohesivo del ciclo de carga de datos.
 * <p>
 * Reemplaza los LiveData dispersos ({@code isLoading}, {@code progress},
 * {@code statusMessage}) por un único objeto inmutable que encapsula el estado
 * actual de la carga y la información que la UI necesita para mostrarlo.
 * </p>
 */
public class LoadState {

    public enum Status {
        /** Cargando datos desde la API (avanza según {@code progress}). */
        LOADING,
        /** Datos cargados y procesados correctamente. */
        LOADED,
        /** Ocurrió un error durante la carga o el procesamiento. */
        ERROR
    }

    private final Status status;
    private final int progress;
    private final String statusKey;

    public LoadState(Status status, int progress, String statusKey) {
        this.status = status;
        this.progress = progress;
        this.statusKey = statusKey;
    }

    public Status getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    /** Clave i18n del mensaje de estado (resuelta por la UI). */
    public String getStatusKey() {
        return statusKey;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isLoaded() {
        return status == Status.LOADED;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}