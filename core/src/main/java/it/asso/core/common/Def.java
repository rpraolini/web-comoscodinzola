package it.asso.core.common;

public class Def {
	
	public static final String  PWD_DEFAULT = "Asso123!";
	
	public static final String  DATA_FORMAT_DB = "dd/mm/rrrr";
	public static final String  DATA_FORMAT_SIMPLE = "dd/MM/yyyy";
	public static final String  DATA_FORMAT_HOUR = "hh:mm:ss a";
	public static final String  DATA_FORMAT_DATA_HOUR = "dd/MM/yyyy hh:mm:ss";
	
	public static final String NUM_MENO_UNO = "-1";
	public static final String NUM_ZERO = "0";
	public static final String NUM_UNO = "1";
	public static final String NUM_DUE = "2";
	public static final String NUM_TRE = "3";
	public static final String NUM_QUATTRO = "4";
	public static final String NUM_CINQUE = "5";
	public static final String NUM_OTTO = "8";
	public static final String NUM_NOVANTANOVE = "99";
	
	public static final String NUM_MAGGIORE_DUE = ">2";
	
	public static final String TIPO_DOC_TEMPORANEO = "17";
	public static final String TIPO_DOC_FATTURA = "15";
	
	public static final String TIPO_DOC_RICEVUTA= "18";
	public static final String TIPO_DOC_SCONTRINO= "19";
	
	public static final String TIPO_MOVIMENTO_USCITA = "2";
	public static final String TIPO_MOVIMENTO_ENTRATA = "1";
	public static final String TIPO_MOVIMENTO_INTERNO = "3";
	
	public static final String CAUSALE_MOVIMENTO_FATTURA = "3";
	public static final String CAUSALE_MOVIMENTO_RISCOSSIONE_FATTURA = "11";
	public static final String CAUSALE_MOVIMENTO_RITENUTA_ACCONTO = "8";
	public static final String CAUSALE_GIROFONDO_ENTRATA = "10";
	public static final String CAUSALE_GIROFONDO_USCITA = "9";
	
	public static final String CONTATTO_ORGANIZZAZIONE = "0";
	
	public static final String RITENUTA_ACCONTO = "16";
	
	public static final String FOTO_PROFILO = "1";
	public static final String FOTO_POST_ADOZIONE = "2";
	public static final String FOTO_PUBBLICA = "1";
	
	public static final String STR_KO = "ko";
	public static final String STR_OK = "ok";
	
	public static final String STR_SUCCESS = "success";
	public static final String STR_ERROR = "Error: ";
	
	
	public static final String STR_UNDEFINED = "undefined";
	public static final String STR_PERCENTAGE = "%";
	
	public static final String EVE_STERILIZZATO = "14";
	
	public static final String STR_VOLONTARIO_CIBO_RINGRAZIAMENTO = "RINGRAZIAMENTO PER RACCOLTA CIBO";
	public static final String STR_VOLONTARIO_CIBO = "RACCOLTA CIBO";
	public static final String STR_RATE_SCADUTE = "RATE SCADUTE";
	public static final String STR_RATE_IN_SCADENZA = "RATE IN SCADENZA";
	public static final String STR_QUESTIONARIO = "QUESTIONARIO";
	public static final String STR_PREAFFIDANTE = "PREAFFIDANTE";
	public static final String STR_GENERICA = "GENERICA";
	public static final String STR_VACCINO_IN_SCADENZA_7 = "VACCINAZIONE IN SCADENZA FRA 7 GIORNI";
	public static final String STR_VACCINO_IN_SCADENZA_15 = "VACCINAZIONE IN SCADENZA FRA 15 GIORNI";
	
	public static final String STR_PREFIX_NUOVO_ANIMALE = " - Copia";
	
	/* aree dati / ruoli */
	
	public static final String ROLE_AMMINISTRATORE = "amministratore";
	
	public static final String AREA_DATI_TABELLE = "tabelle"; 
	public static final String AREA_DATI_CONTATTI = "contatti"; 
	public static final String AREA_DATI_ANIMALE = "animale";
	public static final String AREA_DATI_CONTABILITA = "contabilita";
	public static final String AREA_DATI_UTENTE = "utente";
	public static final String AREA_DATI_RACCOLTA = "raccolta";
	public static final String AREA_DATI_ADOZIONI = "adozioni";
	public static final String AREA_DATI_ASSOCIAZIONE = "associazione";
	
	/* eccezioni */
	public static final String STR_ERROR_000 = "000 - Errore generico : ";
	public static final String STR_ERROR_100 = "100 - Devi essere autenticato per eseguire questa operazione!";
	public static final String STR_ERROR_200 = "200 - Non hai i permessi necessari per eseguire questa operazione!";
	public static final String STR_ERROR_300 = "300 - Non hai il ruolo necessario per eseguire questa operazione!";
	
	public static final String STR_ERROR_500 = "500 - Valore gia' presente in tabella";
	public static final String STR_ERROR_501 = "501 - Elemento non eliminabile.";
	public static final String STR_ERROR_502 = "502 - Il microchip e' gia' presente nella banca dati.";
	
	public static final String STR_ERROR_600 = "600 - Utente gia' esistente";
	
	public static final String STR_ERROR_701 = "701 - Esiste gia' una pratica aperta.";
	
	public static final String STR_ERROR_1000 = "Inserire l'origine dell'animale.";
	public static final String STR_ERROR_1001 = "L'utente non puo' essere eliminato. Esistono delle attivita' a suo nome. Puoi disabilitarlo.";
	
	/* attivita */
	public static final String ATT_PRIMO_INSERIMENTO = "1";
	public static final String ATT_PRIMO_INSERIMENTO_DESCR = "Primo inserimento a sistema";
	public static final String ATT_VALIDA = "2";
	public static final String ATT_ADOTTABILE = "3";
	public static final String ATT_REVOCA_ADOTTABILE = "4";
	public static final String ATT_RICH_PREAFFIDO ="5";
	public static final String ATT_ADOZIONE = "6";
	public static final String ATT_REVOCA_PREAFFIDO = "7";
	public static final String ATT_REVOCA_ADOZIONE = "8";
	public static final String ATT_PROPRIETA = "9";
	public static final String ATT_REVOCA_PROPRIETA = "10";
	public static final String ATT_CONSEGNA = "11";
	public static final String ATT_REVOCA_CONSEGNA = "12";
	public static final String ATT_DECESSO = "99";
	public static final String ATT_CHIUSURA_ISTRUTTORIA = "98";
	public static final String ATT_RIAPERTURA_ISTRUTTORIA = "97";
	
	/* stati */
	public static final String ST_INSERITO = "1";
	public static final String ST_VALIDA = "2";
	public static final String ST_ADOTTABILE = "3";
	public static final String ST_REVOCA_ADOTTABILE = "2";
	public static final String ST_IN_PREAFFIDO = "4";
	public static final String ST_ADOTTATO = "5";
	public static final String ST_PROPRIETA = "6";
	public static final String ST_CONSEGNATO = "7";
	public static final String ST_DECESSO = "99";
	public static final String ST_ISTRUTTORIA_CHIUSA = "98";
	
	/* tipo di contatto */
	public static final String TC_PERSONA_FISICA = "1";
	
	/* attivita istruttoria */
	public static final String ATT_P_APERTURA_PRATICA = "1";
	public static final String ATT_P_CHIUSURA_PRATICA = "99";
	
	/* stati istruttoria*/
	public static final String ST_P_PRATICA_APERTA = "1";
	public static final String ST_P_PRATICA_CHIUSA = "99";

	public static final String ST_P_PRATICA_ATTIVA = "ATTIVO";
	public static final String ST_ATTIVO = "ATTIVO";
	public static final String ST_CHIUSO = "CHIUSO";
	
	/* taglia */
	public static final String TG_MINI = "Mini";
	public static final String TG_PICCOLA = "Piccola";
	public static final String TG_MEDIO_PICCOLA = "Medio piccola";
	public static final String TG_MEDIO_CONTENUTA = "Medio contenuta";
	public static final String TG_MEDIA = "Media";
	public static final String TG_MEDIA_ABBONDANTE = "Media abbondante";
	public static final String TG_GRANDE = "Grande";
	public static final String TG_ND = "Non definita";
	
	/* anzianita */
	public static final String ET_CUCCIOLO = "Cucciolo";
	public static final String ET_ADULTO_GIOVANE = "Adulto giovane";
	public static final String ET_ADULTO = "Adulto";
	public static final String ET_ANZIANO = "Anziano";
	public static final String ET_ND = "Non definita";

	/* tipi di richiesta */
	public static final String TR_PREAFFIDO = "1";
	public static final String TR_ADOZIONE = "2";
	public static final String TR_PROPRIETA = "4";
	public static final String TR_CONSEGNA = "3";
	
	
	/* preaffido */
	public static final String STR_QUEST_INVIATO = "I";
	public static final String STR_QUEST_RITORNATO = "R";
	
	/* ruoli */
	public static final String STR_ROLE_ADMIN = "1";
	public static final String STR_ROLE_GUEST = "2";
	
	
	/* aree dati */
	public static final String SCHEDA = "1";
	public static final String CONTATTI = "2";
	public static final String ANAGRAFICHE = "3";
	public static final String UTENTI = "4";
	
	/* contatti */
	public static final String DOC_CONTATTO = "C";
	public static final String DOC_ITER = "I";
	public static final String DOC_EC = "E";
	public static final String DOC_ANIMALE = "A";
	
	/* ambiti documenti */
	public static final String DOC_FATTURA = "F";
	public static final String DOC_MOVIMENTO = "M";
	public static final String DOC_ASSOCIAZIONE = "O";
	public static final String DOC_VERBALI = "V";
	
	/* attivita da loggare */
	public static final String LOG_VISUALIZZA_DETTAGLIO_PUBBLICO = "Visualizza dettaglio";
	public static final String LOG_VISUALIZZA_VOLANTINO_PUBBLICO = "Volantino";
	
	/* eventi storici */
	public static final String EVENTO_STORICO_ADOZIONE  = "6";
	
	/* eventi push */
	public static final String EVENTO_PUSH_ADOTTABILE  = "2";
	
	/* TESTI PUSH */
	public static final String EVENTO_PUSH_TITOLO_ADOTTABILE  = "%s da oggi e' adottabile";
	public static final String EVENTO_PUSH_TESTO_ADOTTABILE  = "Oggi il nostro amico %s e' adottabile";
	
	/* TIPI CONTATTO */
	public static final String CONTATTO_PERSONA_FISICA = "1";
	public static final String CONTATTO_CANILE = "2";
	public static final String CONTATTO_PENSIONE = "3";
	public static final String CONTATTO_PUNTO_VENDITA = "4";
	public static final String CONTATTO_VETERINARIO_CLINICA = "5";
	public static final String CONTATTO_FORNITORE = "6";
	public static final String CONTATTO_ASSOCIAZIONE = "7";

	
}
