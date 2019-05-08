package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 5/30/18.
 */
public class Deduccion {

    private String TipoDeduccion;
    private String Clave;
    private String Concepto;
    private String Importe;

    public String getTipoDeduccion() {
        return TipoDeduccion;
    }

    public void setTipoDeduccion(String tipoDeduccion) {
        TipoDeduccion = tipoDeduccion;
    }

    public String getClave() {
        return Clave;
    }

    public void setClave(String clave) {
        Clave = clave;
    }

    public String getConcepto() {
        return Concepto;
    }

    public void setConcepto(String concepto) {
        Concepto = concepto;
    }

    public String getImporte() {
        return Importe;
    }

    public void setImporte(String importe) {
        Importe = importe;
    }
}
