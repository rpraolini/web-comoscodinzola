package it.asso.core.model.organizzazione.contabilita;

public class DettaglioMovimento {
	
	private String id_movimento_dettaglio;
	private String id_movimento;
	private String id_cr_sottovoce;
	private String importo;
	private Rendiconto rendiconto;
	
	public String getId_movimento_dettaglio() {
		return id_movimento_dettaglio;
	}
	public void setId_movimento_dettaglio(String id_movimento_dettaglio) {
		this.id_movimento_dettaglio = id_movimento_dettaglio;
	}
	public String getId_movimento() {
		return id_movimento;
	}
	public void setId_movimento(String id_movimento) {
		this.id_movimento = id_movimento;
	}
	public String getId_cr_sottovoce() {
		return id_cr_sottovoce;
	}
	public void setId_cr_sottovoce(String id_cr_sottovoce) {
		this.id_cr_sottovoce = id_cr_sottovoce;
	}
	public String getImporto() {
		return importo;
	}
	public void setImporto(String importo) {
		this.importo = importo;
	}
	public Rendiconto getRendiconto() {
		return rendiconto;
	}
	public void setRendiconto(Rendiconto rendiconto) {
		this.rendiconto = rendiconto;
	}

	
}
