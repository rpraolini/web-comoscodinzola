package it.asso.core.model.raccolta;

import java.util.List;

import it.asso.core.model.contatto.Contatto;

public class Turno {

	private String id_turno;
	private String id_evento;
	private String orario_da;
	private String orario_a;
	private String ordine;
	private String note;
	
	private List<Contatto> contatti;
	
	public String getId_turno() {
		return id_turno;
	}
	public void setId_turno(String id_turno) {
		this.id_turno = id_turno;
	}
	public String getId_evento() {
		return id_evento;
	}
	public void setId_evento(String id_evento) {
		this.id_evento = id_evento;
	}
	public String getOrario_da() {
		return orario_da;
	}
	public void setOrario_da(String orario_da) {
		this.orario_da = orario_da;
	}
	public String getOrario_a() {
		return orario_a;
	}
	public void setOrario_a(String orario_a) {
		this.orario_a = orario_a;
	}
	public String getOrdine() {
		return ordine;
	}
	public void setOrdine(String ordine) {
		this.ordine = ordine;
	}
	public List<Contatto> getContatti() {
		return contatti;
	}
	public void setContatti(List<Contatto> contatti) {
		this.contatti = contatti;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	
}
