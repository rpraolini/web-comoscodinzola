package it.asso.core.dao.animali.gestione;

import it.asso.core.common.Def;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.attivita.AttivitaDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.attivita.Attivita;
import it.asso.core.model.utente.Utente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProcessoDAO {

	@Autowired
    @Qualifier("animaleAttivitaDAO") AttivitaDAO attivitaDao;
	@Autowired  AnimaleDAO animaleDao;

    private final JdbcTemplate jdbcTemplate;

    public ProcessoDAO(JdbcTemplate jdbcTemplate,@Qualifier("animaleAttivitaDAO") AttivitaDAO attivitaDao, AnimaleDAO animaleDao ) {
        this.jdbcTemplate = jdbcTemplate;
        this.animaleDao = animaleDao;
        this.attivitaDao = attivitaDao;
    }
	
	/**
	 * @param idAnimale, idStato
	 * @return int
	 */
	@Transactional()
	public int setStato(String idAnimale, String idStato) {
		String queryStr = "UPDATE an_animale SET id_stato = ? WHERE id_animale = ? ";
		return  jdbcTemplate.update(queryStr, idStato, idAnimale);
	}
	
	/**
	 * @param id_animale, user, idTipoIter
	 * @return String
	 */
	@Transactional
	public String setStatoAnimale(String id_animale, Utente  user, String idTipoIter) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		//Se idTipoIter = 1 Preaffido e id_stato < 4 allora id_stato=4
		if(Def.TR_PREAFFIDO.equals(idTipoIter) && Integer.valueOf(animale.getId_stato()) < Integer.valueOf(Def.ST_IN_PREAFFIDO)) {
			richiestaPreaffido(id_animale,user);
			animale.setId_stato(Def.ST_IN_PREAFFIDO);
			animaleDao.saveOrUpdate(animale);
		}
		//Se idTipoIter = 2 Adozione e id_stato < 5 allora id_stato=5
		else if(Def.TR_ADOZIONE.equals(idTipoIter) && Integer.valueOf(animale.getId_stato()) < Integer.valueOf(Def.ST_ADOTTATO)) {
			adotta(id_animale,user);
			animale.setId_stato(Def.ST_ADOTTATO);
			animaleDao.saveOrUpdate(animale);
		}
		//Se idTipoIter = 4 Passaggio di proprieta e id_stato < 6 allora id_stato=6
		else if(Def.TR_PROPRIETA.equals(idTipoIter) && Integer.valueOf(animale.getId_stato()) < Integer.valueOf(Def.ST_PROPRIETA)) {
			passaggioProprieta(id_animale,user);
			animale.setId_stato(Def.ST_PROPRIETA);
			animaleDao.saveOrUpdate(animale);
		}
		else if(Def.TR_PROPRIETA.equals(idTipoIter) && (animale.getId_stato().equals(Def.ST_ISTRUTTORIA_CHIUSA)) || animale.getId_stato().equals(Def.ST_CONSEGNATO)) {
			passaggioProprieta(id_animale,user);
			animale.setId_stato(Def.ST_PROPRIETA);
			animaleDao.saveOrUpdate(animale);
		}
		//Se idTipoIter = 3 Consegna e id_stato < 7 allora id_stato=7
		else if(Def.TR_CONSEGNA.equals(idTipoIter) && Integer.valueOf(animale.getId_stato()) < Integer.valueOf(Def.ST_CONSEGNATO)) {
			consegna(id_animale,user);
			animale.setId_stato(Def.ST_CONSEGNATO);
			animaleDao.saveOrUpdate(animale);
		}
				
		return  Def.STR_OK;
	}
	
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String chiudiIstruttoria(String id_animale, Utente  user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		chiusuraIstruttoria(id_animale,user);
		animale.setId_stato(Def.ST_ISTRUTTORIA_CHIUSA);
		animaleDao.saveOrUpdate(animale);
				
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String riapriIstruttoria(String id_animale, Utente  user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		riaperturaIstruttoria(id_animale,user);
		animale.setId_stato(Def.ST_PROPRIETA);
		animaleDao.saveOrUpdate(animale);
				
		return  Def.STR_OK;
	}
	
	
	
	/**
	 * @param idTipoIterPrev, idTipoIterNext, user
	 * @return String
	 */
	@Transactional
	public String setStatoAnimale(String idTipoIterPrev, String idTipoIterNext, Utente  user, String id_animale) {
		
		if(!idTipoIterPrev.equals(idTipoIterNext)) {
			Animale animale = animaleDao.getById(id_animale);
			if(idTipoIterNext == null) {
				revocaPreaffido(id_animale, user);
				animale.setId_stato(Def.ST_ADOTTABILE);
			} else {
				if(Integer.valueOf(idTipoIterPrev) > Integer.valueOf(idTipoIterNext)) {
			
					if(Def.TR_PREAFFIDO.equals(idTipoIterNext)) {
						revocaAdozione(id_animale, user);
						animale.setId_stato(Def.ST_IN_PREAFFIDO);
					}else if(Def.TR_ADOZIONE.equals(idTipoIterNext)) {
						revocaConsegna(id_animale, user);
						animale.setId_stato(Def.ST_ADOTTATO);
					}else if(Def.TR_CONSEGNA.equals(idTipoIterNext)) {
						revocaPassaggioProprieta(id_animale, user);
						animale.setId_stato(Def.ST_CONSEGNATO);
					}
				}
			}
			animaleDao.saveOrUpdate(animale);
		}
				
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String valida(String id_animale, Utente  user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Dati validati");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_VALIDA));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_VALIDA);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String adottabile(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Animale adottabile");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_ADOTTABILE));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_ADOTTABILE);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String revocaAdottabile(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Adottabilita revocata");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_REVOCA_ADOTTABILE));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_REVOCA_ADOTTABILE);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String richiestaPreaffido(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Richiesta di preaffido");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_RICH_PREAFFIDO));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_RICH_PREAFFIDO);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String revocaPreaffido(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Preaffido revocato");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_REVOCA_PREAFFIDO));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_REVOCA_PREAFFIDO);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String revocaAdozione(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Adozione revocata");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_REVOCA_ADOZIONE));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_REVOCA_ADOZIONE);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String passaggioProprieta(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Passaggio di proprieta");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_PROPRIETA));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_PROPRIETA);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String revocaPassaggioProprieta(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Passaggio di proprieta revocato");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_REVOCA_PROPRIETA));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_REVOCA_PROPRIETA);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String adotta(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Richiesta di adozione");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_ADOZIONE));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_ADOZIONE);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param idAnimale, user
	 * @return String
	 */
	@Transactional
	public void registraDecesso(String idAnimale, String dtDecesso, Utente utente) {
		Animale animale = animaleDao.getById(idAnimale);

		// 1. Salva l'attività (logica preesistente mantenuta)
		Attivita attivita = new Attivita();
		attivita.setId_animale(idAnimale);
		attivita.setNote_attivita("Dichiarato il decesso" +
				(dtDecesso != null ? " in data " + dtDecesso : ""));
		attivita.setId_utente(utente.getId_utente());
		attivita.setAccount(utente.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_DECESSO));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_DECESSO);
		attivitaDao.save(attivita);
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String consegna(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Consegnato ad adottante");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_CONSEGNA));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_CONSEGNA);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String revocaConsegna(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Revoca consegna ad adottante");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_REVOCA_CONSEGNA));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_REVOCA_CONSEGNA);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String chiusuraIstruttoria(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Istruttoria chiusa");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_CHIUSURA_ISTRUTTORIA));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_CHIUSURA_ISTRUTTORIA);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	
	
	/**
	 * @param id_animale, user
	 * @return String
	 */
	@Transactional
	public String riaperturaIstruttoria(String id_animale, Utente user) {
		
		Animale animale = animaleDao.getById(id_animale);
		
		Attivita attivita = new Attivita();
		attivita.setId_animale(id_animale);
		attivita.setNote_attivita("Istruttoria riaperta");
		attivita.setId_utente(user.getId_utente());
		attivita.setAccount(user.getAccount());
		attivita.setId_stato_padre(attivitaDao.getStatoPadre(Def.ATT_RIAPERTURA_ISTRUTTORIA));
		attivita.setId_stato_precedente(animale.getId_stato());
		attivita.setId_attivita(Def.ATT_RIAPERTURA_ISTRUTTORIA);
		
		attivitaDao.save(attivita);
		
		return  Def.STR_OK;
	}
	

}
