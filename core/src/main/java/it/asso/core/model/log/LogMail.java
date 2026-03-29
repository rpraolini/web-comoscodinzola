package it.asso.core.model.log;

public class LogMail {

	private String id_mail;
	private String note;
	private String mail_to;
	private String dt_invio;
	
	public String getId_mail() {
		return id_mail;
	}
	public void setId_mail(String id_mail) {
		this.id_mail = id_mail;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getMail_to() {
		return mail_to;
	}
	public void setMail_to(String mail_to) {
		this.mail_to = mail_to;
	}
	public String getDt_invio() {
		return dt_invio;
	}
	public void setDt_invio(String dt_invio) {
		this.dt_invio = dt_invio;
	}
	
	
}
