package it.asso.core.common;

import java.util.List;

public class ResultGrid {
	private int totale;
	private List<?> records;
	private List<?> totali;
	
	public int getTotale() {
		return totale;
	}
	public void setTotale(int totale) {
		this.totale = totale;
	}
	public List<?> getRecords() {
		return records;
	}
	public void setRecords(List<?> records) {
		this.records = records;
	}
	public List<?> getTotali() {
		return totali;
	}
	public void setTotali(List<?> totali) {
		this.totali = totali;
	}

}
