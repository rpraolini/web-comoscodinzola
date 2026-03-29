package it.asso.core.dto.ricerca;

public class FiltroRicerca {
    private String tipo;
    private String eta;
    private String taglia;
    private String sesso;
    private String regione;
    private String provincia;

    // Getter e Setter (o usa @Data di Lombok se lo hai)
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }

    public String getTaglia() { return taglia; }
    public void setTaglia(String taglia) { this.taglia = taglia; }

    public String getSesso() { return sesso; }
    public void setSesso(String sesso) { this.sesso = sesso; }

    public String getRegione() { return regione; }
    public void setRegione(String regione) { this.regione = regione; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
}
