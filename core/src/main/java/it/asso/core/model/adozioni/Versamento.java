package it.asso.core.model.adozioni;

public class Versamento {

	private String id_versamento;
	private String id_adozione;
	private String importo;
	private String dt_versamento;
	private String account;
	private String dt_inserimento;


	public String getId_adozione() {
		return id_adozione;
	}
	public void setId_adozione(String id_adozione) {
		this.id_adozione = id_adozione;
	}
	public String getId_versamento() {
		return id_versamento;
	}
	public void setId_versamento(String id_versamento) {
		this.id_versamento = id_versamento;
	}
	public String getImporto() {
		return importo;
	}
	public void setImporto(String importo) {
		this.importo = importo;
	}
	public String getDt_versamento() {
		return dt_versamento;
	}
	public void setDt_versamento(String dt_versamento) {
		this.dt_versamento = dt_versamento;
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
	
	
}
