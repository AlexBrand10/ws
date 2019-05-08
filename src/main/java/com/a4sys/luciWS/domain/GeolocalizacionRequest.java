package com.a4sys.luciWS.domain;

/**
 * Created by mmorones on 6/6/17.
 */
public class GeolocalizacionRequest {

    String exacta;
    String colonia;
    String cp;
    String estado;
    String municipio;
    String nivelPrecision;

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getExacta() {
        return exacta;
    }

    public void setExacta(String exacta) {
        this.exacta = exacta;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCp() {
        return cp.contains(" XICO,") ? cp.replace(" XICO,",""): cp ;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getNivelPrecision() {
        return nivelPrecision;
    }

    public void setNivelPrecision(String nivelPrecision) {
        this.nivelPrecision = nivelPrecision;
    }
}
