package com.a4sys.luciWS.domain;

public class ClientInfo {

    private Long iduser;
    private String user;
    private String password;
    private String folio;
    private String message;
    private String detail;
    private Long idLog;


    public ClientInfo() {
        this.iduser = 0L;
        this.user = "";
        this.password = "";
        this.folio = "";
        this.message = "";
        this.detail = "";
        this.idLog = 0L;
    }

    /**
     * @return the idUser, este dato representa el emisor de crédito (idEmisorCredito)
     */

    public Long getIdUser() {
        return iduser;
    }

    /**
     * @param iduser to set, este dato representa el emisor de crédito (idEmisorCredito)
     */

    public void setIdUser(Long iduser){
        if(iduser == null){
            this.iduser = 0L;
        }else{
            this.iduser = iduser;
        }

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Long getIdLog() {
        return idLog;
    }

    public void setIdLog(Long idLog) {
        this.idLog = idLog;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getRespuesta() {

        return new StringBuffer(" { ")
                .append("\"folio\" :").append("\"").append((this.getFolio().length() > 0) ? this.getFolio() : "").append("\"").append(", ")
                .append("\"message\" :").append("\"").append(this.getMessage()).append("\"").append(", ")
                .append("\"details\" :").append("[").append(this.getDetail()).append("]")
                .append("}").toString();


    }

    @Override
    public String toString() {

        try {

            return new StringBuffer(" { ").append("\"idUser\" :").append("\"")
                    .append(this.iduser).append("\"").append(", ")
                    .append("\"user\" :").append("\"").append(this.user)
                    .append("\"").append(", ").append("\"password\" :")
                    .append("\"").append(this.password).append("\"").append(", ")
                    .append("\"folio\" :").append("\"").append(this.folio).append("\"")
                    .append("}")
                    .toString();

        } catch (Exception e) {
            // TODO: handle exception
            return "BAD REQUEST";
        }

    }
}

