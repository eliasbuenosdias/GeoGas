    package com.example.geogas.models;

    import com.google.gson.annotations.SerializedName;

    /**
     * Modelo de datos para representar una estación de servicio (gasolinera) de la API de Miteco.
     * Contiene toda la información sobre ubicación, precios de combustibles, horarios y servicios
     * disponibles en una estación terrestre de servicio.
     * Utiliza anotaciones Gson para mapear los campos JSON de la API REST del Ministerio.
     *
     * @author Elías Prieto Parrilla
     * @version 1.0
     */
    public class GasolineraAPI {

        // === IDENTIFICACIÓN Y UBICACIÓN ===
        /**
         * Identificador único de la estación de servicio.
         */
        @SerializedName("IDEESS")
        private String id;
        /**
         * Rótulo o marca comercial de la gasolinera.
         */
        @SerializedName("Rótulo")
        private String rotulo;
        /**
         * Código postal de la ubicación de la estación.
         */
        @SerializedName("C.P.")
        private String codigoPostal;
        /**
         * Dirección completa de la estación de servicio.
         */
        @SerializedName("Dirección")
        private String direccion;
        /**
         * Localidad donde se encuentra la estación.
         */
        @SerializedName("Localidad")
        private String localidad;
        /**
         * Municipio al que pertenece la estación.
         */
        @SerializedName("Municipio")
        private String municipio;
        /**
         * Provincia donde está ubicada la estación.
         */
        @SerializedName("Provincia")
        private String provincia;
        /**
         * Latitud de la ubicación geográfica de la estación.
         */
        @SerializedName("Latitud")
        private String latitud;
        /**
         * Longitud de la ubicación geográfica en formato WGS84.
         */
        @SerializedName("Longitud (WGS84)")
        private String longitud;

        // === HORARIOS Y SERVICIOS ===
        /**
         * Horario de apertura y cierre de la estación.
         */
        @SerializedName("Horario")
        private String horario;
        /**
         * Tipo de venta de la estación.
         * Valores posibles: "P" = Público, "R" = Restringido.
         */
        @SerializedName("Tipo Venta")
        private String tipoVenta; // "P" = Público, "R" = Restringido
        /**
         * Indicador de remisión de datos.
         * Valores posibles: "dm" = datos obligatorios, "np" = no disponible.
         */
        @SerializedName("Remisión")
        private String remision; // "dm" = datos obligatorios, "np" = no disponible
        /**
         * Tipo de margen comercial de la estación.
         * Valores posibles: "I" = Integrada, "C" = Competición.
         */
        @SerializedName("Margen")
        private String margen; // "I" = Integrada, "C" = Competición

        // === GASOLINAS ===
        /**
         * Precio de la gasolina 95 E5 en euros por litro.
         */
        @SerializedName("Precio Gasolina 95 E5")
        private String precioGasolina95;
        /**
         * Precio de la gasolina 95 E10 en euros por litro.
         */
        @SerializedName("Precio Gasolina 95 E10")
        private String precioGasolina95E10;
        /**
         * Precio de la gasolina 98 E5 en euros por litro.
         */
        @SerializedName("Precio Gasolina 98 E5")
        private String precioGasolina98;
        /**
         * Precio de la gasolina 98 E10 en euros por litro.
         */
        @SerializedName("Precio Gasolina 98 E10")
        private String precioGasolina98E10;

        // === DIÉSEL/GASÓLEOS ===
        /**
         * Precio del gasóleo A (diésel estándar) en euros por litro.
         */
        @SerializedName("Precio Gasoleo A")
        private String precioGasoleoA; // Diésel estándar
        /**
         * Precio del gasóleo B (agrícola) en euros por litro.
         */
        @SerializedName("Precio Gasoleo B")
        private String precioGasoleoB; // Agrícola
        /**
         * Precio del gasóleo C (calefacción) en euros por litro.
         */
        @SerializedName("Precio Gasoleo C")
        private String precioGasoleoC; // Calefacción
        /**
         * Precio del gasóleo premium en euros por litro.
         */
        @SerializedName("Precio Gasoleo Premium")
        private String precioGasoleoPremium;

        // === COMBUSTIBLES ALTERNATIVOS ===
        /**
         * Precio de los gases licuados del petróleo (GLP) en euros por litro.
         */
        @SerializedName("Precio Gases licuados del petróleo")
        private String precioGLP;
        /**
         * Precio del gas natural comprimido (GNC) en euros por kilogramo.
         */
        @SerializedName("Precio Gas Natural Comprimido")
        private String precioGNC;
        /**
         * Precio del gas natural licuado (GNL) en euros por kilogramo.
         */
        @SerializedName("Precio Gas Natural Licuado")
        private String precioGNL;
        /**
         * Precio del hidrógeno en euros por kilogramo.
         */
        @SerializedName("Precio Hidrogeno")
        private String precioHidrogeno;
        /**
         * Precio del biodiesel en euros por litro.
         */
        @SerializedName("Precio Biodiesel")
        private String precioBiodiesel;
        /**
         * Precio del bioetanol en euros por litro.
         */
        @SerializedName("Precio Bioetanol")
        private String precioBioetanol;

        // === INFORMACIÓN ADICIONAL ===
        /**
         * Porcentaje de bioetanol en el combustible.
         */
        @SerializedName("Porcentaje Bioetanol")
        private String porcentajeBioetanol;
        /**
         * Porcentaje de biodiesel en el combustible.
         */
        @SerializedName("Porcentaje Biodiesel")
        private String porcentajeBiodiesel;
        /**
         * Información sobre éster metílico en el combustible.
         */
        @SerializedName("Éster metílico")
        private String esterMetilico;
        /**
         * Información sobre bioalcohol en el combustible.
         */
        @SerializedName("Bioalcohol")
        private String bioalcohol;

        // === FECHAS Y ESTADO ===
        /**
         * Fecha de actualización de los datos de la estación.
         */
        @SerializedName("Fecha")
        private String fecha; // Fecha de actualización
        /**
         * Porcentaje de etanol en el combustible.
         */
        @SerializedName("% Etanol")
        private String porcentajeEtanol;
        /**
         * Fecha de actualización del éster metílico.
         */
        @SerializedName("F. ester metílico")
        private String fechaEsterMetilico;

        // Getters y Setters para TODOS los campos
        /**
         * Obtiene el identificador único de la estación.
         *
         * @return el identificador de la estación
         */
        public String getId() { return id; }
        /**
         * Establece el identificador único de la estación.
         *
         * @param id el identificador a establecer
         */
        public void setId(String id) { this.id = id; }
        /**
         * Obtiene el rótulo o marca comercial de la gasolinera.
         *
         * @return el rótulo de la estación
         */
        public String getRotulo() { return rotulo; }
        /**
         * Establece el rótulo o marca comercial de la gasolinera.
         *
         * @param rotulo el rótulo a establecer
         */
        public void setRotulo(String rotulo) { this.rotulo = rotulo; }
        /**
         * Obtiene el código postal de la estación.
         *
         * @return el código postal
         */
        public String getCodigoPostal() { return codigoPostal; }
        /**
         * Establece el código postal de la estación.
         *
         * @param codigoPostal el código postal a establecer
         */
        public void setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }
        /**
         * Obtiene la dirección completa de la estación.
         *
         * @return la dirección de la estación
         */
        public String getDireccion() { return direccion; }
        /**
         * Establece la dirección completa de la estación.
         *
         * @param direccion la dirección a establecer
         */
        public void setDireccion(String direccion) { this.direccion = direccion; }
        /**
         * Obtiene la localidad donde se encuentra la estación.
         *
         * @return la localidad de la estación
         */
        public String getLocalidad() { return localidad; }
        /**
         * Establece la localidad donde se encuentra la estación.
         *
         * @param localidad la localidad a establecer
         */
        public void setLocalidad(String localidad) { this.localidad = localidad; }
        /**
         * Obtiene el municipio al que pertenece la estación.
         *
         * @return el municipio de la estación
         */
        public String getMunicipio() { return municipio; }
        /**
         * Establece el municipio al que pertenece la estación.
         *
         * @param municipio el municipio a establecer
         */
        public void setMunicipio(String municipio) { this.municipio = municipio; }
        /**
         * Obtiene la provincia donde está ubicada la estación.
         *
         * @return la provincia de la estación
         */
        public String getProvincia() { return provincia; }
        /**
         * Establece la provincia donde está ubicada la estación.
         *
         * @param provincia la provincia a establecer
         */
        public void setProvincia(String provincia) { this.provincia = provincia; }
        /**
         * Obtiene la latitud de la ubicación geográfica.
         *
         * @return la latitud de la estación
         */
        public String getLatitud() { return latitud; }
        /**
         * Establece la latitud de la ubicación geográfica.
         *
         * @param latitud la latitud a establecer
         */
        public void setLatitud(String latitud) { this.latitud = latitud; }
        /**
         * Obtiene la longitud de la ubicación geográfica en formato WGS84.
         *
         * @return la longitud de la estación
         */
        public String getLongitud() { return longitud; }
        /**
         * Establece la longitud de la ubicación geográfica en formato WGS84.
         *
         * @param longitud la longitud a establecer
         */
        public void setLongitud(String longitud) { this.longitud = longitud; }
        /**
         * Obtiene el horario de apertura y cierre.
         *
         * @return el horario de la estación
         */
        public String getHorario() { return horario; }
        /**
         * Establece el horario de apertura y cierre.
         *
         * @param horario el horario a establecer
         */
        public void setHorario(String horario) { this.horario = horario; }
        /**
         * Obtiene el tipo de venta de la estación.
         *
         * @return el tipo de venta (P = Público, R = Restringido)
         */
        public String getTipoVenta() { return tipoVenta; }
        /**
         * Establece el tipo de venta de la estación.
         *
         * @param tipoVenta el tipo de venta a establecer
         */
        public void setTipoVenta(String tipoVenta) { this.tipoVenta = tipoVenta; }
        /**
         * Obtiene el indicador de remisión de datos.
         *
         * @return el indicador de remisión
         */
        public String getRemision() { return remision; }
        /**
         * Establece el indicador de remisión de datos.
         *
         * @param remision el indicador a establecer
         */
        public void setRemision(String remision) { this.remision = remision; }
        /**
         * Obtiene el tipo de margen comercial.
         *
         * @return el tipo de margen (I = Integrada, C = Competición)
         */
        public String getMargen() { return margen; }
        /**
         * Establece el tipo de margen comercial.
         *
         * @param margen el tipo de margen a establecer
         */
        public void setMargen(String margen) { this.margen = margen; }
        /**
         * Obtiene el precio de la gasolina 95 E5.
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasolina95() { return precioGasolina95; }
        /**
         * Establece el precio de la gasolina 95 E5.
         *
         * @param precioGasolina95 el precio a establecer
         */
        public void setPrecioGasolina95(String precioGasolina95) { this.precioGasolina95 = precioGasolina95; }
        /**
         * Obtiene el precio de la gasolina 95 E10.
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasolina95E10() { return precioGasolina95E10; }
        /**
         * Establece el precio de la gasolina 95 E10.
         *
         * @param precioGasolina95E10 el precio a establecer
         */
        public void setPrecioGasolina95E10(String precioGasolina95E10) { this.precioGasolina95E10 = precioGasolina95E10; }
        /**
         * Obtiene el precio de la gasolina 98 E5.
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasolina98() { return precioGasolina98; }
        /**
         * Establece el precio de la gasolina 98 E5.
         *
         * @param precioGasolina98 el precio a establecer
         */
        public void setPrecioGasolina98(String precioGasolina98) { this.precioGasolina98 = precioGasolina98; }
        /**
         * Obtiene el precio de la gasolina 98 E10.
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasolina98E10() { return precioGasolina98E10; }
        /**
         * Establece el precio de la gasolina 98 E10.
         *
         * @param precioGasolina98E10 el precio a establecer
         */
        public void setPrecioGasolina98E10(String precioGasolina98E10) { this.precioGasolina98E10 = precioGasolina98E10; }
        /**
         * Obtiene el precio del gasóleo A (diésel estándar).
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasoleoA() { return precioGasoleoA; }
        /**
         * Establece el precio del gasóleo A (diésel estándar).
         *
         * @param precioGasoleoA el precio a establecer
         */
        public void setPrecioGasoleoA(String precioGasoleoA) { this.precioGasoleoA = precioGasoleoA; }
        /**
         * Obtiene el precio del gasóleo B (agrícola).
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasoleoB() { return precioGasoleoB; }
        /**
         * Establece el precio del gasóleo B (agrícola).
         *
         * @param precioGasoleoB el precio a establecer
         */
        public void setPrecioGasoleoB(String precioGasoleoB) { this.precioGasoleoB = precioGasoleoB; }
        /**
         * Obtiene el precio del gasóleo C (calefacción).
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasoleoC() { return precioGasoleoC; }
        /**
         * Establece el precio del gasóleo C (calefacción).
         *
         * @param precioGasoleoC el precio a establecer
         */
        public void setPrecioGasoleoC(String precioGasoleoC) { this.precioGasoleoC = precioGasoleoC; }
        /**
         * Obtiene el precio del gasóleo premium.
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGasoleoPremium() { return precioGasoleoPremium; }
        /**
         * Establece el precio del gasóleo premium.
         *
         * @param precioGasoleoPremium el precio a establecer
         */
        public void setPrecioGasoleoPremium(String precioGasoleoPremium) { this.precioGasoleoPremium = precioGasoleoPremium; }
        /**
         * Obtiene el precio de los gases licuados del petróleo (GLP).
         *
         * @return el precio en euros por litro
         */
        public String getPrecioGLP() { return precioGLP; }
        /**
         * Establece el precio de los gases licuados del petróleo (GLP).
         *
         * @param precioGLP el precio a establecer
         */
        public void setPrecioGLP(String precioGLP) { this.precioGLP = precioGLP; }
        /**
         * Obtiene el precio del gas natural comprimido (GNC).
         *
         * @return el precio en euros por kilogramo
         */
        public String getPrecioGNC() { return precioGNC; }
        /**
         * Establece el precio del gas natural comprimido (GNC).
         *
         * @param precioGNC el precio a establecer
         */
        public void setPrecioGNC(String precioGNC) { this.precioGNC = precioGNC; }
        /**
         * Obtiene el precio del gas natural licuado (GNL).
         *
         * @return el precio en euros por kilogramo
         */
        public String getPrecioGNL() { return precioGNL; }
        /**
         * Establece el precio del gas natural licuado (GNL).
         *
         * @param precioGNL el precio a establecer
         */
        public void setPrecioGNL(String precioGNL) { this.precioGNL = precioGNL; }
        /**
         * Obtiene el precio del hidrógeno.
         *
         * @return el precio en euros por kilogramo
         */
        public String getPrecioHidrogeno() { return precioHidrogeno; }
        /**
         * Establece el precio del hidrógeno.
         *
         * @param precioHidrogeno el precio a establecer
         */
        public void setPrecioHidrogeno(String precioHidrogeno) { this.precioHidrogeno = precioHidrogeno; }
        /**
         * Obtiene el precio del biodiesel.
         *
         * @return el precio en euros por litro
         */
        public String getPrecioBiodiesel() { return precioBiodiesel; }
        /**
         * Establece el precio del biodiesel.
         *
         * @param precioBiodiesel el precio a establecer
         */
        public void setPrecioBiodiesel(String precioBiodiesel) { this.precioBiodiesel = precioBiodiesel; }
        /**
         * Obtiene el precio del bioetanol.
         *
         * @return el precio en euros por litro
         */
        public String getPrecioBioetanol() { return precioBioetanol; }
        /**
         * Establece el precio del bioetanol.
         *
         * @param precioBioetanol el precio a establecer
         */
        public void setPrecioBioetanol(String precioBioetanol) { this.precioBioetanol = precioBioetanol; }
        /**
         * Obtiene el porcentaje de bioetanol en el combustible.
         *
         * @return el porcentaje de bioetanol
         */
        public String getPorcentajeBioetanol() { return porcentajeBioetanol; }
        /**
         * Establece el porcentaje de bioetanol en el combustible.
         *
         * @param porcentajeBioetanol el porcentaje a establecer
         */
        public void setPorcentajeBioetanol(String porcentajeBioetanol) { this.porcentajeBioetanol = porcentajeBioetanol; }
        /**
         * Obtiene el porcentaje de biodiesel en el combustible.
         *
         * @return el porcentaje de biodiesel
         */
        public String getPorcentajeBiodiesel() { return porcentajeBiodiesel; }
        /**
         * Establece el porcentaje de biodiesel en el combustible.
         *
         * @param porcentajeBiodiesel el porcentaje a establecer
         */
        public void setPorcentajeBiodiesel(String porcentajeBiodiesel) { this.porcentajeBiodiesel = porcentajeBiodiesel; }
        /**
         * Obtiene la información sobre éster metílico.
         *
         * @return la información del éster metílico
         */
        public String getEsterMetilico() { return esterMetilico; }
        /**
         * Establece la información sobre éster metílico.
         *
         * @param esterMetilico la información a establecer
         */
        public void setEsterMetilico(String esterMetilico) { this.esterMetilico = esterMetilico; }
        /**
         * Obtiene la información sobre bioalcohol.
         *
         * @return la información del bioalcohol
         */
        public String getBioalcohol() { return bioalcohol; }
        /**
         * Establece la información sobre bioalcohol.
         *
         * @param bioalcohol la información a establecer
         */
        public void setBioalcohol(String bioalcohol) { this.bioalcohol = bioalcohol; }
        /**
         * Obtiene la fecha de actualización de los datos.
         *
         * @return la fecha de actualización
         */
        public String getFecha() { return fecha; }
        /**
         * Establece la fecha de actualización de los datos.
         *
         * @param fecha la fecha a establecer
         */
        public void setFecha(String fecha) { this.fecha = fecha; }
        /**
         * Obtiene el porcentaje de etanol en el combustible.
         *
         * @return el porcentaje de etanol
         */
        public String getPorcentajeEtanol() { return porcentajeEtanol; }
        /**
         * Establece el porcentaje de etanol en el combustible.
         *
         * @param porcentajeEtanol el porcentaje a establecer
         */
        public void setPorcentajeEtanol(String porcentajeEtanol) { this.porcentajeEtanol = porcentajeEtanol; }
        /**
         * Obtiene la fecha de actualización del éster metílico.
         *
         * @return la fecha de actualización
         */
        public String getFechaEsterMetilico() { return fechaEsterMetilico; }
        /**
         * Establece la fecha de actualización del éster metílico.
         *
         * @param fechaEsterMetilico la fecha a establecer
         */
        public void setFechaEsterMetilico(String fechaEsterMetilico) { this.fechaEsterMetilico = fechaEsterMetilico; }
    }