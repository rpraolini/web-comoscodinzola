package it.asso.core.model.utente;

import java.io.Serializable;

public class Ruolo implements Serializable{
 
	private static final long serialVersionUID = 1L;
	private String id_ruolo;
	private String ruolo;
	
	public String getId_ruolo() {
		return id_ruolo;
	}
	public void setId_ruolo(String id_ruolo) {
		this.id_ruolo = id_ruolo;
	}
	public String getRuolo() {
		return ruolo;
	}
	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}


}
