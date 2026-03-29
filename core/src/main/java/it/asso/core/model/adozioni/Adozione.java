package it.asso.core.model.adozioni;

import java.util.List;

public class Adozione {

	private String id_adozione;
	
	private String id_adottabile;
	private String id_adottante;
	private String dt_inserimento;
	private String account;
	private String quota;
	private String TotQuota;
	private String note;
	private String attivo;
	
	private List<Adottante> adottanti;
	private List<Adottabile> adottabili;
	
	
	public String getAttivo() {
		return attivo;
	}
	public void setAttivo(String attivo) {
		this.attivo = attivo;
	}
	public String getTotQuota() {
		return TotQuota;
	}
	public void setTotQuota(String totQuota) {
		TotQuota = totQuota;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getId_adozione() {
		return id_adozione;
	}
	public void setId_adozione(String id_adozione) {
		this.id_adozione = id_adozione;
	}
	public String getId_adottabile() {
		return id_adottabile;
	}
	public void setId_adottabile(String id_adottabile) {
		this.id_adottabile = id_adottabile;
	}
	public String getId_adottante() {
		return id_adottante;
	}
	public void setId_adottante(String id_adottante) {
		this.id_adottante = id_adottante;
	}
	public String getDt_inserimento() {
		return dt_inserimento;
	}
	public void setDt_inserimento(String dt_inserimento) {
		this.dt_inserimento = dt_inserimento;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getQuota() {
		return quota;
	}
	public void setQuota(String quota) {
		this.quota = quota;
	}
	public List<Adottante> getAdottanti() {
		return adottanti;
	}
	public void setAdottanti(List<Adottante> adottanti) {
		this.adottanti = adottanti;
	}
	public List<Adottabile> getAdottabili() {
		return adottabili;
	}
	public void setAdottabili(List<Adottabile> adottabili) {
		this.adottabili = adottabili;
	}
	
	
	
	
}
