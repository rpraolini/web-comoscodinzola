package it.asso.core.model.magazzino;

public class RicercaDTO {

	private String idMagazzino;
	private String idTipologia;
	private String limit;
	private String offset;
	
	public String getIdMagazzino() {
		return idMagazzino;
	}
	public void setIdMagazzino(String idMagazzino) {
		this.idMagazzino = idMagazzino;
	}
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public String getIdTipologia() {
		return idTipologia;
	}
	public void setIdTipologia(String idTipologia) {
		this.idTipologia = idTipologia;
	}
	
	
}
