package it.asso.core.model.adozioni;

import java.util.List;

public class Adottabile {
	
	private String id_adottabile;
	private String id_animale;
	private String note;
	private String dt_inserimento;
	private String account;
	private String costo;
	private String attivo;
	private String cod_animale;
	private String nome;
	private Adozione adozione;
	private String totQuota;
	private List<Adottante> adottanti;
	
	
	public List<Adottante> getAdottanti() {
		return adottanti;
	}
	public void setAdottanti(List<Adottante> adottanti) {
		this.adottanti = adottanti;
	}
	public String getTotQuota() {
		return totQuota;
	}
	public void setTotQuota(String totQuota) {
		this.totQuota = totQuota;
	}
	public Adozione getAdozione() {
		return adozione;
	}
	public void setAdozione(Adozione adozione) {
		this.adozione = adozione;
	}
	public String getId_adottabile() {
		return id_adottabile;
	}
	public void setId_adottabile(String id_adottabile) {
		this.id_adottabile = id_adottabile;
	}
	public String getId_animale() {
		return id_animale;
	}
	public void setId_animale(String id_animale) {
		this.id_animale = id_animale;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
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
	public String getCosto() {
		return costo;
	}
	public void setCosto(String costo) {
		this.costo = costo;
	}
	public String getAttivo() {
		return attivo;
	}
	public void setAttivo(String attivo) {
		this.attivo = attivo;
	}
	public String getCod_animale() {
		return cod_animale;
	}
	public void setCod_animale(String cod_animale) {
		this.cod_animale = cod_animale;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	


}
