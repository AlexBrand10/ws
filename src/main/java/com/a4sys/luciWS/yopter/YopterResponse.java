package com.a4sys.luciWS.yopter;

/**
 * Created by mmorones on 5/14/18.
 */
public class YopterResponse {

    private Integer idUser;
    private String message;
    private String key = "MHh9Mb2wzf";



    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
