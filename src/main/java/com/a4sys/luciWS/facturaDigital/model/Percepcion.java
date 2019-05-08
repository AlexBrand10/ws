package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 5/30/18.
 */
public class Percepcion {

    private String TipoPercepcion;
    private String Clave;
    private String Concepto;
    private String ImporteGravado;
    private String ImporteExento;

    public String getTipoPercepcion() {
        return TipoPercepcion;
    }

    public void setTipoPercepcion(String tipoPercepcion) {
        TipoPercepcion = tipoPercepcion;
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

    public String getImporteGravado() {
        return ImporteGravado;
    }

    public void setImporteGravado(String importeGravado) {
        ImporteGravado = importeGravado;
    }

    public String getImporteExento() {
        return ImporteExento;
    }

    public void setImporteExento(String importeExento) {
        ImporteExento = importeExento;
    }
}
