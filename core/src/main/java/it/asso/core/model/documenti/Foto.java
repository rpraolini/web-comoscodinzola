package it.asso.core.model.documenti;

import org.springframework.web.multipart.MultipartFile;

public class Foto {
	
	private String id_foto;
	private String nome_file;
	private String nome_file_t;
	private String dimensione;
	private String estensione;
	private String percorso;
	private String id_animale;
	private String dt_inserimento;
	private String account;
	private String url;
	private String url_t;
	private String didascalia;
	private String pubblica;
	private String id_tipo_foto;
	private MultipartFile[] foto;
	
	
	public MultipartFile[] getFoto() {
		return foto;
	}
	public void setFoto(MultipartFile[] foto) {
		this.foto = foto;
	}
	public String getId_foto() {
		return id_foto;
	}
	public void setId_foto(String id_foto) {
		this.id_foto = id_foto;
	}
	public String getNome_file() {
		return nome_file;
	}
	public void setNome_file(String nome_file) {
		this.nome_file = nome_file;
	}
	public String getDimensione() {
		return dimensione;
	}
	public void setDimensione(String dimensione) {
		this.dimensione = dimensione;
	}
	public String getEstensione() {
		return estensione;
	}
	public void setEstensione(String estensione) {
		this.estensione = estensione;
	}
	public String getPercorso() {
		return percorso;
	}
	public void setPercorso(String percorso) {
		this.percorso = percorso;
	}
	public String getId_animale() {
		return id_animale;
	}
	public void setId_animale(String id_animale) {
		this.id_animale = id_animale;
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDidascalia() {
		return didascalia;
	}
	public void setDidascalia(String didascalia) {
		this.didascalia = didascalia;
	}
	public String getPubblica() {
		return pubblica;
	}
	public void setPubblica(String pubblica) {
		this.pubblica = pubblica;
	}
	/**
	 * @return the id_tipo_foto
	 */
	public String getId_tipo_foto() {
		return id_tipo_foto;
	}
	/**
	 * @param id_tipo_foto the id_tipo_foto to set
	 */
	public void setId_tipo_foto(String id_tipo_foto) {
		this.id_tipo_foto = id_tipo_foto;
	}
	public String getUrl_t() {
		return url_t;
	}
	public void setUrl_t(String url_t) {
		this.url_t = url_t;
	}
	public String getNome_file_t() {
		return nome_file_t;
	}
	public void setNome_file_t(String nome_file_t) {
		this.nome_file_t = nome_file_t;
	}

		

}
