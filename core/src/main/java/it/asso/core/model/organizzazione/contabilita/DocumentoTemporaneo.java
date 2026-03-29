package it.asso.core.model.organizzazione.contabilita;

import it.asso.core.model.documenti.AssoFile;
import it.asso.core.model.documenti.Documento;

import java.util.List;


public class DocumentoTemporaneo extends Documento {
	
	private String id_documento;
	private String dt_inserimento;
	private String account;
	
	private List<AssoFile> assoFiles;

	public String getId_documento() {
		return id_documento;
	}

	public void setId_documento(String id_documento) {
		this.id_documento = id_documento;
	}

	public String getDt_inserimento() {
		return dt_inserimento;
	}

	public void setDt_inserimento(String dt_inserimento) {
		this.dt_inserimento = dt_inserimento;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public List<AssoFile> getAssoFiles() {
		return assoFiles;
	}

	public void setAssoFiles(List<AssoFile> assoFiles) {
		this.assoFiles = assoFiles;
	}


	

}
