
package com.a4sys.luciWS.test;

import java.util.List;

public class Solicitud {
	
	
	private String usuario;
	private String contrasena;
	private List<Campo> campos;
	
	
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getContrasena() {
		return contrasena;
	}
	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}
	
	public List<Campo> getCampos() {
		return campos;
	}
	public void setCampos(List<Campo> campos) {
		this.campos = campos;
	}
	
		
	
	
	
}