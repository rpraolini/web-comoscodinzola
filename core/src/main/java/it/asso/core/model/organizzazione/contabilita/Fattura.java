package it.asso.core.model.organizzazione.contabilita;

import java.util.ArrayList;
import java.util.List;

import it.asso.core.model.documenti.Documento;

public class Fattura {

	private String id_fattura;
	private String tipo_fattura;
	private String codice;
	private String numero;
	private String id_contatto;
	private String note;
	private String dt_emissione;
	private String dt_scadenza;
	private String imponibile;
	private String iva;
	private String importo;
	private String account;
	private String dt_aggiornamento;
	private String dt_inserimento;
	private List<DettaglioFattura> dettaglio = new ArrayList<DettaglioFattura>();
	private String contatto;
	private List<Documento> documenti = new ArrayList<Documento>();
	private String daPagare;
	private String ritAccFattura;
	private String id_organizzazione;
	
	public String getRitAccFattura() {
		return ritAccFattura;
	}
	public void setRitAccFattura(String ritAccFattura) {
		this.ritAccFattura = ritAccFattura;
	}
	public List<DettaglioFattura> getDettaglio() {
		return dettaglio;
	}
	public void setDettaglio(List<DettaglioFattura> dettaglio) {
		this.dettaglio = dettaglio;
	}
	public String getId_fattura() {
		return id_fattura;
	}
	public void setId_fattura(String id_fattura) {
		this.id_fattura = id_fattura;
	}
	public String getTipo_fattura() {
		return tipo_fattura;
	}
	public void setTipo_fattura(String tipo_fattura) {
		this.tipo_fattura = tipo_fattura;
	}
	public String getCodice() {
		return codice;
	}
	public void setCodice(String codice) {
		this.codice = codice;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}
	public String getId_contatto() {
		return id_contatto;
	}
	public void setId_contatto(String id_contatto) {
		this.id_contatto = id_contatto;
	}
	public String getDt_emissione() {
		return dt_emissione;
	}
	public void setDt_emissione(String dt_emissione) {
		this.dt_emissione = dt_emissione;
	}
	public String getDt_scadenza() {
		return dt_scadenza;
	}
	public void setDt_scadenza(String dt_scadenza) {
		this.dt_scadenza = dt_scadenza;
	}
	public String getImponibile() {
		return imponibile;
	}
	public void setImponibile(String imponibile) {
		this.imponibile = imponibile;
	}
	public String getIva() {
		return iva;
	}
	public void setIva(String iva) {
		this.iva = iva;
	}
	public String getImporto() {
		return importo;
	}
	public void setImporto(String importo) {
		this.importo = importo;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getDt_aggiornamento() {
		return dt_aggiornamento;
	}
	public void setDt_aggiornamento(String dt_aggiornamento) {
		this.dt_aggiornamento = dt_aggiornamento;
	}
	public String getDt_inserimento() {
		return dt_inserimento;
	}
	public void setDt_inserimento(String dt_inserimento) {
		this.dt_inserimento = dt_inserimento;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getContatto() {
		return contatto;
	}
	public void setContatto(String contatto) {
		this.contatto = contatto;
	}
	public List<Documento> getDocumenti() {
		return documenti;
	}
	public void setDocumenti(List<Documento> documenti) {
		this.documenti = documenti;
	}
	public String getDaPagare() {
		return daPagare;
	}
	public void setDaPagare(String daPagare) {
		this.daPagare = daPagare;
	}
	public String getId_organizzazione() {
		return id_organizzazione;
	}
	public void setId_organizzazione(String id_organizzazione) {
		this.id_organizzazione = id_organizzazione;
	}
	

}
