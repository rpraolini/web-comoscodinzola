package it.asso.core.model.raccolta;

import java.util.List;

import it.asso.core.model.contatto.Contatto;

public class Evento {

	private String id_evento;
	private String id_punto_raccolta;
	private Contatto contatto;
	private String dt_evento;
	private String note;
	private String dt_inserimento;
	private String account;
	private String numTurni;
	private String totale;
	
	private List<Turno> turni;
	private List<MerceEvento> merce;
	
	
	public String getNumTurni() {
		return numTurni;
	}
	
	public void setNumTurni(String numTurni) {
		this.numTurni = numTurni;
	}

	public String getId_evento() {
		return id_evento;
	}
	public void setId_evento(String id_evento) {
		this.id_evento = id_evento;
	}
	public String getId_punto_raccolta() {
		return id_punto_raccolta;
	}
	public void setId_punto_raccolta(String id_punto_raccolta) {
		this.id_punto_raccolta = id_punto_raccolta;
	}
	public String getDt_evento() {
		return dt_evento;
	}
	public void setDt_evento(String dt_evento) {
		this.dt_evento = dt_evento;
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
	public List<Turno> getTurni() {
		return turni;
	}
	public void setTurni(List<Turno> turni) {
		this.turni = turni;
	}

	public List<MerceEvento> getMerce() {
		return merce;
	}

	public void setMerce(List<MerceEvento> merce) {
		this.merce = merce;
	}

	public Contatto getContatto() {
		return contatto;
	}
	public void setContatto(Contatto contatto) {
		this.contatto = contatto;
	}

	public String getTotale() {
		return totale;
	}

	public void setTotale(String totale) {
		this.totale = totale;
	}
	
	
	
	
}
