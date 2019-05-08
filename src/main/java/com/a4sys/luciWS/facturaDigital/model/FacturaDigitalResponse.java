package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 5/30/18.
 */
public class FacturaDigitalResponse {

    private String mensaje;
    private String codigo;
    private Cfdi cfdi;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Cfdi getCfdi() {
        return cfdi;
    }

    public void setCfdi(Cfdi cfdi) {
        this.cfdi = cfdi;
    }
}
