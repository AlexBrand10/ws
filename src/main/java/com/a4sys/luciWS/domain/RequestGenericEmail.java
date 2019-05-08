package com.a4sys.luciWS.domain;

import java.util.List;

public class RequestGenericEmail {
    private String folio;
    private String idNotificacionEmail;
    private String email;
    private String idUsuario;
    private String idCorporation;

    private List<Adjuntos> adjuntos;

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getIdNotificacionEmail() {
        return idNotificacionEmail;
    }

    public void setIdNotificacionEmail(String idNotificacionEmail) {
        this.idNotificacionEmail = idNotificacionEmail;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdCorporation() { return idCorporation; }

    public void setIdCorporation(String idCorporation) { this.idCorporation = idCorporation; }

    public List<Adjuntos> getAdjuntos() {
        return adjuntos;
    }

    public void setAdjuntos(List<Adjuntos> adjuntos) {
        this.adjuntos = adjuntos;
    }

    @Override
    public String toString() {
        return "RequestGenericEmail{" +
                "folio='" + folio + '\'' +
                ", idNotificacionEmail='" + idNotificacionEmail + '\'' +
                ", email='" + email + '\'' +
                ", idUsuario='" + idUsuario + '\'' +
                ", idCorporation='" + idCorporation + '\'' +
                ", adjuntos=" + adjuntos +
                '}';
    }
}
