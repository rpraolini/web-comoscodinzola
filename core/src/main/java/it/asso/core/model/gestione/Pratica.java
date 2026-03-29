package it.asso.core.model.gestione;

import it.asso.core.common.Def;
import it.asso.core.model.animali.gestione.Iter;

import java.util.List;


public class Pratica {

	private String id_pratica;
	private String id_animale;
	private String id_stato;
	private String stato;
	private String macro_stato;
	private String account;
	private String dt_aggiornamento;
	private boolean isAperta = true;
	
	private List<Iter> iter;
	
	public String getId_pratica() {
		return id_pratica;
	}
	public void setId_pratica(String id_pratica) {
		this.id_pratica = id_pratica;
	}
	public String getId_animale() {
		return id_animale;
	}
	public void setId_animale(String id_animale) {
		this.id_animale = id_animale;
	}
	public String getId_stato() {
		return id_stato;
	}
	public void setId_stato(String id_stato) {
		this.id_stato = id_stato;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getDt_aggiornamento() {
		return dt_aggiornamento;
	}
	public void setDt_aggiornamento(String dt_aggiornamento) {
		this.dt_aggiornamento = dt_aggiornamento;
	}
	public List<Iter> getIter() {
		return iter;
	}
	public void setIter(List<Iter> iter) {
		this.iter = iter;
	}
	public String getStato() {
		return stato;
	}
	public void setStato(String stato) {
		this.stato = stato;
	}
	public String getMacro_stato() {
		return macro_stato;
	}
	public void setMacro_stato(String macro_stato) {
		this.macro_stato = macro_stato;
	}
	public boolean isAperta() {
		if(Def.ST_P_PRATICA_CHIUSA.equals(getId_stato())) {
			isAperta = false;
		}
		return isAperta;
	}
	public boolean getIsAperta() {
		return !"99".equals(this.id_stato);
	}
	
	
}
