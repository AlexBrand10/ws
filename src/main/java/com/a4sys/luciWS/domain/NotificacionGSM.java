package com.a4sys.luciWS.domain;

/**
 * Created by mmorones on 5/19/17.
 */
public class NotificacionGSM {

    DataGSM data;
    String to;

    public DataGSM getData() {
        return data;
    }

    public void setData(DataGSM data) {
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }



    @Override
    public String toString() {
        return "NotificacionGSM{" +
                "data=" + data.toString() +
                ", to='" + to + '\'' +
                '}';
    }
}
