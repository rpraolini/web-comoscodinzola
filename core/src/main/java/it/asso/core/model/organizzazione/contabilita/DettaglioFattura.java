package it.asso.core.model.organizzazione.contabilita;

public class DettaglioFattura {
	
	private String id_fd;
	private String id_fattura;
	private String id_vf;
	private String imponibile;
	private String iva;
	private String importo;
	private String descrizione;
	
	public String getId_fd() {
		return id_fd;
	}
	public void setId_fd(String id_fd) {
		this.id_fd = id_fd;
	}
	public String getId_fattura() {
		return id_fattura;
	}
	public void setId_fattura(String id_fattura) {
		this.id_fattura = id_fattura;
	}
	public String getId_vf() {
		return id_vf;
	}
	public void setId_vf(String id_vf) {
		this.id_vf = id_vf;
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
	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

}
