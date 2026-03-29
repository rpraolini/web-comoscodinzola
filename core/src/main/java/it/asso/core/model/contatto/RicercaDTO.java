package it.asso.core.model.contatto;

public class RicercaDTO {

	private String search;
	private String regione;
	private String provincia;
	private String idTipoContatto;
	private String idQualifica;
	
	private String limit;
	private String offset;
	
	
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
	public String getRegione() {
		return regione;
	}
	public void setRegione(String regione) {
		this.regione = regione;
	}
	public String getProvincia() {
		return provincia;
	}
	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}
	public String getIdTipoContatto() {
		return idTipoContatto;
	}
	public void setIdTipoContatto(String idTipoContatto) {
		this.idTipoContatto = idTipoContatto;
	}
	public String getIdQualifica() {
		return idQualifica;
	}
	public void setIdQualifica(String idQualifica) {
		this.idQualifica = idQualifica;
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
	
	
}
