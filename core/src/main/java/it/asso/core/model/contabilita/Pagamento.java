package it.asso.core.model.contabilita;

public class Pagamento {

	private String id_evento;
    private String dt_pagamento;
    private String importo;
    private String id_pagamento;
    private String note;
    private String tipoPagamento;
    
    
    
	public String getId_evento() {
		return id_evento;
	}
	public void setId_evento(String id_evento) {
		this.id_evento = id_evento;
	}
	public String getDt_pagamento() {
		return dt_pagamento;
	}
	public void setDt_pagamento(String dt_pagamento) {
		this.dt_pagamento = dt_pagamento;
	}
	public String getImporto() {
		return importo;
	}
	public void setImporto(String importo) {
		this.importo = importo;
	}
	public String getId_pagamento() {
		return id_pagamento;
	}
	public void setId_pagamento(String id_pagamento) {
		this.id_pagamento = id_pagamento;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getTipoPagamento() {
		return tipoPagamento;
	}
	public void setTipoPagamento(String tipoPagamento) {
		this.tipoPagamento = tipoPagamento;
	}
    
    
}
