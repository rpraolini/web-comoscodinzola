package it.asso.core.model.documenti;

public class Video {

	private String id_video;
	private String id_animale;
	private String dt_inserimento;
	private String account;
	private String url;
	private String pubblico;
	
	
	public String getId_video() {
		return id_video;
	}
	public void setId_video(String id_video) {
		this.id_video = id_video;
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
	public String getPubblico() {
		return pubblico;
	}
	public void setPubblico(String pubblico) {
		this.pubblico = pubblico;
	}
	
	
}
