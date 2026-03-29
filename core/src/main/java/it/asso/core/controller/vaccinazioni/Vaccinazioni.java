package it.asso.core.controller.vaccinazioni;

public class Vaccinazioni {

	private String id_animale;
	private String nome;
	private String evento;
	private String da_inviare_7;
	private String da_inviare_15;
	private String dt_richiamo;
	private String dt_evento;
	
	public String getId_animale() {
		return id_animale;
	}
	public void setId_animale(String id_animale) {
		this.id_animale = id_animale;
	}
	public String getEvento() {
		return evento;
	}
	public void setEvento(String evento) {
		this.evento = evento;
	}
	public String getDa_inviare_7() {
		return da_inviare_7;
	}
	public void setDa_inviare_7(String da_inviare_7) {
		this.da_inviare_7 = da_inviare_7;
	}
	public String getDa_inviare_15() {
		return da_inviare_15;
	}
	public void setDa_inviare_15(String da_inviare_15) {
		this.da_inviare_15 = da_inviare_15;
	}
	public String getDt_richiamo() {
		return dt_richiamo;
	}
	public void setDt_richiamo(String dt_richiamo) {
		this.dt_richiamo = dt_richiamo;
	}
	public String getDt_evento() {
		return dt_evento;
	}
	public void setDt_evento(String dt_evento) {
		this.dt_evento = dt_evento;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}


	
}
