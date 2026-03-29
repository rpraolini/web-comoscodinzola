package it.asso.core.model.animali.salute;

import java.util.ArrayList;
import java.util.List;

import it.asso.core.model.documenti.Documento;

public class EventoClinico {

	private String id_evento;
	private String id_animale;
	private String id_tipo_evento;
	private String evento;
	private String dt_evento;
	private String dt_richiamo;
	private String note;
	private String evento_clinico;
	private String id_tipo_evento_clinico;
	private String note_tipo_evento;
	private List<Documento> documenti  = new ArrayList<Documento>();

	
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
	public String getEvento() {
		return evento;
	}
	public void setEvento(String evento) {
		this.evento = evento;
	}
	public String getDt_evento() {
		return dt_evento;
	}
	public void setDt_evento(String dt_evento) {
		this.dt_evento = dt_evento;
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
	public String getDt_richiamo() {
		return dt_richiamo;
	}
	public void setDt_richiamo(String dt_richiamo) {
		this.dt_richiamo = dt_richiamo;
	}

	public String getEvento_clinico() {
		return evento_clinico;
	}
	public void setEvento_clinico(String evento_clinico) {
		this.evento_clinico = evento_clinico;
	}
	public String getId_tipo_evento_clinico() {
		return id_tipo_evento_clinico;
	}
	public void setId_tipo_evento_clinico(String id_tipo_evento_clinico) {
		this.id_tipo_evento_clinico = id_tipo_evento_clinico;
	}
	public String getNote_tipo_evento() {
		return note_tipo_evento;
	}
	public void setNote_tipo_evento(String note_tipo_evento) {
		this.note_tipo_evento = note_tipo_evento;
	}
	public List<Documento> getDocumenti() {
		return documenti;
	}
	public void setDocumenti(List<Documento> documenti) {
		this.documenti = documenti;
	}
	
}
