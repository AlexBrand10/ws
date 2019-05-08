package com.a4sys.luciWS.facturaDigital.model;

/**
 * Created by mmorones on 5/30/18.
 */
public class Cfdi {

    private String NoCertificado;
    private String UUID;
    private String FechaTimbrado;
    private String RfcProvCertif;
    private String SelloCFD;
    private String NoCertificadoSAT;
    private String SelloSAT;
    private String CadenaOrigTFD;
    private String CadenaQR;
    private String XmlBase64;
    private String PDF;

    public String getNoCertificado() {
        return NoCertificado;
    }

    public void setNoCertificado(String noCertificado) {
        NoCertificado = noCertificado;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getFechaTimbrado() {
        return FechaTimbrado;
    }

    public void setFechaTimbrado(String fechaTrimbado) {
        FechaTimbrado = fechaTrimbado;
    }

    public String getRfcProvCertif() {
        return RfcProvCertif;
    }

    public void setRfcProvCertif(String rfcProvCertif) {
        RfcProvCertif = rfcProvCertif;
    }

    public String getSelloCFD() {
        return SelloCFD;
    }

    public void setSelloCFD(String selloCFD) {
        SelloCFD = selloCFD;
    }

    public String getNoCertificadoSAT() {
        return NoCertificadoSAT;
    }

    public void setNoCertificadoSAT(String noCertificadoSAT) {
        NoCertificadoSAT = noCertificadoSAT;
    }

    public String getSelloSAT() {
        return SelloSAT;
    }

    public void setSelloSAT(String selloSAT) {
        SelloSAT = selloSAT;
    }

    public String getCadenaOrigTFD() {
        return CadenaOrigTFD;
    }

    public void setCadenaOrigTFD(String cadenaOrigTFD) {
        CadenaOrigTFD = cadenaOrigTFD;
    }

    public String getCadenaQR() {
        return CadenaQR;
    }

    public void setCadenaQR(String cadenaQR) {
        CadenaQR = cadenaQR;
    }

    public String getXmlBase64() {
        return XmlBase64;
    }

    public void setXmlBase64(String xmlBase64) {
        XmlBase64 = xmlBase64;
    }

    public String getPDF() {
        return PDF;
    }

    public void setPDF(String PDF) {
        this.PDF = PDF;
    }
}
