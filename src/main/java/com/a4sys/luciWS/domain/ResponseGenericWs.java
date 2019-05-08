package com.a4sys.luciWS.domain;

public class ResponseGenericWs {
    private Boolean success;
    private String description;
    private String result;

    public ResponseGenericWs() {
        this.success =  false;
    }
    public Boolean getSuccess() {
        return success;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResponseGenericWs{" +
                "success=" + success +
                ", description='" + description + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
