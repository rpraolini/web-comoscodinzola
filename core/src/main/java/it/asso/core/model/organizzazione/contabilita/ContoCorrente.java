package it.asso.core.model.organizzazione.contabilita;

public class ContoCorrente {

	private String id_cc;
	private String id_organizzazione;
	private String num_cc;
	private String iban;
	private String ente;
	private String dt_aggiornamento;
	private String preferito;
	
	public String getId_cc() {
		return id_cc;
	}
	public void setId_cc(String id_cc) {
		this.id_cc = id_cc;
	}
	public String getId_organizzazione() {
		return id_organizzazione;
	}
	public void setId_organizzazione(String id_organizzazione) {
		this.id_organizzazione = id_organizzazione;
	}
	public String getNum_cc() {
		return num_cc;
	}
	public void setNum_cc(String num_cc) {
		this.num_cc = num_cc;
	}
	public String getIban() {
		return iban;
	}
	public void setIban(String iban) {
		this.iban = iban;
	}
	public String getEnte() {
		return ente;
	}
	public void setEnte(String ente) {
		this.ente = ente;
	}
	public String getDt_aggiornamento() {
		return dt_aggiornamento;
	}
	public void setDt_aggiornamento(String dt_aggiornamento) {
		this.dt_aggiornamento = dt_aggiornamento;
	}
	public String getPreferito() {
		return preferito;
	}
	public void setPreferito(String preferito) {
		this.preferito = preferito;
	}
	
	
	  
}
