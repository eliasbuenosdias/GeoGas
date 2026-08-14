package com.eliasbuenosdias.geogas.utils;

/**
 * Estado inmutable de los filtros de {@link FiltrosManager}.
 * <p>
 * Sirve como snapshot para el patrón Command (undo de filtros) y como DTO
 * para transportar el estado de los filtros entre la UI y el ViewModel.
 * </p>
 */
public final class FiltrosEstado {

    private final String provincia;
    private final String municipio;
    private final String gasolinera;
    private final boolean soloGasolina95;
    private final boolean soloGasolina98;
    private final boolean soloDiesel;
    private final boolean soloDieselPremium;
    private final boolean soloGLP;
    private final boolean solo24Horas;
    private final boolean soloFavoritas;
    private final Double precioMaxGasolina95;
    private final Double precioMaxDiesel;

    private FiltrosEstado(String provincia, String municipio, String gasolinera,
            boolean soloGasolina95, boolean soloGasolina98, boolean soloDiesel,
            boolean soloDieselPremium, boolean soloGLP, boolean solo24Horas,
            boolean soloFavoritas, Double precioMaxGasolina95, Double precioMaxDiesel) {
        this.provincia = provincia;
        this.municipio = municipio;
        this.gasolinera = gasolinera;
        this.soloGasolina95 = soloGasolina95;
        this.soloGasolina98 = soloGasolina98;
        this.soloDiesel = soloDiesel;
        this.soloDieselPremium = soloDieselPremium;
        this.soloGLP = soloGLP;
        this.solo24Horas = solo24Horas;
        this.soloFavoritas = soloFavoritas;
        this.precioMaxGasolina95 = precioMaxGasolina95;
        this.precioMaxDiesel = precioMaxDiesel;
    }

    public static FiltrosEstado from(FiltrosManager filtros) {
        return new FiltrosEstado(filtros.getProvincia(), filtros.getMunicipio(),
                filtros.getGasolinera(), filtros.isSoloGasolina95(), filtros.isSoloGasolina98(),
                filtros.isSoloDiesel(), filtros.isSoloDieselPremium(), filtros.isSoloGLP(),
                filtros.isSolo24Horas(), filtros.isSoloFavoritas(),
                filtros.getPrecioMaxGasolina95(), filtros.getPrecioMaxDiesel());
    }

    public static FiltrosEstado sinFiltros() {
        return new Builder().build();
    }

    public void aplicarA(FiltrosManager filtros) {
        filtros.setProvincia(provincia);
        filtros.setMunicipio(municipio);
        filtros.setGasolinera(gasolinera);
        filtros.setSoloGasolina95(soloGasolina95);
        filtros.setSoloGasolina98(soloGasolina98);
        filtros.setSoloDiesel(soloDiesel);
        filtros.setSoloDieselPremium(soloDieselPremium);
        filtros.setSoloGLP(soloGLP);
        filtros.setSolo24Horas(solo24Horas);
        filtros.setSoloFavoritas(soloFavoritas);
        filtros.setPrecioMaxGasolina95(precioMaxGasolina95);
        filtros.setPrecioMaxDiesel(precioMaxDiesel);
    }

    public String getProvincia() {
        return provincia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getGasolinera() {
        return gasolinera;
    }

    public boolean isSoloGasolina95() {
        return soloGasolina95;
    }

    public boolean isSoloGasolina98() {
        return soloGasolina98;
    }

    public boolean isSoloDiesel() {
        return soloDiesel;
    }

    public boolean isSoloDieselPremium() {
        return soloDieselPremium;
    }

    public boolean isSoloGLP() {
        return soloGLP;
    }

    public boolean isSolo24Horas() {
        return solo24Horas;
    }

    public boolean isSoloFavoritas() {
        return soloFavoritas;
    }

    public Double getPrecioMaxGasolina95() {
        return precioMaxGasolina95;
    }

    public Double getPrecioMaxDiesel() {
        return precioMaxDiesel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FiltrosEstado))
            return false;
        FiltrosEstado other = (FiltrosEstado) o;
        return soloGasolina95 == other.soloGasolina95 &&
                soloGasolina98 == other.soloGasolina98 &&
                soloDiesel == other.soloDiesel &&
                soloDieselPremium == other.soloDieselPremium &&
                soloGLP == other.soloGLP &&
                solo24Horas == other.solo24Horas &&
                soloFavoritas == other.soloFavoritas &&
                java.util.Objects.equals(provincia, other.provincia) &&
                java.util.Objects.equals(municipio, other.municipio) &&
                java.util.Objects.equals(gasolinera, other.gasolinera) &&
                java.util.Objects.equals(precioMaxGasolina95, other.precioMaxGasolina95) &&
                java.util.Objects.equals(precioMaxDiesel, other.precioMaxDiesel);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(provincia, municipio, gasolinera, soloGasolina95,
                soloGasolina98, soloDiesel, soloDieselPremium, soloGLP, solo24Horas,
                soloFavoritas, precioMaxGasolina95, precioMaxDiesel);
    }

    /**
     * Builder para {@link FiltrosEstado}, permite construir el estado desde la UI.
     */
    public static class Builder {

        private String provincia;
        private String municipio;
        private String gasolinera;
        private boolean soloGasolina95;
        private boolean soloGasolina98;
        private boolean soloDiesel;
        private boolean soloDieselPremium;
        private boolean soloGLP;
        private boolean solo24Horas;
        private boolean soloFavoritas;
        private Double precioMaxGasolina95;
        private Double precioMaxDiesel;

        public Builder provincia(String provincia) {
            this.provincia = provincia;
            return this;
        }

        public Builder municipio(String municipio) {
            this.municipio = municipio;
            return this;
        }

        public Builder gasolinera(String gasolinera) {
            this.gasolinera = gasolinera;
            return this;
        }

        public Builder soloGasolina95(boolean soloGasolina95) {
            this.soloGasolina95 = soloGasolina95;
            return this;
        }

        public Builder soloGasolina98(boolean soloGasolina98) {
            this.soloGasolina98 = soloGasolina98;
            return this;
        }

        public Builder soloDiesel(boolean soloDiesel) {
            this.soloDiesel = soloDiesel;
            return this;
        }

        public Builder soloDieselPremium(boolean soloDieselPremium) {
            this.soloDieselPremium = soloDieselPremium;
            return this;
        }

        public Builder soloGLP(boolean soloGLP) {
            this.soloGLP = soloGLP;
            return this;
        }

        public Builder solo24Horas(boolean solo24Horas) {
            this.solo24Horas = solo24Horas;
            return this;
        }

        public Builder soloFavoritas(boolean soloFavoritas) {
            this.soloFavoritas = soloFavoritas;
            return this;
        }

        public Builder precioMaxGasolina95(Double precioMaxGasolina95) {
            this.precioMaxGasolina95 = precioMaxGasolina95;
            return this;
        }

        public Builder precioMaxDiesel(Double precioMaxDiesel) {
            this.precioMaxDiesel = precioMaxDiesel;
            return this;
        }

        public FiltrosEstado build() {
            return new FiltrosEstado(provincia, municipio, gasolinera, soloGasolina95,
                    soloGasolina98, soloDiesel, soloDieselPremium, soloGLP, solo24Horas,
                    soloFavoritas, precioMaxGasolina95, precioMaxDiesel);
        }
    }
}