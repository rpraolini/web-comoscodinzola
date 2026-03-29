package it.asso.core.model.animali.animale;

import java.util.List;

public class TipoCarattere {

	private String id_tipo_carattere;
	private String contesto;
	private String icona;
	private List<Carattere> caratteri;
	
	
	public String getId_tipo_carattere() {
		return id_tipo_carattere;
	}
	public void setId_tipo_carattere(String id_tipo_carattere) {
		this.id_tipo_carattere = id_tipo_carattere;
	}
	public String getContesto() {
		return contesto;
	}
	public void setContesto(String contesto) {
		this.contesto = contesto;
	}
	public String getIcona() {
		return icona;
	}
	public void setIcona(String icona) {
		this.icona = icona;
	}
	public List<Carattere> getCaratteri() {
		return caratteri;
	}
	public void setCaratteri(List<Carattere> caratteri) {
		this.caratteri = caratteri;
	}
	
	
}
