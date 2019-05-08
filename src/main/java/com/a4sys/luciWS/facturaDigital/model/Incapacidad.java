package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 5/30/18.
 */
public class Incapacidad {

    private String DiasIncapacidad;
    private String TipoIncapacidad;
    private String ImporteMonetario;

    public String getDiasIncapacidad() {
        return DiasIncapacidad;
    }

    public void setDiasIncapacidad(String diasIncapacidad) {
        DiasIncapacidad = diasIncapacidad;
    }

    public String getTipoIncapacidad() {
        return TipoIncapacidad;
    }

    public void setTipoIncapacidad(String tipoIncapacidad) {
        TipoIncapacidad = tipoIncapacidad;
    }

    public String getImporteMonetario() {
        return ImporteMonetario;
    }

    public void setImporteMonetario(String importeMonetario) {
        ImporteMonetario = importeMonetario;
    }
}
