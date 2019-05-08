package com.a4sys.luciWS.domain;

public class RequestGenericWs {
    private String idCorporation;
    private String phone;
    private String token;
    private String function;
    private String ip;
    private String type;
    private String idTelefoniaProveedor;
    private String idWebService;
    private String url;
    private String id_call;
    private String remote_path;
    private String fechaInicio;
    private String fechaFin;
    private String idSftp;
    private String idsEstadoSolicitud;
    private String borraArchivo;


    public String getIdCorporation() { return idCorporation; }
    public void setIdCorporation(String idCorporation) { this.idCorporation = idCorporation; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdTelefoniaProveedor() {
        return idTelefoniaProveedor;
    }

    public void setIdTelefoniaProveedor(String idTelefoniaProveedor) {
        this.idTelefoniaProveedor = idTelefoniaProveedor;
    }

    public String getIdWebService() {
        return idWebService;
    }

    public void setIdWebService(String idWebService) {
        this.idWebService = idWebService;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId_call() {
        return id_call;
    }

    public void setId_call(String id_call) {
        this.id_call = id_call;
    }



    public String getRemote_path() {
        return remote_path;
    }

    public void setRemote_path(String remote_path) {
        this.remote_path = remote_path;
    }

    @Override
    public String toString() {
        return "RequestGenericWs{" +
                "idCorporation='" + idCorporation + '\'' +
                ", phone='" + phone + '\'' +
                ", token='" + token + '\'' +
                ", function='" + function + '\'' +
                ", ip='" + ip + '\'' +
                ", type='" + type + '\'' +
                ", idTelefoniaProveedor='" + idTelefoniaProveedor + '\'' +
                ", idWebService='" + idWebService + '\'' +
                ", url='" + url + '\'' +
                ", id_call='" + id_call + '\'' +
                ", remote_path='" + remote_path + '\'' +
                ", fechaInicio='" + fechaInicio + '\'' +
                ", fechaFin='" + fechaFin + '\'' +
                ", idSftp='" + idSftp + '\'' +
                ", idsEstadoSolicitud='" + idsEstadoSolicitud + '\'' +
                ", borraArchivo='" + borraArchivo + '\'' +
                '}';
    }

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

    public String getIdsEstadoSolicitud() {
        return idsEstadoSolicitud;
    }

    public void setIdsEstadoSolicitud(String idsEstadoSolicitud) {
        this.idsEstadoSolicitud = idsEstadoSolicitud;
    }

    public String getBorraArchivo() {
        return borraArchivo;
    }

    public void setBorraArchivo(String borraArchivo) {
        this.borraArchivo = borraArchivo;
    }
}
