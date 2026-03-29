package it.asso.core.model.organizzazione.contabilita;

import java.util.List;

public class Destinazione {

	private String id_destinazione;
	private String descrizione;
	private List<TipoDestinazione> tipi;
	private TipoDestinazione tipoDestinazione;
	

	public String getId_destinazione() {
		return id_destinazione;
	}
	public void setId_destinazione(String id_destinazione) {
		this.id_destinazione = id_destinazione;
	}
	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	public List<TipoDestinazione> getTipi() {
		return tipi;
	}
	public void setTipi(List<TipoDestinazione> tipi) {
		this.tipi = tipi;
	}
	public TipoDestinazione getTipoDestinazione() {
		return tipoDestinazione;
	}
	public void setTipoDestinazione(TipoDestinazione tipoDestinazione) {
		this.tipoDestinazione = tipoDestinazione;
	}
	
	
}
