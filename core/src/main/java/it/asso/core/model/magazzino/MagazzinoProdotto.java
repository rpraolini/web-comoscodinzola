package it.asso.core.model.magazzino;

public class MagazzinoProdotto {

	private String id;
	private String idProdotto;
	private String idMagazzino;
	private String idTaglia;
	private String idColore;
	private String quantita;
	
	private Taglia taglia;
	private Magazzino magazzino;
	private Colore colore;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIdProdotto() {
		return idProdotto;
	}
	public void setIdProdotto(String idProdotto) {
		this.idProdotto = idProdotto;
	}
	public String getIdMagazzino() {
		return idMagazzino;
	}
	public void setIdMagazzino(String idMagazzino) {
		this.idMagazzino = idMagazzino;
	}
	public String getIdTaglia() {
		return idTaglia;
	}
	public void setIdTaglia(String idTaglia) {
		this.idTaglia = idTaglia;
	}
	public String getIdColore() {
		return idColore;
	}
	public void setIdColore(String idColore) {
		this.idColore = idColore;
	}
	public String getQuantita() {
		return quantita;
	}
	public void setQuantita(String quantita) {
		this.quantita = quantita;
	}
	public Taglia getTaglia() {
		return taglia;
	}
	public void setTaglia(Taglia taglia) {
		this.taglia = taglia;
	}
	public Magazzino getMagazzino() {
		return magazzino;
	}
	public void setMagazzino(Magazzino magazzino) {
		this.magazzino = magazzino;
	}
	public Colore getColore() {
		return colore;
	}
	public void setColore(Colore colore) {
		this.colore = colore;
	}
	
	
}
