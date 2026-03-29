package it.asso.core.model.animali.animale;

public class RicercaDTO {

	private String search;
	private String sesso;
	private String eta;
	private String vaccini;
	private String regione;
	private String provincia;
	private String stato;
	private String tag;
	private String tipoAnimale;

	// Usiamo Integer per permettere a Spring di mappare automaticamente i numeri da Angular
	private Integer limit = 0;
	private Integer offset = 10;

	// --- GETTER E SETTER ESISTENTI ---
	public String getSearch() { return search; }
	public void setSearch(String search) { this.search = search; }

	public String getSesso() { return sesso; }
	public void setSesso(String sesso) { this.sesso = sesso; }

	public String getEta() { return eta; }
	public void setEta(String eta) { this.eta = eta; }

	public String getVaccini() { return vaccini; }
	public void setVaccini(String vaccini) { this.vaccini = vaccini; }

	public String getRegione() { return regione; }
	public void setRegione(String regione) { this.regione = regione; }

	public String getProvincia() { return provincia; }
	public void setProvincia(String provincia) { this.provincia = provincia; }

	public String getStato() { return stato; }
	public void setStato(String stato) { this.stato = stato; }

	public String getTag() { return tag; }
	public void setTag(String tag) { this.tag = tag; }

	public String getTipoAnimale() { return tipoAnimale; }
	public void setTipoAnimale(String tipoAnimale) { this.tipoAnimale = tipoAnimale; }

	// --- AGGIUNGI QUESTI PER LA PAGINAZIONE ---
	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}
}