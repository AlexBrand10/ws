package com.a4sys.luciWS.amazonSMS;

/**
 * Created by mmorones on 4/18/18.
 */
public class AmazonSMSRequest {

    private String idWebService;
    private String phone;
    private String message;


    public String getIdWebService() {
        return idWebService;
    }

    public void setIdWebService(String idWebService) {
        this.idWebService = idWebService;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
