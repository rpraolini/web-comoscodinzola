package it.asso.core.model.animali.gestione;

import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.documenti.Documento;

import java.util.ArrayList;
import java.util.List;


public class Iter {

	private String id_iter;
	private String id_tipo_iter;
	private String nome;
	private String localita;
	private String note;
	private String id_animale;
	private String tipo_iter;
	private String quest_f;
	private String quest_invio;
	private String quest_ritorno;
	private String quest_key;
	private String account;
	private String dt_aggiornamento;
	private String email;
	private String telefono;
	private String id_contatto;
	private String id_contatto_vol;
	private String id_contatto_adottante;
	private String id_contatto_proprietario;
	private String esito;
	private Contatto contatto;
	private Contatto volontaria;
	private Contatto adottante;
	private Contatto proprietario;
	private String dt_colloquio;
	private List<Documento> documenti = new ArrayList<Documento>();
	private String contributo;
	private String id_pratica;
	private String stato;
	private String dt_consegna;
	private String colore;
	
	
	public Contatto getProprietario() {
		return proprietario;
	}
	public void setProprietario(Contatto proprietario) {
		this.proprietario = proprietario;
	}
	public String getId_contatto_proprietario() {
		return id_contatto_proprietario;
	}
	public void setId_contatto_proprietario(String id_contatto_proprietario) {
		this.id_contatto_proprietario = id_contatto_proprietario;
	}
	public Contatto getContatto() {
		return contatto;
	}
	public void setContatto(Contatto contatto) {
		this.contatto = contatto;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getId_animale() {
		return id_animale;
	}
	public void setId_animale(String id_animale) {
		this.id_animale = id_animale;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getDt_aggiornamento() {
		return dt_aggiornamento;
	}
	public void setDt_aggiornamento(String dt_aggiornamento) {
		this.dt_aggiornamento = dt_aggiornamento;
	}
	public String getLocalita() {
		return localita;
	}
	public void setLocalita(String localita) {
		this.localita = localita;
	}
	public String getQuest_f() {
		return quest_f;
	}
	public void setQuest_f(String quest_f) {
		this.quest_f = quest_f;
	}
	public String getQuest_invio() {
		return quest_invio;
	}
	public void setQuest_invio(String quest_invio) {
		this.quest_invio = quest_invio;
	}
	public String getQuest_key() {
		return quest_key;
	}
	public void setQuest_key(String quest_key) {
		this.quest_key = quest_key;
	}
	public String getQuest_ritorno() {
		return quest_ritorno;
	}
	public void setQuest_ritorno(String quest_ritorno) {
		this.quest_ritorno = quest_ritorno;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getId_contatto() {
		return id_contatto;
	}
	public void setId_contatto(String id_contatto) {
		this.id_contatto = id_contatto;
	}
	public String getEsito() {
		return esito;
	}
	public void setEsito(String esito) {
		this.esito = esito;
	}
	public String getId_contatto_vol() {
		return id_contatto_vol;
	}
	public void setId_contatto_vol(String id_contatto_vol) {
		this.id_contatto_vol = id_contatto_vol;
	}
	public Contatto getVolontaria() {
		return volontaria;
	}
	public void setVolontaria(Contatto volontaria) {
		this.volontaria = volontaria;
	}
	public String getId_iter() {
		return id_iter;
	}
	public void setId_iter(String id_iter) {
		this.id_iter = id_iter;
	}
	public String getId_tipo_iter() {
		return id_tipo_iter;
	}
	public void setId_tipo_iter(String id_tipo_iter) {
		this.id_tipo_iter = id_tipo_iter;
	}
	public String getTipo_iter() {
		return tipo_iter;
	}
	public void setTipo_iter(String tipo_iter) {
		this.tipo_iter = tipo_iter;
	}
	public String getDt_colloquio() {
		return dt_colloquio;
	}
	public void setDt_colloquio(String dt_colloquio) {
		this.dt_colloquio = dt_colloquio;
	}
	public List<Documento> getDocumenti() {
		return documenti;
	}
	public void setDocumenti(List<Documento> documenti) {
		this.documenti = documenti;
	}
	public String getId_contatto_adottante() {
		return id_contatto_adottante;
	}
	public void setId_contatto_adottante(String id_contatto_adottante) {
		this.id_contatto_adottante = id_contatto_adottante;
	}
	public Contatto getAdottante() {
		return adottante;
	}
	public void setAdottante(Contatto adottante) {
		this.adottante = adottante;
	}
	public String getContributo() {
		return contributo;
	}
	public void setContributo(String contributo) {
		this.contributo = contributo;
	}
	public String getId_pratica() {
		return id_pratica;
	}
	public void setId_pratica(String id_pratica) {
		this.id_pratica = id_pratica;
	}
	public String getStato() {
		return stato;
	}
	public void setStato(String stato) {
		this.stato = stato;
	}
	public String getDt_consegna() {
		return dt_consegna;
	}
	public void setDt_consegna(String dt_consegna) {
		this.dt_consegna = dt_consegna;
	}
	public String getColore() {
		return colore;
	}
	public void setColore(String colore) {
		this.colore = colore;
	}
	
	
}
