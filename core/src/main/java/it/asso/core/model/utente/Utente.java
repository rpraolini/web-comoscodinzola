package it.asso.core.model.utente;

import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.organizzazione.Organizzazione;

import java.io.Serializable;
import java.util.List;


public class Utente extends Contatto implements Serializable{

	private static final long serialVersionUID = 1L;
	private String id_utente;
	private String account;
	private String pwd;
	private String abilitato;
	
	private List<Ruolo> ruoli;
	private List<AreaPermesso> permessi;
	private Organizzazione organizzazione;
	
	private String dt_inserimento;
	private String dt_aggiornamento;
	private String dt_scadenza;
	
	
	
	public String getAbilitato() {
		return abilitato;
	}
	public void setAbilitato(String abilitato) {
		this.abilitato = abilitato;
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
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public List<Ruolo> getRuoli() {
		return ruoli;
	}
	public void setRuoli(List<Ruolo> ruoli) {
		this.ruoli = ruoli;
	}
	public String getDt_inserimento() {
		return dt_inserimento;
	}
	public void setDt_inserimento(String dt_inserimento) {
		this.dt_inserimento = dt_inserimento;
	}
	public String getDt_aggiornamento() {
		return dt_aggiornamento;
	}
	public void setDt_aggiornamento(String dt_aggiornamento) {
		this.dt_aggiornamento = dt_aggiornamento;
	}
	public List<AreaPermesso> getPermessi() {
		return permessi;
	}
	public void setPermessi(List<AreaPermesso> permessi) {
		this.permessi = permessi;
	}
	public String getDt_scadenza() {
		return dt_scadenza;
	}
	public void setDt_scadenza(String dt_scadenza) {
		this.dt_scadenza = dt_scadenza;
	}
	public Organizzazione getOrganizzazione() {
		return organizzazione;
	}
	public void setOrganizzazione(Organizzazione organizzazione) {
		this.organizzazione = organizzazione;
	}
	
}
