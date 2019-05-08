package com.a4sys.luciWS.domain;

public class Adjuntos {
    private String tipo; // 1 = id del webservice (dicwebservices), 2 = id del reporte (dicreportes) y 3 = ruta de archivo en físico.
    private String idAdjunto;
    private String ruta;
    private String extension;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIdAdjunto() {
        return idAdjunto;
    }

    public void setIdAdjunto(String idAdjunto) {
        this.idAdjunto = idAdjunto;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return "Adjuntos{" +
                "tipo='" + tipo + '\'' +
                ", idAdjunto='" + idAdjunto + '\'' +
                ", ruta='" + ruta + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }
}
