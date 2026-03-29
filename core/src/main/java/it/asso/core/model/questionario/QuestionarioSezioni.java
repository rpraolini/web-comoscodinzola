package it.asso.core.model.questionario;

import java.util.List;

public class QuestionarioSezioni {
	
	private String id_sezione;
	private String sezione;
	
	
	private List<Questionario> questionario;
	
	public String getId_sezione() {
		return id_sezione;
	}
	public void setId_sezione(String id_sezione) {
		this.id_sezione = id_sezione;
	}
	public String getSezione() {
		return sezione;
	}
	public void setSezione(String sezione) {
		this.sezione = sezione;
	}
	public List<Questionario> getQuestionario() {
		return questionario;
	}
	public void setQuestionario(List<Questionario> questionario) {
		this.questionario = questionario;
	}
	
	
}
