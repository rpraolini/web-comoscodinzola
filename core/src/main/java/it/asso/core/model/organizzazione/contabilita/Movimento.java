package it.asso.core.model.organizzazione.contabilita;

import java.util.ArrayList;
import java.util.List;

import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.documenti.Documento;

public class Movimento{
	
	private String id_movimento;
	private String id_tipo_movimento;
	private String id_organizzazione;
	private String codice;
	private String dt_operazione;
	private String importo;
	private String account;
	private String dt_inserimento;
	private String dt_aggiornamento;
	
	private String id_destinazione;
	private String id_tipo_destinazione;
	
	private String id_contatto;
	
	private Destinazione destinazione;
	private Destinazione destinazioneA;
	
	private String id_cc;
	
	private String id_cc_a;
	
	private String id_causale;
	private String note;
	
	private Contatto destinatario;
	
	private TipoMovimento tipoMovimento;
	private Rendiconto rendiconto;
	
	private List<Fattura> fatture;
	
	private List<DettaglioMovimento> dettaglioMovimento;
	
	private List<Documento> documenti = new ArrayList<Documento>();
	
	private int girofondo;
	
	
	public int getGirofondo() {
		return girofondo;
	}
	public void setGirofondo(int girofondo) {
		this.girofondo = girofondo;
	}
	public String getId_movimento() {
		return id_movimento;
	}
	public void setId_movimento(String id_movimento) {
		this.id_movimento = id_movimento;
	}
	public String getId_tipo_movimento() {
		return id_tipo_movimento;
	}
	public void setId_tipo_movimento(String id_tipo_movimento) {
		this.id_tipo_movimento = id_tipo_movimento;
	}
	public String getId_organizzazione() {
		return id_organizzazione;
	}
	public void setId_organizzazione(String id_organizzazione) {
		this.id_organizzazione = id_organizzazione;
	}
	public String getCodice() {
		return codice;
	}
	public void setCodice(String codice) {
		this.codice = codice;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getDt_inserimento() {
		return dt_inserimento;
	}
	public void setDt_inserimento(String dt_inserimento) {
		this.dt_inserimento = dt_inserimento;
	}
	public String getDt_aggiornamento() {
		return dt_aggiornamento;
	}
	public void setDt_aggiornamento(String dt_aggiornamento) {
		this.dt_aggiornamento = dt_aggiornamento;
	}
	public TipoMovimento getTipoMovimento() {
		return tipoMovimento;
	}
	public void setTipoMovimento(TipoMovimento tipoMovimento) {
		this.tipoMovimento = tipoMovimento;
	}
	public String getDt_operazione() {
		return dt_operazione;
	}
	public void setDt_operazione(String dt_operazione) {
		this.dt_operazione = dt_operazione;
	}
	public String getImporto() {
		return importo;
	}
	public void setImporto(String importo) {
		this.importo = importo;
	}
	public String getId_causale() {
		return id_causale;
	}
	public void setId_causale(String id_causale) {
		this.id_causale = id_causale;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	public Rendiconto getRendiconto() {
		return rendiconto;
	}
	public void setRendiconto(Rendiconto rendiconto) {
		this.rendiconto = rendiconto;
	}
	
	public Destinazione getDestinazione() {
		return destinazione;
	}
	public void setDestinazione(Destinazione destinazione) {
		this.destinazione = destinazione;
	}
	public Contatto getDestinatario() {
		return destinatario;
	}
	public void setDestinatario(Contatto destinatario) {
		this.destinatario = destinatario;
	}
	public String getId_cc() {
		return id_cc;
	}
	public void setId_cc(String id_cc) {
		this.id_cc = id_cc;
	}
	public List<Fattura> getFatture() {
		return fatture;
	}
	public void setFatture(List<Fattura> fatture) {
		this.fatture = fatture;
	}
	public List<DettaglioMovimento> getDettaglioMovimento() {
		return dettaglioMovimento;
	}
	public void setDettaglioMovimento(List<DettaglioMovimento> dettaglioMovimento) {
		this.dettaglioMovimento = dettaglioMovimento;
	}
	public String getId_destinazione() {
		return id_destinazione;
	}
	public void setId_destinazione(String id_destinazione) {
		this.id_destinazione = id_destinazione;
	}
	public String getId_tipo_destinazione() {
		return id_tipo_destinazione;
	}
	public void setId_tipo_destinazione(String id_tipo_destinazione) {
		this.id_tipo_destinazione = id_tipo_destinazione;
	}
	public String getId_contatto() {
		return id_contatto;
	}
	public void setId_contatto(String id_contatto) {
		this.id_contatto = id_contatto;
	}
	public List<Documento> getDocumenti() {
		return documenti;
	}
	public void setDocumenti(List<Documento> documenti) {
		this.documenti = documenti;
	}
	public Destinazione getDestinazioneA() {
		return destinazioneA;
	}
	public void setDestinazioneA(Destinazione destinazioneA) {
		this.destinazioneA = destinazioneA;
	}
	public String getId_cc_a() {
		return id_cc_a;
	}
	public void setId_cc_a(String id_cc_a) {
		this.id_cc_a = id_cc_a;
	}
	
	
}
