package it.asso.core.model.notifiche;

public class Notifica {
	
	private String id_notifica;
	private String titolo_notifica;
	private String descr_notifica;
	private String titolo_testo;
	private String descr_testo;
	private String attiva;
	private String dt_notifica;
	private String id_evento;
	private String evento;
	
	public String getId_notifica() {
		return id_notifica;
	}
	public void setId_notifica(String id_notifica) {
		this.id_notifica = id_notifica;
	}
	public String getDescr_notifica() {
		return descr_notifica;
	}
	public void setDescr_notifica(String descr_notifica) {
		this.descr_notifica = descr_notifica;
	}
	public String getAttiva() {
		return attiva;
	}
	public void setAttiva(String attiva) {
		this.attiva = attiva;
	}
	public String getDt_notifica() {
		return dt_notifica;
	}
	public void setDt_notifica(String dt_notifica) {
		this.dt_notifica = dt_notifica;
	}
	public String getTitolo_notifica() {
		return titolo_notifica;
	}
	public void setTitolo_notifica(String titolo_notifica) {
		this.titolo_notifica = titolo_notifica;
	}
	public String getTitolo_testo() {
		return titolo_testo;
	}
	public void setTitolo_testo(String titolo_testo) {
		this.titolo_testo = titolo_testo;
	}
	public String getDescr_testo() {
		return descr_testo;
	}
	public void setDescr_testo(String descr_testo) {
		this.descr_testo = descr_testo;
	}
	public String getId_evento() {
		return id_evento;
	}
	public void setId_evento(String id_evento) {
		this.id_evento = id_evento;
	}
	public String getEvento() {
		return evento;
	}
	public void setEvento(String evento) {
		this.evento = evento;
	}
	
	

}
