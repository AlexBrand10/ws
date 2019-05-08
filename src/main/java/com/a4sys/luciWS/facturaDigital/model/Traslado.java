package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 6/1/18.
 */
public class Traslado {

    private String Base;
    private String Impuesto;
    private String TipoFactor;
    private String TasaOCuota;
    private String Importe;

    public String getBase() {
        return Base;
    }

    public void setBase(String base) {
        Base = base;
    }

    public String getImpuesto() {
        return Impuesto;
    }

    public void setImpuesto(String impuesto) {
        Impuesto = impuesto;
    }

    public String getTipoFactor() {
        return TipoFactor;
    }

    public void setTipoFactor(String tipoFactor) {
        TipoFactor = tipoFactor;
    }

    public String getTasaOCuota() {
        return TasaOCuota;
    }

    public void setTasaOCuota(String tasaOCuota) {
        TasaOCuota = tasaOCuota;
    }

    public String getImporte() {
        return Importe;
    }

    public void setImporte(String importe) {
        Importe = importe;
    }
}
