package it.asso.core.model.magazzino;

public class SottoTipologia {

	private String idSottoTipologia;
	private String idTipologia;
	private String descrSottoTipologia;
	private String numProdotti;
	private String colore;
	private String immagine;
	private String url;
	private Tipologia tipologia;
	
	
	public String getIdTipologia() {
		return idTipologia;
	}
	public void setIdTipologia(String idTipologia) {
		this.idTipologia = idTipologia;
	}
	public String getNumProdotti() {
		return numProdotti;
	}
	public void setNumProdotti(String numProdotti) {
		this.numProdotti = numProdotti;
	}
	public String getColore() {
		return colore;
	}
	public void setColore(String colore) {
		this.colore = colore;
	}
	public String getImmagine() {
		return immagine;
	}
	public void setImmagine(String immagine) {
		this.immagine = immagine;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getIdSottoTipologia() {
		return idSottoTipologia;
	}
	public void setIdSottoTipologia(String idSottoTipologia) {
		this.idSottoTipologia = idSottoTipologia;
	}
	public String getDescrSottoTipologia() {
		return descrSottoTipologia;
	}
	public void setDescrSottoTipologia(String descrSottoTipologia) {
		this.descrSottoTipologia = descrSottoTipologia;
	}
	public Tipologia getTipologia() {
		return tipologia;
	}
	public void setTipologia(Tipologia tipologia) {
		this.tipologia = tipologia;
	}
	
}
