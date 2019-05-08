package com.a4sys.luciWS.facturaDigital.model;

import java.util.List;

/**
 * Created by mmorones on 5/30/18.
 */
public class Percepciones {

    private double TotalGravado;
    private double TotalExento;
    private String TotalSueldos;
    private List<Percepcion> Percepcion;

    public double getTotalGravado() {
        return TotalGravado;
    }

    public void setTotalGravado(double totalGravado) {
        TotalGravado = totalGravado;
    }

    public double getTotalExento() {
        return TotalExento;
    }

    public void setTotalExento(double totalExento) {
        TotalExento = totalExento;
    }

    public String getTotalSueldos() {
        return TotalSueldos;
    }

    public void setTotalSueldos(String totalSueldos) {
        TotalSueldos = totalSueldos;
    }

    public List<com.a4sys.luciWS.facturaDigital.model.Percepcion> getPercepcion() {
        return Percepcion;
    }

    public void setPercepcion(List<com.a4sys.luciWS.facturaDigital.model.Percepcion> percepcion) {
        Percepcion = percepcion;
    }
}
