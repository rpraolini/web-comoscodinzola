package it.asso.core.model.raccolta;

public class MerceEvento extends Merce {

	private String id_evento;
	private String id_merce;
	private String quantita;
	private String pesoTot;
	private String tipo_merce;
	private String tipo_animale;
	
	public String getId_evento() {
		return id_evento;
	}
	public void setId_evento(String id_evento) {
		this.id_evento = id_evento;
	}
	public String getQuantita() {
		return quantita;
	}
	public void setQuantita(String quantita) {
		this.quantita = quantita;
	}
	public String getPesoTot() {
		return pesoTot;
	}
	public void setPesoTot(String pesoTot) {
		this.pesoTot = pesoTot;
	}
	public String getId_merce() {
		return id_merce;
	}
	public void setId_merce(String id_merce) {
		this.id_merce = id_merce;
	}
	public String getTipo_merce() {
		return tipo_merce;
	}
	public void setTipo_merce(String tipo_merce) {
		this.tipo_merce = tipo_merce;
	}
	public String getTipo_animale() {
		return tipo_animale;
	}
	public void setTipo_animale(String tipo_animale) {
		this.tipo_animale = tipo_animale;
	}
	
}
