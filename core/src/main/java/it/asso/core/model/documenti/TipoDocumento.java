package it.asso.core.model.documenti;

public class TipoDocumento {

	private String id_tipo_documento;
	private String documento;
	private String ambito;
	private String prefix_filename;
	
	
	public String getId_tipo_documento() {
		return id_tipo_documento;
	}
	public void setId_tipo_documento(String id_tipo_documento) {
		this.id_tipo_documento = id_tipo_documento;
	}
	public String getDocumento() {
		return documento;
	}
	public void setDocumento(String documento) {
		this.documento = documento;
	}
	public String getAmbito() {
		return ambito;
	}
	public void setAmbito(String ambito) {
		this.ambito = ambito;
	}
	public String getPrefix_filename() {
		return prefix_filename;
	}
	public void setPrefix_filename(String prefix_filename) {
		this.prefix_filename = prefix_filename;
	}
	
	
}
