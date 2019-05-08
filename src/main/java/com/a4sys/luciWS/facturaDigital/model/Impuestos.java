package com.a4sys.luciWS.facturaDigital.model;

import java.util.List;

/**
 * Created by mmorones on 6/1/18.
 */
public class Impuestos {

    private String TotalImpuestosTrasladados;
    private List<Traslado> Traslados;

    public String getTotalImpuestosTrasladados() {
        return TotalImpuestosTrasladados;
    }

    public void setTotalImpuestosTrasladados(String totalImpuestosTrasladados) {
        TotalImpuestosTrasladados = totalImpuestosTrasladados;
    }

    public List<Traslado> getTraslados() {
        return Traslados;
    }

    public void setTraslados(List<Traslado> traslados) {
        Traslados = traslados;
    }
}
