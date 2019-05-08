package com.a4sys.luciWS.domain;

public class AmazonRequest {
    private String fechaInicio;
    private String fechaFin;
    private String idSftp;
    private String idCampana;
    private String idWebService;
    private String folios;




    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getIdSftp() {
        return idSftp;
    }

    public void setIdSftp(String idSftp) {
        this.idSftp = idSftp;
    }

    public String getIdCampana() {
        return idCampana;
    }

    public void setIdCampana(String idCampana) {
        this.idCampana = idCampana;
    }

    public String getIdWebService() {
        return idWebService;
    }

    public void setIdWebService(String idWebService) {
        this.idWebService = idWebService;
    }

    public String getFolios() {
        return folios;
    }

    public void setFolios(String folios) {
        this.folios = folios;
    }

    @Override
    public String toString() {
        return "AmazonRequest{" +
                "fechaInicio='" + fechaInicio + '\'' +
                ", fechaFin='" + fechaFin + '\'' +
                ", idSftp='" + idSftp + '\'' +
                ", idCampana='" + idCampana + '\'' +
                ", idWebService='" + idWebService + '\'' +
                ", folios='" + folios + '\'' +
                '}';
    }
}
