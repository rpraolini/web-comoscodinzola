package it.asso.core.model.gestione;

public class Attivita {

	private String id_r_attivita_p; 
	private String id_attivita_p; 
	private String id_pratica; 
	private String dt_attivita; 
	private String note_attivita; 
	private String id_utente; 
	private String account; 
	private String id_stato_padre;  
	private String id_stato_precedente;
	
	
	public String getId_r_attivita_p() {
		return id_r_attivita_p;
	}
	public void setId_r_attivita_p(String id_r_attivita_p) {
		this.id_r_attivita_p = id_r_attivita_p;
	}
	public String getId_attivita_p() {
		return id_attivita_p;
	}
	public void setId_attivita_p(String id_attivita_p) {
		this.id_attivita_p = id_attivita_p;
	}
	public String getId_pratica() {
		return id_pratica;
	}
	public void setId_pratica(String id_pratica) {
		this.id_pratica = id_pratica;
	}
	public String getDt_attivita() {
		return dt_attivita;
	}
	public void setDt_attivita(String dt_attivita) {
		this.dt_attivita = dt_attivita;
	}
	public String getNote_attivita() {
		return note_attivita;
	}
	public void setNote_attivita(String note_attivita) {
		this.note_attivita = note_attivita;
	}
	public String getId_utente() {
		return id_utente;
	}
	public void setId_utente(String id_utente) {
		this.id_utente = id_utente;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getId_stato_padre() {
		return id_stato_padre;
	}
	public void setId_stato_padre(String id_stato_padre) {
		this.id_stato_padre = id_stato_padre;
	}
	public String getId_stato_precedente() {
		return id_stato_precedente;
	}
	public void setId_stato_precedente(String id_stato_precedente) {
		this.id_stato_precedente = id_stato_precedente;
	}
	
	
}