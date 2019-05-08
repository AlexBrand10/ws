package com.a4sys.luciWS.yopter;

/**
 * Created by mmorones on 5/14/18.
 */
public class YopterRequest {

    private String user;
    private String password;
    private String idCorporation;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public String getIdCorporation() {
        return idCorporation;
    }

    public void setIdCorporation(String idCorporation) {
        this.idCorporation = idCorporation;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
