package it.asso.core.model.organizzazione;

public class RicercaDTO {

	private String anno;
	private String tMovimento;
	private String tDestinazione;
	
	private String limit;
	private String offset;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    private String search;
	
	
	public String getAnno() {
		return anno;
	}
	public void setAnno(String anno) {
		this.anno = anno;
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
	public String gettMovimento() {
		return tMovimento;
	}
	public void settMovimento(String tMovimento) {
		this.tMovimento = tMovimento;
	}
	public String gettDestinazione() {
		return tDestinazione;
	}
	public void settDestinazione(String tDestinazione) {
		this.tDestinazione = tDestinazione;
	}

}
