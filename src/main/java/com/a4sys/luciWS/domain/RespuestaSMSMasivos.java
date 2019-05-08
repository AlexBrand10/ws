package com.a4sys.luciWS.domain;

/**
 * Created by mmorones on 5/17/17.
 */
public class RespuestaSMSMasivos {


    String referenciaId;
    String destinatario;
    String mensaje;
    String respuesta;
    String fechaEnvio;
    String fechaRespuesta;


    public RespuestaSMSMasivos(String referenciaId, String destinatario, String mensaje, String respuesta, String fechaEnvio, String fechaRespuesta) {
        this.referenciaId = referenciaId;
        this.destinatario = destinatario;
        this.mensaje = mensaje;
        this.respuesta = respuesta;
        this.fechaEnvio = fechaEnvio;
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(String referenciaId) {
        this.referenciaId = referenciaId;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(String fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }


    @Override
    public String toString() {
        return "RespuestaSMSMasivos{" +
                "referenciaId='" + referenciaId + '\'' +
                ", destinatario='" + destinatario + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", respuesta='" + respuesta + '\'' +
                ", fechaEnvio='" + fechaEnvio + '\'' +
                ", fechaRespuesta='" + fechaRespuesta + '\'' +
                '}';
    }
}
