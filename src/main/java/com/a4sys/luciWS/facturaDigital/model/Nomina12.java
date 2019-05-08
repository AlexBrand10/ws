package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 5/30/18.
 */
public class Nomina12 {

    private String Version;
    private String FechaPago;
    private String FechaInicialPago;
    private String FechaFinalPago;
    private String NumDiasPagados;
    private String TipoNomina;
    private String TotalDeducciones;
    private String TotalPercepciones;
    private EmisorComplementoNomina Emisor;
    private ReceptorComplemento Receptor;
    private Percepciones Percepciones;
    private Deduccciones Deducciones;
    private Incapacidades Incapacidades;


    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public String getFechaPago() {
        return FechaPago;
    }

    public void setFechaPago(String fechaPago) {
        FechaPago = fechaPago;
    }

    public String getFechaInicialPago() {
        return FechaInicialPago;
    }

    public void setFechaInicialPago(String fechaInicialPago) {
        FechaInicialPago = fechaInicialPago;
    }

    public String getFechaFinalPago() {
        return FechaFinalPago;
    }

    public void setFechaFinalPago(String fechaFinalPago) {
        FechaFinalPago = fechaFinalPago;
    }

    public String getNumDiasPagados() {
        return NumDiasPagados;
    }

    public void setNumDiasPagados(String numDiasPagados) {
        NumDiasPagados = numDiasPagados;
    }

    public String getTipoNomina() {
        return TipoNomina;
    }

    public void setTipoNomina(String tipoNomina) {
        TipoNomina = tipoNomina;
    }

    public String getTotalDeducciones() {
        return TotalDeducciones;
    }

    public void setTotalDeducciones(String totalDeducciones) {
        TotalDeducciones = totalDeducciones;
    }

    public String getTotalPercepciones() {
        return TotalPercepciones;
    }

    public void setTotalPercepciones(String totalPercepciones) {
        TotalPercepciones = totalPercepciones;
    }

    public EmisorComplementoNomina getEmisor() {
        return Emisor;
    }

    public void setEmisor(EmisorComplementoNomina emisor) {
        Emisor = emisor;
    }

    public ReceptorComplemento getReceptor() {
        return Receptor;
    }

    public void setReceptor(ReceptorComplemento receptor) {
        Receptor = receptor;
    }

    public com.a4sys.luciWS.facturaDigital.model.Percepciones getPercepciones() {
        return Percepciones;
    }

    public void setPercepciones(com.a4sys.luciWS.facturaDigital.model.Percepciones percepciones) {
        Percepciones = percepciones;
    }

    public Deduccciones getDeducciones() {
        return Deducciones;
    }

    public void setDeducciones(Deduccciones deducciones) {
        Deducciones = deducciones;
    }

    public com.a4sys.luciWS.facturaDigital.model.Incapacidades getIncapacidades() {
        return Incapacidades;
    }

    public void setIncapacidades(com.a4sys.luciWS.facturaDigital.model.Incapacidades incapacidades) {
        Incapacidades = incapacidades;
    }
}
