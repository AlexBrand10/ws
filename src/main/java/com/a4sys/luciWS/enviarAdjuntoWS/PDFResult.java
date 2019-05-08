package com.a4sys.luciWS.enviarAdjuntoWS;

public class PDFResult {
	
	private String url = null;
	private String message = "";
	private String nombreReporte = "";
	private String rutaAdjuntoReporte = "";
	private String nombreArchivos = "";
	private String idSftp;
	private String idExternoSolitud;
	private String idAdjunto;
	private String tipo;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getNombreReporte() {
		return nombreReporte;
	}
	public void setNombreReporte(String nombreReporte) {
		this.nombreReporte = nombreReporte;
	}
	public String getRutaAdjuntoReporte() {
		return rutaAdjuntoReporte;
	}
	public void setRutaAdjuntoReporte(String rutaAdjuntoReporte) {
		this.rutaAdjuntoReporte = rutaAdjuntoReporte;
	}
	public String getNombreArchivos() {
		return nombreArchivos;
	}
	public void setNombreArchivos(String nombreArchivos) {
		this.nombreArchivos = nombreArchivos;
	}
	public String getIdSftp() {
		return idSftp;
	}
	public void setIdSftp(String idSftp) {
		this.idSftp = idSftp;
	}
	public String getIdExternoSolitud() {
		return idExternoSolitud;
	}
	public void setIdExternoSolitud(String idExternoSolitud) {
		this.idExternoSolitud = idExternoSolitud;
	}
	public String getIdAdjunto() {
		return idAdjunto;
	}
	public void setIdAdjunto(String idAdjunto) {
		this.idAdjunto = idAdjunto;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}