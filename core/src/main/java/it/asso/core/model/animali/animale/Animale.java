package it.asso.core.model.animali.animale;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

import it.asso.core.common.Def;
import it.asso.core.common.Utils;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.documenti.Foto;
import it.asso.core.model.tag.Tag;

public class Animale {

    private String id_animale;
    private String cod_animale;
    private String nome;
    private String dt_nascita;
    private String num_microchip;
    private String peso;
    private String id_colore;
    private String id_tipo_animale;
    private String id_stato;
    private String caratteristiche;
    private String descr_stato;
    private String sesso;
    private Foto foto;
    private String coloreStato;
    private String eta;
    private String periodo;
    private String periodo_short;
    private String id_razza;
    private String razza;
    private String tipo_razza;
    private String taglia;
    private boolean sterilizzato;
    private Contatto proprietario;
    private List<Documento> documenti;
    private List<Tag> tags;
    private String regione;
    private String descr_breve;
    private String descr_lunga;
    private String location;

    public String getId_animale() {
        return id_animale;
    }

    public String getCod_animale() {
        return cod_animale;
    }

    public void setCod_animale(String cod_animale) {
        this.cod_animale = cod_animale;
    }

    public void setId_animale(String id_animale) {
        this.id_animale = id_animale;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDt_nascita() {
        return dt_nascita;
    }

    public void setDt_nascita(String dt_nascita) {
        this.dt_nascita = dt_nascita;
    }

    public String getNum_microchip() {
        return num_microchip;
    }

    public void setNum_microchip(String num_microchip) {
        this.num_microchip = num_microchip;
    }

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getId_colore() {
        return id_colore;
    }

    public void setId_colore(String id_colore) {
        this.id_colore = id_colore;
    }

    public String getId_tipo_animale() {
        return id_tipo_animale;
    }

    public void setId_tipo_animale(String id_tipo_animale) {
        this.id_tipo_animale = id_tipo_animale;
    }

    public String getId_stato() {
        return id_stato;
    }

    public void setId_stato(String id_stato) {
        this.id_stato = id_stato;
    }

    public String getCaratteristiche() {
        return caratteristiche;
    }

    public void setCaratteristiche(String caratteristiche) {
        this.caratteristiche = caratteristiche;
    }

    public String getDescr_stato() {
        return descr_stato;
    }

    public void setDescr_stato(String descr_stato) {
        this.descr_stato = descr_stato;
    }

    public Foto getFoto() {
        return foto;
    }

    public void setFoto(Foto foto) {
        this.foto = foto;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public String getId_razza() {
        return id_razza;
    }

    public void setId_razza(String id_razza) {
        this.id_razza = id_razza;
    }

    public String getTipo_razza() {
        return tipo_razza;
    }

    public String getRazza() {
        return razza;
    }

    public void setRazza(String razza) {
        this.razza = razza;
    }

    public void setTipo_razza(String tipo_razza) {
        this.tipo_razza = tipo_razza;
    }

    public boolean isSterilizzato() {
        return sterilizzato;
    }

    public void setSterilizzato(boolean sterilizzato) {
        this.sterilizzato = sterilizzato;
    }

    public String getColoreStato() {
        if (Def.ST_INSERITO.equals(id_stato)) {
            coloreStato = "gray";
        } else if (Def.ST_VALIDA.equals(id_stato)) {
            coloreStato = "orange";
        } else if (Def.ST_ADOTTABILE.equals(id_stato)) {
            coloreStato = "#0066ff";
        } else if (Def.ST_IN_PREAFFIDO.equals(id_stato)) {
            coloreStato = "#990099";
        } else if (Def.ST_ADOTTATO.equals(id_stato)) {
            coloreStato = "purple";
        } else if (Def.ST_PROPRIETA.equals(id_stato)) {
            coloreStato = "#cc3300";
        } else if (Def.ST_CONSEGNATO.equals(id_stato)) {
            coloreStato = "green";
        } else if (Def.ST_DECESSO.equals(id_stato)) {
            coloreStato = "black";
        } else if (Def.ST_ISTRUTTORIA_CHIUSA.equals(id_stato)) {
            coloreStato = "#404040";
        } else {
            coloreStato = "gray";
        }
        return coloreStato;
    }

    public String getEta() throws ParseException {
        if (dt_nascita == null) {
            eta = Def.ET_ND;
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dt = LocalDate.parse(dt_nascita, formatter);
            Period period = Period.between(dt, LocalDate.now());
            if (period.getYears() < 1 && period.getMonths() <= 11) {
                eta = Def.ET_CUCCIOLO;
            } else if (period.getYears() <= 3) {
                eta = Def.ET_ADULTO_GIOVANE;
            } else if (period.getYears() <= 5) {
                eta = Def.ET_ADULTO_GIOVANE;
            } else if (period.getYears() <= 10) {
                eta = Def.ET_ADULTO;
            } else if (period.getYears() > 10) {
                eta = Def.ET_ANZIANO;
            }
            if (period.getYears() < 1) {
                setPeriodo(String.valueOf(period.getMonths()) + "m " + String.valueOf(period.getDays()) + "g ");
                setPeriodo_short(String.valueOf(period.getMonths()) + "m");
            } else {
                setPeriodo(String.valueOf(period.getYears()) + "a " + String.valueOf(period.getMonths()) + "m ");
                setPeriodo_short(String.valueOf(period.getYears()) + "a ");
            }
        }

        return eta;
    }


    public String getPeriodo_short() {
        return periodo_short;
    }

    public void setPeriodo_short(String periodo_short) {
        this.periodo_short = periodo_short;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getTaglia() {
        if (peso == null) {
            taglia = Def.TG_ND;
        } else if (Utils.getDouble(peso) <= 6) {
            taglia = Def.TG_MINI;
        } else if (Utils.getDouble(peso) <= 10) {
            taglia = Def.TG_PICCOLA;
        } else if (Utils.getDouble(peso) <= 15) {
            taglia = Def.TG_MEDIO_PICCOLA;
        } else if (Utils.getDouble(peso) <= 18) {
            taglia = Def.TG_MEDIO_CONTENUTA;
        } else if (Utils.getDouble(peso) <= 25) {
            taglia = Def.TG_MEDIA;
        } else if (Utils.getDouble(peso) <= 30) {
            taglia = Def.TG_MEDIA_ABBONDANTE;
        } else if (Utils.getDouble(peso) > 30) {
            taglia = Def.TG_GRANDE;
        }
        return taglia;
    }

    public Contatto getProprietario() {
        return proprietario;
    }

    public void setProprietario(Contatto proprietario) {
        this.proprietario = proprietario;
    }

    public List<Documento> getDocumenti() {
        return documenti;
    }

    public void setDocumenti(List<Documento> documenti) {
        this.documenti = documenti;
    }

    public String getRegione() {
        return regione;
    }

    public void setRegione(String regione) {
        this.regione = regione;
    }

    public String getDescr_breve() {
        return descr_breve;
    }

    public void setDescr_breve(String descr_breve) {
        this.descr_breve = descr_breve;
    }

    public String getDescr_lunga() {
        return descr_lunga;
    }

    public void setDescr_lunga(String descr_lunga) {
        this.descr_lunga = descr_lunga;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
