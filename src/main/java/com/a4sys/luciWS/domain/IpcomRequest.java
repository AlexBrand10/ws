package com.a4sys.luciWS.domain;

public class IpcomRequest {

    private String idWebService;
    private String url;
    private String token;
    private String report;
    private String action;
    private String dateIni;
    private String dateEnd;
    private String idProveedor;
    private String idTelefoniaCampanaCallCenter;

    private String ip;
    private String function;
    private String id_call;
    private String ftp_server;
    private String ftp_user;
    private String ftp_pass;
    private String remote_path;
    private String new_name_audio;

    private String type;


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
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getReport() {
        return report;
    }
    public void setReport(String report) {
        this.report = report;
    }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDateIni() {
        return dateIni;
    }
    public void setDateIni(String dateIni) {
        this.dateIni = dateIni;
    }
    public String getDateEnd() {
        return dateEnd;
    }
    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }
    public String getIdProveedor() {
        return idProveedor;
    }
    public void setIdProveedor(String idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getFunction() {
        return function;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public String getId_call() {
        return id_call;
    }
    public void setId_call(String id_call) {
        this.id_call = id_call;
    }
    public String getFtp_server() {
        return ftp_server;
    }
    public void setFtp_server(String ftp_server) {
        this.ftp_server = ftp_server;
    }
    public String getFtp_user() {
        return ftp_user;
    }
    public void setFtp_user(String ftp_user) {
        this.ftp_user = ftp_user;
    }
    public String getFtp_pass() {
        return ftp_pass;
    }
    public void setFtp_pass(String ftp_pass) {
        this.ftp_pass = ftp_pass;
    }

    public String getRemote_path() {
        return remote_path;
    }

    public void setRemote_path(String remote_path) {
        this.remote_path = remote_path;
    }

    public String getNew_name_audio() {
        return new_name_audio;
    }

    public void setNew_name_audio(String new_name_audio) {
        this.new_name_audio = new_name_audio;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getIdTelefoniaCampanaCallCenter() {
        return idTelefoniaCampanaCallCenter;
    }

    public void setIdTelefoniaCampanaCallCenter(String idTelefoniaCampanaCallCenter) {
        this.idTelefoniaCampanaCallCenter = idTelefoniaCampanaCallCenter;
    }

    @Override
    public String toString() {
        return "IpcomRequest{" +
                "idWebService='" + idWebService + '\'' +
                ", url='" + url + '\'' +
                ", token='" + token + '\'' +
                ", report='" + report + '\'' +
                ", action='" + action + '\'' +
                ", dateIni='" + dateIni + '\'' +
                ", dateEnd='" + dateEnd + '\'' +
                ", idProveedor='" + idProveedor + '\'' +
                ", idTelefoniaCampanaCallCenter='" + idTelefoniaCampanaCallCenter + '\'' +
                ", ip='" + ip + '\'' +
                ", function='" + function + '\'' +
                ", id_call='" + id_call + '\'' +
                ", ftp_server='" + ftp_server + '\'' +
                ", ftp_user='" + ftp_user + '\'' +
                ", ftp_pass='" + ftp_pass + '\'' +
                ", remote_path='" + remote_path + '\'' +
                ", new_name_audio='" + new_name_audio + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
