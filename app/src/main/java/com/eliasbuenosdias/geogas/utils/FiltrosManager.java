package com.eliasbuenosdias.geogas.utils;

import com.eliasbuenosdias.geogas.models.GasolineraAPI;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor de filtros para gasolineras.
 * <p>
 * Esta clase proporciona funcionalidad para filtrar listas de gasolineras según
 * múltiples criterios como ubicación, tipo de combustible, precio, servicios y favoritos.
 * Aplica filtros de forma combinada utilizando operadores AND.
 * </p>
 *
 * @author Elías Prieto Parrilla
 * @version 1.0
 */
public class FiltrosManager {
    private String provincia;
    private String municipio;
    private String gasolinera;
    private boolean soloGasolina95 = false;
    private boolean soloGasolina98 = false;
    private boolean soloDiesel = false;
    private boolean soloDieselPremium = false;
    private boolean soloGLP = false;
    private boolean solo24Horas = false;
    private boolean soloFavoritas = false;
    private Double precioMaxGasolina95;
    private Double precioMaxDiesel;

    // Getters y Setters
    /**
     * Obtiene la provincia configurada como filtro.
     *
     * @return la provincia o {@code null} si no hay filtro de provincia
     */
    public String getProvincia() { return provincia; }
    /**
     * Establece el filtro de provincia.
     *
     * @param provincia la provincia a filtrar
     */
    public void setProvincia(String provincia) { this.provincia = provincia; }
    /**
     * Obtiene el municipio configurado como filtro.
     *
     * @return el municipio o {@code null} si no hay filtro de municipio
     */
    public String getMunicipio() { return municipio; }
    /**
     * Establece el filtro de municipio.
     *
     * @param municipio el municipio a filtrar
     */
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    /**
     * Obtiene el nombre de gasolinera configurado como filtro.
     *
     * @return el nombre de la gasolinera o {@code null} si no hay filtro
     */
    public String getGasolinera() { return gasolinera; }
    /**
     * Establece el filtro por nombre de gasolinera.
     *
     * @param gasolinera el nombre de la gasolinera a filtrar
     */
    public void setGasolinera(String gasolinera) { this.gasolinera = gasolinera; }
    /**
     * Verifica si el filtro de Gasolina 95 está activo.
     *
     * @return {@code true} si solo se muestran gasolineras con Gasolina 95
     */
    public boolean isSoloGasolina95() { return soloGasolina95; }
    /**
     * Establece el filtro de Gasolina 95.
     *
     * @param soloGasolina95 {@code true} para mostrar solo gasolineras con Gasolina 95
     */
    public void setSoloGasolina95(boolean soloGasolina95) { this.soloGasolina95 = soloGasolina95; }
    /**
     * Verifica si el filtro de Gasolina 98 está activo.
     *
     * @return {@code true} si solo se muestran gasolineras con Gasolina 98
     */
    public boolean isSoloGasolina98() { return soloGasolina98; }
    /**
     * Establece el filtro de Gasolina 98.
     *
     * @param soloGasolina98 {@code true} para mostrar solo gasolineras con Gasolina 98
     */
    public void setSoloGasolina98(boolean soloGasolina98) { this.soloGasolina98 = soloGasolina98; }
    /**
     * Verifica si el filtro de Diesel está activo.
     *
     * @return {@code true} si solo se muestran gasolineras con Diesel
     */
    public boolean isSoloDiesel() { return soloDiesel; }
    /**
     * Establece el filtro de Diesel.
     *
     * @param soloDiesel {@code true} para mostrar solo gasolineras con Diesel
     */
    public void setSoloDiesel(boolean soloDiesel) { this.soloDiesel = soloDiesel; }
    /**
     * Verifica si el filtro de Diesel Premium está activo.
     *
     * @return {@code true} si solo se muestran gasolineras con Diesel Premium
     */
    public boolean isSoloDieselPremium() { return soloDieselPremium; }
    /**
     * Establece el filtro de Diesel Premium.
     *
     * @param soloDieselPremium {@code true} para mostrar solo gasolineras con Diesel Premium
     */
    public void setSoloDieselPremium(boolean soloDieselPremium) { this.soloDieselPremium = soloDieselPremium; }
    /**
     * Verifica si el filtro de GLP está activo.
     *
     * @return {@code true} si solo se muestran gasolineras con GLP
     */
    public boolean isSoloGLP() { return soloGLP; }
    /**
     * Establece el filtro de GLP.
     *
     * @param soloGLP {@code true} para mostrar solo gasolineras con GLP
     */
    public void setSoloGLP(boolean soloGLP) { this.soloGLP = soloGLP; }
    /**
     * Verifica si el filtro de 24 horas está activo.
     *
     * @return {@code true} si solo se muestran gasolineras abiertas 24 horas
     */
    public boolean isSolo24Horas() { return solo24Horas; }
    /**
     * Establece el filtro de servicio 24 horas.
     *
     * @param solo24Horas {@code true} para mostrar solo gasolineras 24 horas
     */
    public void setSolo24Horas(boolean solo24Horas) { this.solo24Horas = solo24Horas; }
    /**
     * Verifica si el filtro de favoritas está activo.
     *
     * @return {@code true} si solo se muestran gasolineras favoritas
     */
    public boolean isSoloFavoritas() { return soloFavoritas; }
    /**
     * Establece el filtro de gasolineras favoritas.
     *
     * @param soloFavoritas {@code true} para mostrar solo gasolineras favoritas
     */
    public void setSoloFavoritas(boolean soloFavoritas) { this.soloFavoritas = soloFavoritas; }
    /**
     * Obtiene el precio máximo para Gasolina 95.
     *
     * @return el precio máximo o {@code null} si no hay límite
     */
    public Double getPrecioMaxGasolina95() { return precioMaxGasolina95; }
    /**
     * Establece el precio máximo para Gasolina 95.
     *
     * @param precioMaxGasolina95 el precio máximo permitido
     */
    public void setPrecioMaxGasolina95(Double precioMaxGasolina95) { this.precioMaxGasolina95 = precioMaxGasolina95; }
    /**
     * Obtiene el precio máximo para Diesel.
     *
     * @return el precio máximo o {@code null} si no hay límite
     */
    public Double getPrecioMaxDiesel() { return precioMaxDiesel; }
    /**
     * Establece el precio máximo para Diesel.
     *
     * @param precioMaxDiesel el precio máximo permitido
     */
    public void setPrecioMaxDiesel(Double precioMaxDiesel) { this.precioMaxDiesel = precioMaxDiesel; }
    /**
     * Aplica todos los filtros configurados a una lista de gasolineras.
     * <p>
     * Los filtros se aplican de forma combinada con operador AND, es decir,
     * una gasolinera debe cumplir todos los filtros activos para ser incluida.
     * </p>
     *
     * @param gasolineras la lista original de gasolineras
     * @param favoritosManager el gestor de favoritos necesario para el filtro de favoritas
     * @return una nueva lista con las gasolineras que cumplen todos los filtros
     */
    public List<GasolineraAPI> aplicarFiltros(List<GasolineraAPI> gasolineras, FavoritosManager favoritosManager) {
        List<GasolineraAPI> resultado = new ArrayList<>();

        for (GasolineraAPI gasolinera : gasolineras) {
            if (cumpleFiltros(gasolinera, favoritosManager)) {
                resultado.add(gasolinera);
            }
        }

        return resultado;
    }
    /**
     * Verifica si una gasolinera cumple todos los filtros configurados.
     *
     * @param gasolinera la gasolinera a verificar
     * @param favoritosManager el gestor de favoritos
     * @return {@code true} si cumple todos los filtros activos
     */
    private boolean cumpleFiltros(GasolineraAPI gasolinera, FavoritosManager favoritosManager) {
        return cumpleFiltroUbicacion(gasolinera) &&
                cumpleFiltroGasolinera(gasolinera) &&
                cumpleFiltroCombustibles(gasolinera) &&
                cumpleFiltroServicios(gasolinera, favoritosManager) &&
                cumpleFiltroPrecios(gasolinera);
    }
    /**
     * Verifica si la gasolinera cumple los filtros de ubicación.
     *
     * @param gasolinera la gasolinera a verificar
     * @return {@code true} si cumple los filtros de provincia y municipio
     */
    private boolean cumpleFiltroUbicacion(GasolineraAPI gasolinera) {
        if (provincia != null && !provincia.isEmpty()) {
            if (!provincia.equals(gasolinera.getProvincia())) {
                return false;
            }
        }

        if (municipio != null && !municipio.isEmpty()) {
            if (!municipio.equals(gasolinera.getMunicipio())) {
                return false;
            }
        }

        return true;
    }
    /**
     * Verifica si la gasolinera cumple el filtro de nombre.
     * <p>
     * Utiliza coincidencia por prefijo en formato case-insensitive.
     * </p>
     *
     * @param gasolinera la gasolinera a verificar
     * @return {@code true} si el nombre comienza con el filtro especificado
     */
    private boolean cumpleFiltroGasolinera(GasolineraAPI gasolinera) {
        if (this.gasolinera == null || this.gasolinera.isEmpty()) return true;

        String rotulo = gasolinera.getRotulo();
        if (rotulo == null) return false;

        return rotulo.toLowerCase().startsWith(this.gasolinera.toLowerCase());
    }
    /**
     * Verifica si la gasolinera cumple los filtros de combustibles.
     * <p>
     * La gasolinera debe tener disponibles todos los tipos de combustible
     * cuyos filtros estén activos.
     * </p>
     *
     * @param gasolinera la gasolinera a verificar
     * @return {@code true} si tiene todos los combustibles filtrados
     */
    private boolean cumpleFiltroCombustibles(GasolineraAPI gasolinera) {
        if (soloGasolina95 && (gasolinera.getPrecioGasolina95() == null || gasolinera.getPrecioGasolina95().isEmpty())) {
            return false;
        }
        if (soloGasolina98 && (gasolinera.getPrecioGasolina98() == null || gasolinera.getPrecioGasolina98().isEmpty())) {
            return false;
        }
        if (soloDiesel && (gasolinera.getPrecioGasoleoA() == null || gasolinera.getPrecioGasoleoA().isEmpty())) {
            return false;
        }
        if (soloDieselPremium && (gasolinera.getPrecioGasoleoPremium() == null || gasolinera.getPrecioGasoleoPremium().isEmpty())) {
            return false;
        }
        if (soloGLP && (gasolinera.getPrecioGLP() == null || gasolinera.getPrecioGLP().isEmpty())) {
            return false;
        }
        return true;
    }
    /**
     * Verifica si la gasolinera cumple los filtros de servicios.
     *
     * @param gasolinera la gasolinera a verificar
     * @param favoritosManager el gestor de favoritos
     * @return {@code true} si cumple los filtros de 24 horas y favoritas
     */
    private boolean cumpleFiltroServicios(GasolineraAPI gasolinera, FavoritosManager favoritosManager) {
        if (solo24Horas && !"24H".equals(gasolinera.getHorario())) {
            return false;
        }

        if (soloFavoritas && favoritosManager != null) {
            if (gasolinera.getId() == null || !favoritosManager.esFavorita(gasolinera.getId())) {
                return false;
            }
        }

        return true;
    }
    /**
     * Verifica si la gasolinera cumple los filtros de precio máximo.
     * <p>
     * Los precios se comparan con los límites establecidos para Gasolina 95 y Diesel.
     * Maneja correctamente el formato de precios con coma como separador decimal.
     * </p>
     *
     * @param gasolinera la gasolinera a verificar
     * @return {@code true} si los precios están dentro de los límites establecidos
     */
    private boolean cumpleFiltroPrecios(GasolineraAPI gasolinera) {
        try {
            if (precioMaxGasolina95 != null && gasolinera.getPrecioGasolina95() != null) {
                double precio = Double.parseDouble(gasolinera.getPrecioGasolina95().replace(",", "."));
                if (precio > precioMaxGasolina95) return false;
            }

            if (precioMaxDiesel != null && gasolinera.getPrecioGasoleoA() != null) {
                double precio = Double.parseDouble(gasolinera.getPrecioGasoleoA().replace(",", "."));
                if (precio > precioMaxDiesel) return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Elimina todos los filtros configurados, restaurando el estado inicial.
     */
    public void limpiarFiltros() {
        provincia = null;
        municipio = null;
        gasolinera = null;
        soloGasolina95 = false;
        soloGasolina98 = false;
        soloDiesel = false;
        soloDieselPremium = false;
        soloGLP = false;
        solo24Horas = false;
        soloFavoritas = false;
        precioMaxGasolina95 = null;
        precioMaxDiesel = null;
    }
    /**
     * Verifica si hay algún filtro activo.
     *
     * @return {@code true} si al menos un filtro está configurado
     */
    public boolean tieneFiltrosActivos() {
        return (provincia != null && !provincia.isEmpty()) ||
                (municipio != null && !municipio.isEmpty()) ||
                (gasolinera != null && !gasolinera.isEmpty()) ||
                soloGasolina95 ||
                soloGasolina98 ||
                soloDiesel ||
                soloDieselPremium ||
                soloGLP ||
                solo24Horas ||
                soloFavoritas ||
                precioMaxGasolina95 != null ||
                precioMaxDiesel != null;
    }

    /**
     * Captura el estado actual de los filtros como snapshot inmutable.
     *
     * @return un {@link FiltrosEstado} con todos los criterios configurados
     */
    public FiltrosEstado snapshot() {
        return FiltrosEstado.from(this);
    }

    /**
     * Restaura el estado de los filtros a partir de un snapshot.
     *
     * @param estado el estado a aplicar
     */
    public void restore(FiltrosEstado estado) {
        estado.aplicarA(this);
    }

    /**
     * Constructor fluido de {@link FiltrosManager}.
     * <p>
     * Aplica los criterios de forma encadenada sobre una instancia y valida el
     * resultado en {@link #build()}, evitando una cascada de setters.
     * </p>
     */
    public static class Builder {

        private final FiltrosManager filtros;

        /**
         * Construye un builder sobre la instancia destino.
         *
         * @param filtros la instancia de {@link FiltrosManager} a configurar
         */
        public Builder(FiltrosManager filtros) {
            this.filtros = filtros;
        }

        public Builder provincia(String provincia) {
            filtros.provincia = provincia;
            return this;
        }

        public Builder municipio(String municipio) {
            filtros.municipio = municipio;
            return this;
        }

        public Builder gasolinera(String gasolinera) {
            filtros.gasolinera = gasolinera;
            return this;
        }

        public Builder soloGasolina95(boolean soloGasolina95) {
            filtros.soloGasolina95 = soloGasolina95;
            return this;
        }

        public Builder soloGasolina98(boolean soloGasolina98) {
            filtros.soloGasolina98 = soloGasolina98;
            return this;
        }

        public Builder soloDiesel(boolean soloDiesel) {
            filtros.soloDiesel = soloDiesel;
            return this;
        }

        public Builder soloDieselPremium(boolean soloDieselPremium) {
            filtros.soloDieselPremium = soloDieselPremium;
            return this;
        }

        public Builder soloGLP(boolean soloGLP) {
            filtros.soloGLP = soloGLP;
            return this;
        }

        public Builder solo24Horas(boolean solo24Horas) {
            filtros.solo24Horas = solo24Horas;
            return this;
        }

        public Builder soloFavoritas(boolean soloFavoritas) {
            filtros.soloFavoritas = soloFavoritas;
            return this;
        }

        public Builder precioMaxGasolina95(Double precioMaxGasolina95) {
            filtros.precioMaxGasolina95 = precioMaxGasolina95;
            return this;
        }

        public Builder precioMaxDiesel(Double precioMaxDiesel) {
            filtros.precioMaxDiesel = precioMaxDiesel;
            return this;
        }

        /**
         * Valida el estado final y devuelve la instancia configurada.
         *
         * @return el {@link FiltrosManager} configurado
         * @throws IllegalArgumentException si algún precio máximo es negativo
         */
        public FiltrosManager build() {
            if (filtros.precioMaxGasolina95 != null && filtros.precioMaxGasolina95 < 0) {
                throw new IllegalArgumentException("precioMaxGasolina95 no puede ser negativo");
            }
            if (filtros.precioMaxDiesel != null && filtros.precioMaxDiesel < 0) {
                throw new IllegalArgumentException("precioMaxDiesel no puede ser negativo");
            }
            return filtros;
        }
    }
}