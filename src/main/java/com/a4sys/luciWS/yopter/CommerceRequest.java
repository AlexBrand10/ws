package com.a4sys.luciWS.yopter;

import java.lang.reflect.InvocationHandler;

public class CommerceRequest {

    private CommerceData commerceData;
    private int idCorporation;
    private int idLuciUser;
    private Payment payment;
    private InVoice invoice;
    private Plan plan;
    private String folioLuci;
    private String fechaCompromiso;

    public CommerceData getCommerceData() { return commerceData; }

    public void setCommerceData(CommerceData commerceData) { this.commerceData = commerceData; }

    public int getIdCorporation() { return idCorporation; }

    public void setIdCorporation(int idCorporation) { this.idCorporation = idCorporation; }

    public int getIdLuciUser() { return idLuciUser; }

    public void setIdLuciUser(int idLuciUser) { this.idLuciUser = idLuciUser; }

    public Payment getPayment() { return payment; }

    public void setPayment(Payment payment) { this.payment = payment; }

    public InVoice getInvoice() { return invoice; }

    public void setInvoice(InVoice invoice) { this.invoice = invoice; }

    public Plan getPlan() { return plan; }

    public void setPlan(Plan plan) { this.plan = plan; }

    public String getFolioLuci() {
        return folioLuci;
    }

    public void setFolioLuci(String folioLuci) {
        this.folioLuci = folioLuci;
    }

    public String getFechaCompromiso() { return fechaCompromiso; }

    public void setFechaCompromiso(String fechaCompromiso) { this.fechaCompromiso = fechaCompromiso; }

    @Override
    public String toString() {
        return "{" +
                "\"commerceData\": {"+
                "\"lastName\":\""+commerceData.getLastName()+"\", "+
                "\"commerceName\":\""+commerceData.getCommerceName()+"\", "+
                "\"name\":\""+commerceData.getName()+"\", "+
                "\"email\":\""+commerceData.getEmail()+"\""+
                "}, "+
                "\"idCorporation\":\""+getIdCorporation()+"\","+
                "\"idLuciUser\":\""+getIdLuciUser()+"\","+
                "\"folioLuci\":\""+getFolioLuci()+"\","+
                "\"fechaCompromiso\":\""+getFechaCompromiso()+"\","+
                "\"payment\": {"+
                "\"type\":\""+payment.getType()+"\""+
                "}, "+
                (null != invoice ?
                    "\"invoice\": {" +
                            "\"address\":\"" + invoice.getAddress() + "\", " +
                            "\"businessName\":\"" + invoice.getBusinessName() + "\", " +
                            "\"rfc\":\"" + invoice.getRfc() + "\", " +
                            "\"email\":\"" + invoice.getEmail() + "\"" +
                            "}, "
                : "") +
                "\"plan\": {"+
                "\"idPlan\":\""+plan.getIdPlan()+"\", "+
                "\"amount\":\""+plan.getAmount()+"\", "+
                "\"name\":\""+plan.getName()+"\""+
                "} "+
                "}";
    }
}
