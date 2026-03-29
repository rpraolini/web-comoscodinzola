package it.asso.core.model.contatto;

import it.asso.core.model.documenti.Documento;
import it.asso.core.model.localizzazione.Comune;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;

import java.util.List;

public class Contatto {

	private String id_contatto;
	private String nome;
	private String cognome;
	private String cod_fiscale;
	private List<Documento> documenti;
	private String rag_sociale;
	private String desc_comune;
	private String desc_provincia;
	private String desc_regione;
	private String id_comune;
	private String id_provincia;
	private String id_regione;
	private String indirizzo;
	private String stato;
	private String email;
	private String telefono_1;
	private String telefono_2;
	private String cellulare;
	private String note;
	private String account;
	private String dt_aggiornamento;
	private String id_tipo_contatto;
	private String descrizione;
	private String blacklist;
	private List<Qualifica> qualifiche;
	private Comune comune;
	private Provincia provincia;
	private Regione regione;
	private String nato_a;
	private String data_nascita;
	private String id_tipo_documento;
	private String num_documento;
	private String orario_apertura;
	private List<AttivitaContatto> attivita;
	private String cap;
	private String num_civico;
	private String localita;
	private String indirizzo_completo;
	private String latitudine;
	private String longitudine;
	 

	public String getId_contatto() {
		return id_contatto;
	}
	public void setId_contatto(String id_contatto) {
		this.id_contatto = id_contatto;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getCognome() {
		return cognome;
	}
	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
	public String getRag_sociale() {
		return rag_sociale;
	}
	public void setRag_sociale(String rag_sociale) {
		this.rag_sociale = rag_sociale;
	}
	public String getIndirizzo() {
		return indirizzo;
	}
	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCellulare() {
		return cellulare;
	}
	public void setCellulare(String cellulare) {
		this.cellulare = cellulare;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
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

	public String getId_tipo_contatto() {
		return id_tipo_contatto;
	}
	public void setId_tipo_contatto(String id_tipo_contatto) {
		this.id_tipo_contatto = id_tipo_contatto;
	}
	public String getDescrizione() {
		if("1".equals(getId_tipo_contatto())) {
			descrizione = getNome() + " " + getCognome();
		}else if("4".equals(getId_tipo_contatto())) {
			descrizione = getRag_sociale() + " (" + getOrario_apertura() + ")";
		}else if("6".equals(getId_tipo_contatto())) {
			descrizione = getRag_sociale() + ( getCognome() != null ? (" (" + getNome() + " " + getCognome() + ")") : "" );
		}else{
			descrizione = getRag_sociale();
		}
		return descrizione;
	}

	public String getBlacklist() {
		return blacklist;
	}
	public void setBlacklist(String blacklist) {
		this.blacklist = blacklist;
	}
	public List<Qualifica> getQualifiche() {
		return qualifiche;
	}
	public void setQualifiche(List<Qualifica> qualifiche) {
		this.qualifiche = qualifiche;
	}
	public Comune getComune() {
		return comune;
	}
	public void setComune(Comune comune) {
		this.comune = comune;
	}
	public Provincia getProvincia() {
		return provincia;
	}
	public void setProvincia(Provincia provincia) {
		this.provincia = provincia;
	}
	public Regione getRegione() {
		return regione;
	}
	public void setRegione(Regione regione) {
		this.regione = regione;
	}
	public String getDesc_comune() {
		return desc_comune;
	}
	public void setDesc_comune(String desc_comune) {
		this.desc_comune = desc_comune;
	}
	public String getDesc_provincia() {
		return desc_provincia;
	}
	public void setDesc_provincia(String desc_provincia) {
		this.desc_provincia = desc_provincia;
	}
	public String getDesc_regione() {
		return desc_regione;
	}
	public void setDesc_regione(String desc_regione) {
		this.desc_regione = desc_regione;
	}
	public String getId_comune() {
		return id_comune;
	}
	public void setId_comune(String id_comune) {
		this.id_comune = id_comune;
	}
	public String getId_provincia() {
		return id_provincia;
	}
	public void setId_provincia(String id_provincia) {
		this.id_provincia = id_provincia;
	}
	public String getId_regione() {
		return id_regione;
	}
	public void setId_regione(String id_regione) {
		this.id_regione = id_regione;
	}
	public String getCod_fiscale() {
		return cod_fiscale;
	}
	public void setCod_fiscale(String cod_fiscale) {
		this.cod_fiscale = cod_fiscale;
	}
	public String getTelefono_1() {
		return telefono_1;
	}
	public void setTelefono_1(String telefono_1) {
		this.telefono_1 = telefono_1;
	}
	public String getTelefono_2() {
		return telefono_2;
	}
	public void setTelefono_2(String telefono_2) {
		this.telefono_2 = telefono_2;
	}
	public String getNato_a() {
		return nato_a;
	}
	public void setNato_a(String nato_a) {
		this.nato_a = nato_a;
	}
	public String getData_nascita() {
		return data_nascita;
	}
	public void setData_nascita(String data_nascita) {
		this.data_nascita = data_nascita;
	}
	public List<Documento> getDocumenti() {
		return documenti;
	}
	public void setDocumenti(List<Documento> documenti) {
		this.documenti = documenti;
	}
	public String getId_tipo_documento() {
		return id_tipo_documento;
	}
	public void setId_tipo_documento(String id_tipo_documento) {
		this.id_tipo_documento = id_tipo_documento;
	}
	public String getNum_documento() {
		return num_documento;
	}
	public void setNum_documento(String num_documento) {
		this.num_documento = num_documento;
	}
	public String getOrario_apertura() {
		return orario_apertura;
	}
	public void setOrario_apertura(String orario_apertura) {
		this.orario_apertura = orario_apertura;
	}
	public String getStato() {
		return stato;
	}
	public void setStato(String stato) {
		this.stato = stato;
	}
	public List<AttivitaContatto> getAttivita() {
		return attivita;
	}
	public void setAttivita(List<AttivitaContatto> attivita) {
		this.attivita = attivita;
	}
	public String getCap() {
		return cap;
	}
	public void setCap(String cap) {
		this.cap = cap;
	}
	public String getNum_civico() {
		return num_civico;
	}
	public void setNum_civico(String num_civico) {
		this.num_civico = num_civico;
	}
	public String getLocalita() {
		return localita;
	}
	public void setLocalita(String localita) {
		this.localita = localita;
	}
	public String getIndirizzo_completo() {
		return indirizzo_completo;
	}
	public void setIndirizzo_completo(String indirizzo_completo) {
		this.indirizzo_completo = indirizzo_completo;
	}
	public String getLatitudine() {
		return latitudine;
	}
	public void setLatitudine(String latitudine) {
		this.latitudine = latitudine;
	}
	public String getLongitudine() {
		return longitudine;
	}
	public void setLongitudine(String longitudine) {
		this.longitudine = longitudine;
	}
	
}
