package it.asso.core.model.documenti;

import java.util.List;

public class Documento {

	private String id_documento;
	private String id_tipo_documento;
	private TipoDocumento tipoDocumento;
	private String num_documento;
	private String account;
	private String dt_inserimento;
	private String note;
	
	private List<AssoFile> assoFiles;
	
	
	public String getId_documento() {
		return id_documento;
	}
	public void setId_documento(String id_documento) {
		this.id_documento = id_documento;
	}
	public String getId_tipo_documento() {
		return id_tipo_documento;
	}
	public void setId_tipo_documento(String id_tipo_documento) {
		this.id_tipo_documento = id_tipo_documento;
	}
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}
	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
	public String getNum_documento() {
		return num_documento;
	}
	public void setNum_documento(String num_documento) {
		this.num_documento = num_documento;
	}
	public List<AssoFile> getAssoFiles() {
		return assoFiles;
	}
	public void setAssoFiles(List<AssoFile> assoFiles) {
		this.assoFiles = assoFiles;
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
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	
	
}
