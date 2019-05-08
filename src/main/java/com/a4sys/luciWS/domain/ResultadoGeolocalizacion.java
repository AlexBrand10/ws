package com.a4sys.luciWS.domain;

/**
 * Created by mmorones on 6/6/17.
 */
public class ResultadoGeolocalizacion {

    String estatus;
    String error;
    String latitud;
    String longitud;
    String tipoGeolocalizacion;

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getTipoGeolocalizacion() {
        return tipoGeolocalizacion;
    }

    public void setTipoGeolocalizacion(String tipoGeolocalizacion) {
        this.tipoGeolocalizacion = tipoGeolocalizacion;
    }


    @Override
    public String toString() {
        return "ResultadoGeolocalizacion{" +
                "estatus=" + estatus +
                ", error='" + error + '\'' +
                ", latitud='" + latitud + '\'' +
                ", longitud='" + longitud + '\'' +
                ", tipoGeolocaclizacion=" + tipoGeolocalizacion +
                '}';
    }

}
