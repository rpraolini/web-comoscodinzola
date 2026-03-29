package it.asso.core.dao.common;

import java.io.Serializable;

public class MessaggioDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2127647086712946279L;

	/** *  */
	private String messaggio;

	/**
	 * @return
	 */
	public String getMessaggio() {
		return messaggio;
	}

	/**
	 * @param messaggio
	 */
	public void setMessaggio(String messaggio) {
		this.messaggio = messaggio;
	}
	
	
	
}
