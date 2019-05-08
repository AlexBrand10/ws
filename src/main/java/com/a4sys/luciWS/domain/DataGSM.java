package com.a4sys.luciWS.domain;

/**
 * Created by mmorones on 5/19/17.
 */
public class DataGSM {

    Long folio;
    Long idTypeMessage;
    String message;


    public Long getFolio() {
        return folio;
    }

    public void setFolio(Long folio) {
        this.folio = folio;
    }

    public Long getIdTypeMessage() {
        return idTypeMessage;
    }

    public void setIdTypeMessage(Long idTypeMessage) {
        this.idTypeMessage = idTypeMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public String toString() {
        return "DataGSM{" +
                "folio=" + folio +
                ", idTypeMessage=" + idTypeMessage +
                ", message='" + message + '\'' +
                '}';
    }
}
