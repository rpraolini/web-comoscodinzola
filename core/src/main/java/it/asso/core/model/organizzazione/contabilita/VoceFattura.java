package it.asso.core.model.organizzazione.contabilita;

public class VoceFattura {

	private String id_vf;
	private String descrizione;
	private String id_cr_sotto_voce;
	private String sottovoce;
	private int locked;
	private Rendiconto rendiconto;
	
	public String getId_vf() {
		return id_vf;
	}
	public void setId_vf(String id_vf) {
		this.id_vf = id_vf;
	}
	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	public String getSottovoce() {
		return sottovoce;
	}
	public void setSottovoce(String sottovoce) {
		this.sottovoce = sottovoce;
	}
	public Rendiconto getRendiconto() {
		return rendiconto;
	}
	public void setRendiconto(Rendiconto rendiconto) {
		this.rendiconto = rendiconto;
	}
	public String getId_cr_sotto_voce() {
		return id_cr_sotto_voce;
	}
	public void setId_cr_sotto_voce(String id_cr_sotto_voce) {
		this.id_cr_sotto_voce = id_cr_sotto_voce;
	}
	public int getLocked() {
		return locked;
	}
	public void setLocked(int locked) {
		this.locked = locked;
	}

	
}
