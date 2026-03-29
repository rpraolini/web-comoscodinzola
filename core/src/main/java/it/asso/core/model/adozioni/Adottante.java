package it.asso.core.model.adozioni;

import java.util.List;

public class Adottante {
	
	private String id_adottante;	
	private String id_contatto;
	private String cognome;
	private String nome;
	@SuppressWarnings("unused")
	private String nomeCompleto;
	private String wallet;
	private String dt_inserimento;
	private String wallet_occupato;
	private String perc_occupato;
	private String wallet_disponibile;
	private String perc_disponibile;
	private String blacklist;
	private String attivo;
	private String account;
	private List<Adottabile> adottati;
	
	public List<Adottabile> getAdottati() {
		return adottati;
	}

	public void setAdottati(List<Adottabile> adottati) {
		this.adottati = adottati;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getWallet_disponibile() {
		return wallet_disponibile;
	}

	public void setWallet_disponibile(String wallet_disponibile) {
		this.wallet_disponibile = wallet_disponibile;
	}

	public String getPerc_disponibile() {
		return perc_disponibile;
	}

	public void setPerc_disponibile(String perc_disponibile) {
		this.perc_disponibile = perc_disponibile;
	}

	public String getAttivo() {
		return attivo;
	}

	public void setAttivo(String attivo) {
		this.attivo = attivo;
	}

	public String getWallet() {
		return wallet;
	}

	public void setWallet(String wallet) {
		this.wallet = wallet;
	}

	public String getDt_inserimento() {
		return dt_inserimento;
	}

	public void setDt_inserimento(String dt_inserimento) {
		this.dt_inserimento = dt_inserimento;
	}

	public String getWallet_occupato() {
		return wallet_occupato;
	}

	public void setWallet_occupato(String wallet_occupato) {
		this.wallet_occupato = wallet_occupato;
	}

	public String getPerc_occupato() {
		return perc_occupato;
	}

	public void setPerc_occupato(String perc_occupato) {
		this.perc_occupato = perc_occupato;
	}

	public String getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(String blacklist) {
		this.blacklist = blacklist;
	}

	public String getId_adottante() {
		return id_adottante;
	}

	public void setId_adottante(String id_adottante) {
		this.id_adottante = id_adottante;
	}

	public String getNomeCompleto() {
		return cognome + " " + nome ;
	}

	public String getId_contatto() {
		return id_contatto;
	}

	public void setId_contatto(String id_contatto) {
		this.id_contatto = id_contatto;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	
}
