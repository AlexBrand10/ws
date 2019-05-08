package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 5/30/18.
 */
public class Concepto {

    private String ClaveProdServ;
    private String Cantidad;
    private String ClaveUnidad;
    private String Descripcion;
    private String ValorUnitario;
    private String Importe;
    private String Descuento;
    private Impuestos Impuestos;

    public String getClaveProdServ() {
        return ClaveProdServ;
    }

    public void setClaveProdServ(String claveProdServ) {
        ClaveProdServ = claveProdServ;
    }

    public String getCantidad() {
        return Cantidad;
    }

    public void setCantidad(String cantidad) {
        Cantidad = cantidad;
    }

    public String getClaveUnidad() {
        return ClaveUnidad;
    }

    public void setClaveUnidad(String claveUnidad) {
        ClaveUnidad = claveUnidad;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getValorUnitario() {
        return ValorUnitario;
    }

    public void setValorUnitario(String valorUnitario) {
        ValorUnitario = valorUnitario;
    }

    public String getImporte() {
        return Importe;
    }

    public void setImporte(String importe) {
        Importe = importe;
    }

    public String getDescuento() {
        return Descuento;
    }

    public void setDescuento(String descuento) {
        Descuento = descuento;
    }

    public com.a4sys.luciWS.facturaDigital.model.Impuestos getImpuestos() {
        return Impuestos;
    }

    public void setImpuestos(com.a4sys.luciWS.facturaDigital.model.Impuestos impuestos) {
        Impuestos = impuestos;
    }
}
