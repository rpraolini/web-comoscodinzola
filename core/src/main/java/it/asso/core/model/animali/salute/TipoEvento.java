package it.asso.core.model.animali.salute;

public class TipoEvento {

	private String id_tipo_evento;
	private String evento;
	private String tipo;
	private String id_tipo_evento_clinico;
	private String evento_clinico;
	private String note_tipo_evento;
	

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
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getId_tipo_evento_clinico() {
		return id_tipo_evento_clinico;
	}
	public void setId_tipo_evento_clinico(String id_tipo_evento_clinico) {
		this.id_tipo_evento_clinico = id_tipo_evento_clinico;
	}
	public String getEvento_clinico() {
		return evento_clinico;
	}
	public void setEvento_clinico(String evento_clinico) {
		this.evento_clinico = evento_clinico;
	}
	public String getNote_tipo_evento() {
		return note_tipo_evento;
	}
	public void setNote_tipo_evento(String note_tipo_evento) {
		this.note_tipo_evento = note_tipo_evento;
	}
	
}
