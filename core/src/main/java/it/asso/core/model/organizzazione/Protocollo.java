package it.asso.core.model.organizzazione;


import it.asso.core.model.documenti.Documento;


public class Protocollo {
	
	private String id_protocollo;
	private String codice;
	private String id_documento; 
	private String documento; 
	private String dt_protocollo; 
	private String oggetto; 
	private String mittente; 
	private String destinatario;
	
	private Documento doc;
	
	public String getId_protocollo() {
		return id_protocollo;
	}
	public void setId_protocollo(String id_protocollo) {
		this.id_protocollo = id_protocollo;
	}
	public String getCodice() {
		return codice;
	}
	public void setCodice(String codice) {
		this.codice = codice;
	}
	public String getId_documento() {
		return id_documento;
	}
	public void setId_documento(String id_documento) {
		this.id_documento = id_documento;
	}
	public String getDocumento() {
		return documento;
	}
	public void setDocumento(String documento) {
		this.documento = documento;
	}
	public String getDt_protocollo() {
		return dt_protocollo;
	}
	public void setDt_protocollo(String dt_protocollo) {
		this.dt_protocollo = dt_protocollo;
	}
	public String getOggetto() {
		return oggetto;
	}
	public void setOggetto(String oggetto) {
		this.oggetto = oggetto;
	}
	public String getMittente() {
		return mittente;
	}
	public void setMittente(String mittente) {
		this.mittente = mittente;
	}
	public String getDestinatario() {
		return destinatario;
	}
	public void setDestinatario(String destinatario) {
		this.destinatario = destinatario;
	}
	public Documento getDoc() {
		return doc;
	}
	public void setDoc(Documento doc) {
		this.doc = doc;
	}

	
	

}
