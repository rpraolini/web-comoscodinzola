package it.asso.core.model.contatto;

import it.asso.core.model.raccolta.Evento;

import java.util.List;



public class ContattiWrapper {

	private List<Contatto> contatti;
	private it.asso.core.model.raccolta.Evento evento;

	public List<Contatto> getContatti() {
		return contatti;
	}

	public void setContatti(List<Contatto> contatti) {
		this.contatti = contatti;
	}

	public Evento getEvento() {
		return evento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}
	
	
}
