package it.asso.core.model.organizzazione;

import it.asso.core.model.organizzazione.contabilita.ContoCorrente;

import java.io.Serializable;


public class Organizzazione implements Serializable{

	private static final long serialVersionUID = -395825972871839512L;
	
	private String id_organizzazione;
	private String id_tipo_organizzazione;
	private String sigla_tipo_organizzazione;
	private String rag_sociale;
	private String indirizzo;
	private String cf;
	private String telefono;
	private String sigla;
	private String telefono_1;
	private String email;
	private String url;
	private String tenant;
	private String iscrizione;
	private ContoCorrente cc;
	
	public String getId_organizzazione() {
		return id_organizzazione;
	}
	public void setId_organizzazione(String id_organizzazione) {
		this.id_organizzazione = id_organizzazione;
	}
	public String getRag_sociale() {
		return rag_sociale;
	}
	public void setRag_sociale(String rag_sociale) {
		this.rag_sociale = rag_sociale;
	}
	public String getIndirizzo() {
		return indirizzo;
	}
	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}
	public String getCf() {
		return cf;
	}
	public void setCf(String cf) {
		this.cf = cf;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getSigla() {
		return sigla;
	}
	public void setSigla(String sigla) {
		this.sigla = sigla;
	}
	public String getTelefono_1() {
		return telefono_1;
	}
	public void setTelefono_1(String telefono_1) {
		this.telefono_1 = telefono_1;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	public String getIscrizione() {
		return iscrizione;
	}
	public void setIscrizione(String iscrizione) {
		this.iscrizione = iscrizione;
	}
	public ContoCorrente getCc() {
		return cc;
	}
	public void setCc(ContoCorrente cc) {
		this.cc = cc;
	}
	public String getId_tipo_organizzazione() {
		return id_tipo_organizzazione;
	}
	public void setId_tipo_organizzazione(String id_tipo_organizzazione) {
		this.id_tipo_organizzazione = id_tipo_organizzazione;
	}
	public String getSigla_tipo_organizzazione() {
		return sigla_tipo_organizzazione;
	}
	public void setSigla_tipo_organizzazione(String sigla_tipo_organizzazione) {
		this.sigla_tipo_organizzazione = sigla_tipo_organizzazione;
	}
	
	
}
