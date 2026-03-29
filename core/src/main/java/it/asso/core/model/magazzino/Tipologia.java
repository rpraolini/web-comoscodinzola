package it.asso.core.model.magazzino;

import java.util.List;

public class Tipologia {

	private String idTipologia;
	private String descrTipologia;
	private String numProdotti;
	private String colore;
	private String immagine;
	private String url;
	private List<SottoTipologia> sottoTipologie;
	
	
	public String getIdTipologia() {
		return idTipologia;
	}
	public void setIdTipologia(String idTipologia) {
		this.idTipologia = idTipologia;
	}
	public String getDescrTipologia() {
		return descrTipologia;
	}
	public void setDescrTipologia(String descrTipologia) {
		this.descrTipologia = descrTipologia;
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
	public List<SottoTipologia> getSottoTipologie() {
		return sottoTipologie;
	}
	public void setSottoTipologie(List<SottoTipologia> sottoTipologie) {
		this.sottoTipologie = sottoTipologie;
	}
	
}
