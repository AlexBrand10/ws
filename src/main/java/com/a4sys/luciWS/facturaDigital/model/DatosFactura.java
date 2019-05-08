package com.a4sys.luciWS.facturaDigital.model;

import java.util.List;

/**
 * Created by mmorones on 5/30/18.
 */
public class DatosFactura {

    private String Serie;
    private String Folio;
    private String Fecha;
    private String SubTotal;
    private String Descuento;
    private String Moneda;
    private String Total;
    private String TipoDeComprobante;
    private String FormaPago;
    private String CondicionesDePago;
    private String MetodoPago;
    private String TipoCambio;
    private String LugarExpedicion;
    private String NombreLugarExpedicion;
    private Emisor Emisor;
    private Receptor Receptor;
    private List<Concepto> Conceptos;
    private Impuestos Impuestos;
    private Complemento Complemento;

    public String getSerie() {
        return Serie;
    }

    public void setSerie(String serie) {
        Serie = serie;
    }

    public String getFolio() {
        return Folio;
    }

    public void setFolio(String folio) {
        Folio = folio;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    public String getSubTotal() {
        return SubTotal;
    }

    public void setSubTotal(String subTotal) {
        SubTotal = subTotal;
    }

    public String getDescuento() {
        return Descuento;
    }

    public void setDescuento(String descuento) {
        Descuento = descuento;
    }

    public String getMoneda() {
        return Moneda;
    }

    public void setMoneda(String moneda) {
        Moneda = moneda;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public String getTipoDeComprobante() {
        return TipoDeComprobante;
    }

    public void setTipoDeComprobante(String tipoDeComprobante) {
        TipoDeComprobante = tipoDeComprobante;
    }

    public String getFormaPago() {
        return FormaPago;
    }

    public void setFormaPago(String formaPago) {
        FormaPago = formaPago;
    }

    public String getCondicionesDePago() {
        return CondicionesDePago;
    }

    public void setCondicionesDePago(String condicionesDePago) {
        CondicionesDePago = condicionesDePago;
    }

    public String getMetodoPago() {
        return MetodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        MetodoPago = metodoPago;
    }

    public String getTipoCambio() {
        return TipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        TipoCambio = tipoCambio;
    }

    public String getLugarExpedicion() {
        return LugarExpedicion;
    }

    public void setLugarExpedicion(String lugarExpedicion) {
        LugarExpedicion = lugarExpedicion;
    }

    public String getNombreLugarExpedicion() {
        return NombreLugarExpedicion;
    }

    public void setNombreLugarExpedicion(String nombreLugarExpedicion) {
        NombreLugarExpedicion = nombreLugarExpedicion;
    }

    public com.a4sys.luciWS.facturaDigital.model.Emisor getEmisor() {
        return Emisor;
    }

    public void setEmisor(com.a4sys.luciWS.facturaDigital.model.Emisor emisor) {
        Emisor = emisor;
    }

    public com.a4sys.luciWS.facturaDigital.model.Receptor getReceptor() {
        return Receptor;
    }

    public void setReceptor(com.a4sys.luciWS.facturaDigital.model.Receptor receptor) {
        Receptor = receptor;
    }

    public com.a4sys.luciWS.facturaDigital.model.Impuestos getImpuestos() {
        return Impuestos;
    }

    public void setImpuestos(com.a4sys.luciWS.facturaDigital.model.Impuestos impuestos) {
        Impuestos = impuestos;
    }

    public List<Concepto> getConceptos() {
        return Conceptos;
    }

    public void setConceptos(List<Concepto> conceptos) {
        Conceptos = conceptos;
    }

    public com.a4sys.luciWS.facturaDigital.model.Complemento getComplemento() {
        return Complemento;
    }

    public void setComplemento(com.a4sys.luciWS.facturaDigital.model.Complemento complemento) {
        Complemento = complemento;
    }
}
