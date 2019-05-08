package com.a4sys.luciWS.domain;

public class ClientValue {

    private String field;
    private String value;


    public String getField() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {

        try {

            return new StringBuffer(" { ").append("\"field\" :").append("\"")
                    .append(this.field).append("\"").append(", ")
                    .append("\"value\" :").append("\"").append(this.value).append("\"")
                    .append("}")
                    .toString();

        } catch (Exception e) {
            // TODO: handle exception
            return "BAD REQUEST";
        }

    }
}
