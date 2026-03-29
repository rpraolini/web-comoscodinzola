package it.asso.core.model.utente;

import java.io.Serializable;

public class AreaPermesso implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String id_area;
	private String id_utente;
	private String id_ruolo;
	
	private String area;
	private String ruolo;
	
	private String read;
	private String write;
	
	
	
	public String getId_area() {
		return id_area;
	}
	public void setId_area(String id_area) {
		this.id_area = id_area;
	}
	public String getId_utente() {
		return id_utente;
	}
	public void setId_utente(String id_utente) {
		this.id_utente = id_utente;
	}
	public String getId_ruolo() {
		return id_ruolo;
	}
	public void setId_ruolo(String id_ruolo) {
		this.id_ruolo = id_ruolo;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}
	public String getRead() {
		return read;
	}
	public void setRead(String read) {
		this.read = read;
	}
	public String getWrite() {
		return write;
	}
	public void setWrite(String write) {
		this.write = write;
	}
	
	
}
