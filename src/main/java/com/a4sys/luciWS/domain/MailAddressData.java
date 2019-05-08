package com.a4sys.luciWS.domain;

import java.util.Locale;

/***
 * POJO PARA LOS DATOS DE LA EDICION DE DOMICILIO DE CORRESPONDENCIA DESDE LA PAGINA WEB DE PROASIST
 * @author Martin Morones
 *
 */
public class MailAddressData {
     private Long idConfirmacion; 	
     private String calle; 
     private String noExterior; 
     private String noInterior; 
     private Long cPostal;
     private Long idMunicipio;	
     private String colonia; 
     private String telefonoCasa; 
     private String telefonoOficina; 
     private String entreCalle1;
     private String entreCalle2;
     private String horaInicioEntrega;
     private String horaFinEntrega;
     private String indicacionesEntrega;
     
     
	public Long getIdConfirmacion() {
		return idConfirmacion;
	}
	public void setIdConfirmacion(Long idConfirmacion) {
		this.idConfirmacion = idConfirmacion;
	}
	public String getCalle() {
		return calle.toUpperCase(new Locale("ES"));
	}
	public void setCalle(String calle) {
		this.calle = calle;
	}
	public String getNoExterior() {
		return noExterior.toUpperCase(new Locale("ES"));
	}
	public void setNoExterior(String noExterior) {
		this.noExterior = noExterior;
	}
	public String getNoInterior() {
		return noInterior.toUpperCase(new Locale("ES"));
	}
	public void setNoInterior(String noInterior) {
		this.noInterior = noInterior;
	}
	public Long getcPostal() {
		return cPostal;
	}
	public void setcPostal(Long cPostal) {
		this.cPostal = cPostal;
	}
	public Long getIdMunicipio() {
		return idMunicipio;
	}
	public void setIdMunicipio(Long idMunicipio) {
		this.idMunicipio = idMunicipio;
	}
	public String getColonia() {
		return colonia.toUpperCase(new Locale("ES"));
	}
	public void setColonia(String colonia) {
		this.colonia = colonia;
	}
	public String getTelefonoCasa() {
		return telefonoCasa.toUpperCase(new Locale("ES"));
	}
	public void setTelefonoCasa(String telefonoCasa) {
		this.telefonoCasa = telefonoCasa;
	}
	public String getTelefonoOficina() {
		return telefonoOficina.toUpperCase(new Locale("ES"));
	}
	public void setTelefonoOficina(String telefonoOficina) {
		this.telefonoOficina = telefonoOficina;
	}
	public String getEntreCalle1() {
		return entreCalle1.toUpperCase(new Locale("ES"));
	}
	public void setEntreCalle1(String entreCalle1) {
		this.entreCalle1 = entreCalle1;
	}
	public String getEntreCalle2() {
		return entreCalle2.toUpperCase(new Locale("ES"));
	}
	public void setEntreCalle2(String entreCalle2) {
		this.entreCalle2 = entreCalle2;
	}
	public String getHoraInicioEntrega() {
		return horaInicioEntrega.toUpperCase(new Locale("ES"));
	}
	public void setHoraInicioEntrega(String horaInicioEntrega) {
		this.horaInicioEntrega = horaInicioEntrega;
	}
	public String getHoraFinEntrega() {
		return horaFinEntrega.toUpperCase(new Locale("ES"));
	}
	public void setHoraFinEntrega(String horaFinEntrega) {
		this.horaFinEntrega = horaFinEntrega;
	}
	public String getIndicacionesEntrega() {
		return indicacionesEntrega.toUpperCase(new Locale("ES"));
	}
	public void setIndicacionesEntrega(String indicacionesEntrega) {
		this.indicacionesEntrega = indicacionesEntrega;
	}    
}
