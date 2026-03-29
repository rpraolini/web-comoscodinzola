package it.asso.core.model.organizzazione.contabilita;

public class VoceMovimento {
	
	private String id_vm;
	private String descrizione;
	private String id_cr_sotto_voce;
	private String sottovoce;
	private Rendiconto rendiconto;
	
	
	public String getId_vm() {
		return id_vm;
	}
	public void setId_vm(String id_vm) {
		this.id_vm = id_vm;
	}
	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	public String getId_cr_sotto_voce() {
		return id_cr_sotto_voce;
	}
	public void setId_cr_sotto_voce(String id_cr_sotto_voce) {
		this.id_cr_sotto_voce = id_cr_sotto_voce;
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
	
	
	

}
