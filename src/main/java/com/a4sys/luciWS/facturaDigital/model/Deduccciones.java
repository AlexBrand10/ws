package com.a4sys.luciWS.facturaDigital.model;

import java.util.List;

/**
 * Created by mmorones on 5/30/18.
 */
public class Deduccciones {

    private double TotalOtrasDeducciones;
    private String TotalImpuestosRetenidos;
    private List<Deduccion> Deduccion;

    public double getTotalOtrasDeducciones() {
        return TotalOtrasDeducciones;
    }

    public void setTotalOtrasDeducciones(double totalOtrasDeducciones) {
        TotalOtrasDeducciones = totalOtrasDeducciones;
    }

    public String getTotalImpuestosRetenidos() {
        return TotalImpuestosRetenidos;
    }

    public void setTotalImpuestosRetenidos(String totalImpuestosRetenidos) {
        TotalImpuestosRetenidos = totalImpuestosRetenidos;
    }

    public List<com.a4sys.luciWS.facturaDigital.model.Deduccion> getDeduccion() {
        return Deduccion;
    }

    public void setDeduccion(List<com.a4sys.luciWS.facturaDigital.model.Deduccion> deduccion) {
        Deduccion = deduccion;
    }
}
