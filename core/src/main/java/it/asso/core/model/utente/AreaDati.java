package it.asso.core.model.utente;

import java.io.Serializable;

public class AreaDati implements Serializable{


	private static final long serialVersionUID = 1L;
	private String id_area;
	private String area;
	
	
	public String getId_area() {
		return id_area;
	}
	public void setId_area(String id_area) {
		this.id_area = id_area;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	
	
}
