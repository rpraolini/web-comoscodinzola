package it.asso.core.model.magazzino;

import java.util.List;

public class Prodotto {

	private String idProdotto;
	private String idTipologia;
	private String idSottoTipologia;
	private String idMarca;
	private String prezzo;
	private String descrProdotto;
	private String immagine;
	private String account;
	private String dtAggiornamento;
	private Tipologia tipologia;
	private SottoTipologia sottoTipologia;
	private List<MagazzinoProdotto> magazzinoProdotto;
	private Marca marca;
	private String url;
	private String numPezzi;
	
	
	public String getNumPezzi() {
		return numPezzi;
	}
	public void setNumPezzi(String numPezzi) {
		this.numPezzi = numPezzi;
	}
	public String getIdProdotto() {
		return idProdotto;
	}
	public void setIdProdotto(String idProdotto) {
		this.idProdotto = idProdotto;
	}
	public String getDescrProdotto() {
		return descrProdotto;
	}
	public void setDescrProdotto(String descrProdotto) {
		this.descrProdotto = descrProdotto;
	}
	public Tipologia getTipologia() {
		return tipologia;
	}
	public void setTipologia(Tipologia tipologia) {
		this.tipologia = tipologia;
	}
	public String getIdTipologia() {
		return idTipologia;
	}
	public void setIdTipologia(String idTipologia) {
		this.idTipologia = idTipologia;
	}
	public String getIdMarca() {
		return idMarca;
	}
	public void setIdMarca(String idMarca) {
		this.idMarca = idMarca;
	}
	public String getPrezzo() {
		return prezzo;
	}
	public void setPrezzo(String prezzo) {
		this.prezzo = prezzo;
	}
	public Marca getMarca() {
		return marca;
	}
	public void setMarca(Marca marca) {
		this.marca = marca;
	}
	public String getImmagine() {
		return immagine;
	}
	public void setImmagine(String immagine) {
		this.immagine = immagine;
	}
	public List<MagazzinoProdotto> getMagazzinoProdotto() {
		return magazzinoProdotto;
	}
	public void setMagazzinoProdotto(List<MagazzinoProdotto> magazzinoProdotto) {
		this.magazzinoProdotto = magazzinoProdotto;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getDtAggiornamento() {
		return dtAggiornamento;
	}
	public void setDtAggiornamento(String dtAggiornamento) {
		this.dtAggiornamento = dtAggiornamento;
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
	public SottoTipologia getSottoTipologia() {
		return sottoTipologia;
	}
	public void setSottoTipologia(SottoTipologia sottoTipologia) {
		this.sottoTipologia = sottoTipologia;
	}
	
}
