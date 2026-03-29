package it.asso.core.model.animali.storia;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.asso.core.model.contatto.Contatto;

public class EventoStorico {

	private String id_evento;
	private String id_animale;
	private String id_tipo_evento;
	private String dt_da;
	private String dt_a;
	private String id_contatto;
	private String note;
	private String account;
	private String dt_aggiornamento;
	private String evento;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Contatto contatto;
	private String ct_gg;
	private String ct_mese;
	
	
	public String getId_evento() {
		return id_evento;
	}
	public void setId_evento(String id_evento) {
		this.id_evento = id_evento;
	}
	public String getId_tipo_evento() {
		return id_tipo_evento;
	}
	public void setId_tipo_evento(String id_tipo_evento) {
		this.id_tipo_evento = id_tipo_evento;
	}
	public String getId_animale() {
		return id_animale;
	}
	public void setId_animale(String id_animale) {
		this.id_animale = id_animale;
	}
	public String getDt_da() {
		return dt_da;
	}
	public void setDt_da(String dt_da) {
		this.dt_da = dt_da;
	}
	public String getDt_a() {
		return dt_a;
	}
	public void setDt_a(String dt_a) {
		this.dt_a = dt_a;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
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
	public String getId_contatto() {
		return id_contatto;
	}
	public void setId_contatto(String id_contatto) {
		this.id_contatto = id_contatto;
	}
	public Contatto getContatto() {
		return contatto;
	}
	public void setContatto(Contatto contatto) {
		this.contatto = contatto;
	}
	public String getEvento() {
		return evento;
	}
	public void setEvento(String evento) {
		this.evento = evento;
	}
	public String getCt_gg() {
		return ct_gg;
	}
	public void setCt_gg(String ct_gg) {
		this.ct_gg = ct_gg;
	}
	public String getCt_mese() {
		return ct_mese;
	}
	public void setCt_mese(String ct_mese) {
		this.ct_mese = ct_mese;
	}

}
