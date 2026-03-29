package it.asso.core.model.organizzazione.contabilita;

public class MovimentoRestrict extends Movimento{

	private String tMovimento;
	private String tDestinazione;
	private String tDestinatario;
	private String causale;
	
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
	public String getCausale() {
		return causale;
	}
	public void setCausale(String causale) {
		this.causale = causale;
	}
	public String gettDestinatario() {
		return tDestinatario;
	}
	public void settDestinatario(String tDestinatario) {
		this.tDestinatario = tDestinatario;
	}
	
	
	
	
}
