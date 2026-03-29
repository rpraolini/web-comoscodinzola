package it.asso.core.dao.contatto;

import it.asso.core.common.Def;
import it.asso.core.common.IsNull;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.localizzazione.LocalizzazioneDAO;
import it.asso.core.model.contatto.*;
import it.asso.core.model.localizzazione.Regione;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



@Repository
public class ContattoDAO{
	
private static Logger logger = LoggerFactory.getLogger(ContattoDAO.class);

    private final JdbcTemplate jdbcTemplate;
    private final LocalizzazioneDAO amministrazioneDao;
    private final DocumentoDAO documentoDao;

    public ContattoDAO(JdbcTemplate jdbcTemplate, LocalizzazioneDAO amministrazioneDao, DocumentoDAO documentoDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.documentoDao = documentoDao;
        this.amministrazioneDao = amministrazioneDao;
    }

	
	/**
	 * @param ricerca
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getBySearch(RicercaDTO ricerca) {
		List<Contatto> contatti;
		String whereTipo = "";
		String forPagination = "";
		
		if(!"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		if(!ricerca.getIdTipoContatto().equals("")) {
			whereTipo = " and id_tipo_contatto = " + ricerca.getIdTipoContatto() + " ";
		}
		
		if(!ricerca.getIdQualifica().equals("")) {
			whereTipo = " and q.id_qualifica = " + ricerca.getIdQualifica() + " ";
		}
		
		String whereRegione = !"".equals(ricerca.getRegione()) && ricerca.getRegione() != null ? " and (id_regione = " + ricerca.getRegione() + ") " : "";
		
		String whereProvincia = !"".equals(ricerca.getProvincia()) && ricerca.getProvincia() != null? " and (id_provincia =  " + ricerca.getProvincia() + ") " : "";

		String strToSearch = "%" + IsNull.thenBlank(ricerca.getSearch()).replaceAll(" ", "") + "%";
		String queryStr = "SELECT  DISTINCT   a.id_contatto," + 
				"    a.nome," + 
				"    a.cognome," + 
				"    a.rag_sociale," + 
				"    a.comune," + 
				"    a.provincia," + 
				"    a.indirizzo," + 
				"    a.email," + 
				"    a.telefono_1," + 
				"    a.telefono_2," + 
				"    a.cellulare," + 
				"    a.note," + 
				"    a.account," + 
				"    NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento," + 
				"    a.id_tipo_contatto," + 
				"    a.blacklist," + 
				"    a.num_civico," + 
				"    a.localita," + 
				"    a.indirizzo_completo," + 
				"    a.latitudine," + 
				"    a.longitudine," + 
				"    a.cap," + 
				"    a.regione," + 
				"    a.id_comune," + 
				"    a.id_provincia," + 
				"    a.id_regione," + 
				"    a.cod_fiscale," + 
				"    a.nato_a," + 
				"    NullIf(Date_Format(a.data_nascita, '%d/%m/%Y'), '') data_nascita," + 
				"    a.id_tipo_documento," + 
				"    a.num_documento," + 
				"    a.orario_apertura " + 
				"FROM an_contatti a Left Join an_r_qualifiche_contatto q On q.id_contatto = a.id_contatto "
				+ " WHERE upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like (?) " + whereTipo + whereRegione + whereProvincia + " order by a.nome, a.rag_sociale " + forPagination;

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim()  });
			contatti = fillContatti(contatti);
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return new ArrayList<Contatto>();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new ArrayList<Contatto>();
		}
		
	}
	
	/**
	 * @param ricerca
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getCountBySearch(RicercaDTO ricerca) {
		int numContatti;
		String whereTipo = "";

		if(!ricerca.getIdTipoContatto().equals("")) {
			whereTipo = " and id_tipo_contatto = " + ricerca.getIdTipoContatto() + " ";
		}
		
		if(!ricerca.getIdQualifica().equals("")) {
			whereTipo = " and q.id_qualifica = " + ricerca.getIdQualifica() + " ";
		}
		
		String whereRegione = !"".equals(ricerca.getRegione()) && ricerca.getRegione() != null ? " and (id_regione = " + ricerca.getRegione() + ") " : "";
		
		String whereProvincia = !"".equals(ricerca.getProvincia()) && ricerca.getProvincia() != null? " and (id_provincia =  " + ricerca.getProvincia() + ") " : "";

		String strToSearch = "%" + IsNull.thenBlank(ricerca.getSearch()) + "%";
		String queryStr = "SELECT  count(DISTINCT a.id_contatto) " + 
				"FROM an_contatti a Left Join an_r_qualifiche_contatto q On q.id_contatto = a.id_contatto "
				+ " WHERE upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like (?) " + whereTipo + whereRegione + whereProvincia;

		try{
			numContatti =  jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { strToSearch.toUpperCase().trim()});
			return numContatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
		
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getBySearch(String strToSearch, String idTipo) {
		List<Contatto> contatti;
		String whereTipo = "";
		if(Def.NUM_CINQUE.equals(idTipo)) {
			whereTipo = " and id_tipo_contatto = " + Def.CONTATTO_PENSIONE + " ";
		}else if(Def.NUM_TRE.equals(idTipo)) {
			whereTipo = " and id_tipo_contatto in (" + Def.CONTATTO_PERSONA_FISICA + " , " + Def.CONTATTO_PENSIONE + ") ";
		}else if(Def.NUM_UNO.equals(idTipo)) {
			whereTipo = " and id_tipo_contatto = " + Def.CONTATTO_PERSONA_FISICA + " ";
		}
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT id_contatto,nome,cognome,rag_sociale,comune,provincia,indirizzo,email,telefono_1,telefono_2,cellulare,note, " + 
				"account,NullIf(Date_Format(dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, id_tipo_contatto, blacklist, " +
				"num_civico, localita, indirizzo_completo, latitudine, longitudine, " +
				"cap, regione, id_comune, id_provincia, id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti"
				+ " WHERE upper(CONCAT(ifnull(nome,''),ifnull(cognome,''),ifnull(rag_sociale,''))) like (?) " + whereTipo + " order by nome, rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	@Transactional(readOnly = true)
	public List<ContattoAutocompleteDTO> getBySearchLight(String strToSearch, String idTipo) {
		String whereTipo = "";
		if (Def.NUM_CINQUE.equals(idTipo)) {
			whereTipo = " and id_tipo_contatto = " + Def.CONTATTO_PENSIONE;
		} else if (Def.NUM_TRE.equals(idTipo)) {
			whereTipo = " and id_tipo_contatto in (" + Def.CONTATTO_PERSONA_FISICA + "," + Def.CONTATTO_PENSIONE + ")";
		} else if (Def.NUM_UNO.equals(idTipo)) {
			whereTipo = " and id_tipo_contatto = " + Def.CONTATTO_PERSONA_FISICA;
		}

		String selectBase = "SELECT id_contatto, nome, cognome, rag_sociale, " +
				"provincia, id_provincia, email, telefono_1, cellulare, id_tipo_contatto, blacklist " +
				"FROM an_contatti ";

		final String queryStr;
		final Object[] params;

		if (strToSearch.length() >= 4) {
			// Usa FULLTEXT index per performance
			queryStr = selectBase +
					"WHERE MATCH(nome, cognome, rag_sociale) AGAINST (? IN BOOLEAN MODE) " +
					whereTipo +
					" ORDER BY nome, rag_sociale LIMIT 20";
			params = new Object[]{ "+" + strToSearch + "*" };
		} else {
			// Fallback LIKE per stringhe corte (sotto ft_min_word_len)
			String like = "%" + strToSearch.toUpperCase() + "%";
			queryStr = selectBase +
					"WHERE upper(CONCAT(ifnull(nome,''), ifnull(cognome,''), ifnull(rag_sociale,''))) LIKE ? " +
					whereTipo +
					" ORDER BY nome, rag_sociale LIMIT 20";
			params = new Object[]{ like };
		}

		try {
			return jdbcTemplate.query(queryStr, (rs, i) -> {
				ContattoAutocompleteDTO o = new ContattoAutocompleteDTO();
				o.setId_contatto(rs.getString("id_contatto"));
				o.setNome(rs.getString("nome"));
				o.setCognome(rs.getString("cognome"));
				o.setRag_sociale(rs.getString("rag_sociale"));
				o.setDesc_provincia(rs.getString("provincia"));
				o.setEmail(rs.getString("email"));
				o.setTelefono_1(rs.getString("telefono_1"));
				o.setCellulare(rs.getString("cellulare"));
				o.setId_tipo_contatto(rs.getString("id_tipo_contatto"));
				o.setBlacklist(rs.getString("blacklist"));
				return o;
			}, params);
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return Collections.emptyList();
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getContattiByTipoAndSearch(String strToSearch, List<String> tipiContatto) {
		List<Contatto> contatti;
		String whereTipo = "";
		if(!tipiContatto.isEmpty()) {
			whereTipo = " and id_tipo_contatto in (";
			for(String t : tipiContatto) {
				whereTipo += t + ",";
			}
			whereTipo = whereTipo.substring(0,whereTipo.length()-1) + ") ";
		}
		
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT id_contatto,nome,cognome,rag_sociale,comune,provincia,indirizzo,email,telefono_1,telefono_2,cellulare,note, " + 
				"account,NullIf(Date_Format(dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, id_tipo_contatto, blacklist, " +
				"num_civico, localita, indirizzo_completo, latitudine, longitudine, " +
				"cap, regione, id_comune, id_provincia, id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti"
				+ " WHERE upper(CONCAT(ifnull(nome,''),ifnull(cognome,''),ifnull(rag_sociale,''))) like (?) " + whereTipo + " order by nome, rag_sociale limit 30";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private List<Contatto> fillContatti(List<Contatto> contatti){
		for (Contatto contatto : contatti) {
			contatto.setQualifiche(getQualificheByID(contatto.getId_contatto()));
			contatto.setComune(amministrazioneDao.getComuneByID(contatto.getId_comune()));
			contatto.setProvincia(amministrazioneDao.getProvinciaByID(contatto.getId_provincia()));
			Regione r = amministrazioneDao.getRegioneByID(contatto.getId_regione());
			contatto.setRegione(r);
			if(r != null)contatto.setStato(r.getStato());
		}
		return contatti;
	}
	
	private Contatto fillContatto(Contatto contatto){
		contatto.setQualifiche(getQualificheByID(contatto.getId_contatto()));
		contatto.setComune(amministrazioneDao.getComuneByID(contatto.getId_comune()));
		contatto.setProvincia(amministrazioneDao.getProvinciaByID(contatto.getId_provincia()));
		Regione r = amministrazioneDao.getRegioneByID(contatto.getId_regione());
		contatto.setRegione(r);
		if(r != null)contatto.setStato(r.getStato());
		return contatto;
	}
	
	
	@Transactional(readOnly = true)
	public List<Proprietario> getProprietariByIDAnimale(String idAnimale) {
		List<Proprietario> contatti = new ArrayList<Proprietario>();

		String queryStr = "Select a.id_contatto,a.nome,a.cognome,a.rag_sociale,a.comune,a.provincia,a.indirizzo,a.email,a.telefono_1, " + 
							"	a.telefono_2,a.cellulare,a.blacklist,a.cap,a.regione,c.id_animale,c.dt_inserimento,c.corrente, " + 
							"   a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine " +
							"From an_contatti a Right Join an_animale_proprietari c On c.id_contatto_proprietario = a.id_contatto " + 
							"Where c.id_animale = ?";

		try{
			contatti = jdbcTemplate.query(queryStr, new ProprietarioRowMapper(), new Object[] { idAnimale });
			return contatti;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getPreaffidantiBySearch(String strToSearch) {
		List<Contatto> contatti;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " + 
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.cap, a.regione, a.id_comune, a.id_provincia, a.id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti a, an_r_qualifiche_contatto b"
				+ " WHERE a.id_contatto = b.id_contatto and b.id_qualifica = 4 and a.blacklist = 0 and upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like (?) order by nome, rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getFornitoriBySearch(String strToSearch) {
		List<Contatto> contatti;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " + 
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.cap, a.regione, a.id_comune, a.id_provincia, a.id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti a"
				+ " WHERE a.id_tipo_contatto != " + Def.CONTATTO_FORNITORE + " and a.blacklist = 0 and upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like (?) order by a.nome, a.rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });	
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getAdottantiADistanza(String strToSearch) {
		List<Contatto> contatti;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " +
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.cap, a.regione, a.id_comune, a.id_provincia, a.id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti a, an_r_qualifiche_contatto b" +
				" WHERE a.id_contatto = b.id_contatto and b.id_qualifica = 5 and a.blacklist = 0 " +
				" and b.id_contatto not in (select id_contatto from md_adottanti) " +
				" and upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like upper(?) order by nome, rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getStallanti(String strToSearch) {
		List<Contatto> contatti;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " +
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.cap, a.regione, a.id_comune, a.id_provincia, a.id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti a, an_r_qualifiche_contatto b"
				+ " WHERE a.id_contatto = b.id_contatto and b.id_qualifica = 1 and a.blacklist = 0 and upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like (?) order by nome, rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getRaccoglitoriBySearch(String strToSearch) {
		List<Contatto> contatti;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " + 
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.cap, a.regione, a.id_comune, a.id_provincia, a.id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti a, an_r_qualifiche_contatto b"
				+ " WHERE a.id_contatto = b.id_contatto and b.id_qualifica = 6 and a.blacklist = 0 and upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like (?) order by nome, rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getPuntiRaccoltaBySearch(String strToSearch) {
		List<Contatto> contatti;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " + 
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.regione, a.id_comune, a.id_provincia, a.id_regione, a.orario_apertura " +
				"FROM an_contatti a"
				+ " WHERE a.id_tipo_contatto = " + Def.CONTATTO_PUNTO_VENDITA + " and a.blacklist = 0 and upper(ifnull(a.rag_sociale,'')) like (?) order by rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new PuntoRaccoltaRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return Contatto
	 */
	@Transactional(readOnly = true)
	public Contatto getPuntoRaccoltaByID(String id) {
		Contatto contatto;
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " + 
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.regione, a.id_comune, a.id_provincia, a.id_regione, a.orario_apertura " +
				"FROM an_contatti a"
				+ " WHERE a.id_tipo_contatto = " + Def.CONTATTO_PUNTO_VENDITA + " and a.blacklist = 0 and a.id_contatto = ?";

		try{
			contatto =  jdbcTemplate.queryForObject(queryStr, new PuntoRaccoltaRowMapper(), new Object[] { id });
			
			contatto = fillContatto(contatto);
			
			return contatto;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idContatto
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getRaccoglitoriByCentro(String idContatto) {
		List<Contatto> contatti = new ArrayList<Contatto>();
		String query = "SELECT distinct a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1, a.telefono_2, a.cellulare, a.note, " + 
				"				a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " + 
				"				a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"				a.regione, a.id_comune, a.id_provincia, a.id_regione, a.orario_apertura " +
				"  FROM  rc_evento c, rc_r_evento_contatti b, an_contatti a " + 
				"  WHERE b.id_evento = c.id_evento AND b.id_contatto = a.id_contatto and c.id_punto_raccolta = ? and (a.email is not null and trim(a.email) <> '') ";

		contatti =  jdbcTemplate.query(query, new PuntoRaccoltaRowMapper(), new Object[] {idContatto });
		contatti = fillContatti(contatti);

		return contatti;
	}
	
	/**
	 * @param strToSearch
	 * @return List<Contatto>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getVolontarieBySearch(String strToSearch) {
		List<Contatto> contatti;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_contatto, a.nome, a.cognome, a.rag_sociale, a.comune, a.provincia, a.indirizzo, a.email, a.telefono_1,a.telefono_2, a.cellulare, a.note, " + 
				"a.account, NullIf(Date_Format(a.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, a.id_tipo_contatto, a.blacklist, " + 
				"a.num_civico, a.localita, a.indirizzo_completo, a.latitudine, a.longitudine, " +
				"a.cap, a.regione, a.id_comune, a.id_provincia, a.id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti a, an_r_qualifiche_contatto b"
				+ " WHERE a.id_contatto = b.id_contatto and b.id_qualifica = 2 and a.blacklist = 0 and upper(CONCAT(ifnull(a.nome,''),ifnull(a.cognome,''),ifnull(a.rag_sociale,''))) like (?) order by nome, rag_sociale";

		try{
			contatti =  jdbcTemplate.query(queryStr, new ContattoRowMapper(), new Object[] { strToSearch.toUpperCase().trim() });
			contatti = fillContatti(contatti);
			
			return contatti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	@Transactional(readOnly = true)
	public Contatto getByID(String idContatto) {
		return getByID(idContatto,false);
	}
	
	@Transactional(readOnly = true)
	public Contatto getByID(String idContatto, Boolean sintetico ) {
		Contatto contatto ;
		String queryStr = "SELECT id_contatto,nome,cognome,rag_sociale,comune,provincia,indirizzo,email,telefono_1,telefono_2,cellulare,note, " + 
				"account,NullIf(Date_Format(dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, id_tipo_contatto, blacklist, " + 
				"num_civico, localita, indirizzo_completo, latitudine, longitudine, " +
				"cap, regione, id_comune, id_provincia, id_regione, cod_fiscale, nato_a, nullif(DATE_FORMAT(data_nascita, '%d/%m/%Y'),'') data_nascita, id_tipo_documento, num_documento, orario_apertura " +
				"FROM an_contatti"
				+ " WHERE id_contatto = ?";

		try{
			contatto =  jdbcTemplate.queryForObject(queryStr, new ContattoRowMapper(), new Object[] { idContatto });
			
			contatto = fillContatto(contatto);
			
			if(!sintetico) {
				contatto.setAttivita(getAttivitaContattoByID(idContatto));
				
				contatto.setDocumenti(documentoDao.getDocumentiByIDContatto(idContatto));
			}
			return contatto;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param 
	 * @return List<TipoContatto>
	 */
	@Transactional(readOnly = true)
	public List<TipoContatto> getTipoContatti() {
		
		String queryStr = "SELECT id_tipo_contatto, tipo_contatto " + 
				"FROM an_x_tipo_contatto"
				+ " ORDER BY tipo_contatto";

		try{
			return jdbcTemplate.query(queryStr, new TipoContattoRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Qualifica>
	 */
	@Transactional(readOnly = true)
	public List<Qualifica> getQualifiche() {
		
		String queryStr = "SELECT id_qualifica, qualifica " + 
				"FROM an_x_qualifica"
				+ " ORDER BY qualifica";

		try{
			return jdbcTemplate.query(queryStr, new QualificaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Qualifica>
	 */
	@Transactional(readOnly = true)
	public List<Qualifica> getQualificheByID(String idContatto) {
		
		String queryStr = "SELECT b.id_qualifica, b.qualifica " + 
				"FROM an_r_qualifiche_contatto a, an_x_qualifica b WHERE a.id_qualifica=b.id_qualifica and  a.id_contatto = ?";

		try{
			return jdbcTemplate.query(queryStr,new QualificaRowMapper(), new Object[] { idContatto } );

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param contatto
	 * @return
	 */
	@Transactional()
	public String saveOrUpdate(Contatto contatto) {
		String idContatto = "";

		if (contatto.getId_contatto() == null) {
			idContatto = save(contatto);
			logger.info("Inserito contatto: " + idContatto + " " + contatto.getDescrizione() );
		} else {
			idContatto = update(contatto);
		}
		
		return idContatto;
	}
	
	/**
	 * @param contatto
	 * @return
	 */
	@Transactional()
	private String save(Contatto contatto) {
		
		if(contatto.getBlacklist() == null) {contatto.setBlacklist("0");}
		String query = "INSERT INTO an_contatti " + 
				"(nome,cognome,rag_sociale,comune,provincia,indirizzo,email,telefono_1,telefono_2,cellulare,note, " + 
				"account,id_tipo_contatto, blacklist, cap, regione, id_comune, id_provincia, id_regione, cod_fiscale, nato_a, data_nascita, id_tipo_documento, num_documento, orario_apertura," +
				"num_civico, localita, indirizzo_completo, latitudine, longitudine) " + 
				"VALUES " + 
				"(:nome , :cognome , :rag_sociale , :comune.nome , :provincia.nome , :indirizzo , :email , :telefono_1 , :telefono_2, :cellulare , :note , " +
				":account , :id_tipo_contatto, :blacklist, :cap, :regione.nome, :comune.id, :provincia.id, :regione.id, upper(:cod_fiscale), :nato_a, str_to_date(nullif(:data_nascita,''), '%d/%m/%Y'), :id_tipo_documento, upper(:num_documento), :orario_apertura," +
				":num_civico, :localita, :indirizzo_completo, :latitudine, :longitudine )";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		if(contatto.getComune() == null) {
			query = "INSERT INTO an_contatti " + 
					"(nome, cognome, rag_sociale, indirizzo, cap, email, telefono_1, telefono_2, cellulare, note, account,id_tipo_contatto, blacklist,  cod_fiscale, nato_a, data_nascita, id_tipo_documento, num_documento, orario_apertura," +
					"num_civico, localita, indirizzo_completo, latitudine, longitudine) " +  
					"VALUES " + 
					"(:nome , :cognome , :rag_sociale , :indirizzo , :cap, :email , :telefono_1 , :telefono_2, :cellulare , :note , :account , :id_tipo_contatto, :blacklist,  upper(:cod_fiscale), :nato_a, str_to_date(nullif(:data_nascita,''), '%d/%m/%Y'), :id_tipo_documento, upper(:num_documento), :orario_apertura," +
					":num_civico, :localita, :indirizzo_completo, :latitudine, :longitudine )";
		}
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(contatto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_contatto" });

		if(Def.NUM_UNO.equals(contatto.getId_tipo_contatto())) {
			Contatto newRef = getByID(String.valueOf(keyHolder.getKey()),true);
			newRef.setQualifiche(contatto.getQualifiche());
			saveQualifiche(newRef);
		}
		
		return String.valueOf(keyHolder.getKey());
	}

	/**
	 * @param contatto
	 * @return
	 */
	@Transactional()
	private String update(Contatto contatto) {

		String query = "UPDATE an_contatti " + 
				"SET " + 
				"nome = :nome," + 
				"cognome = :cognome," + 
				"rag_sociale = :rag_sociale," + 
				"comune = :comune.nome," + 
				"provincia = :provincia.nome," + 
				"indirizzo = :indirizzo," + 
				"cap = :cap," + 
				"num_civico = :num_civico," +  
				"localita = :localita," +  
				"indirizzo_completo = :indirizzo_completo," +  
				"latitudine = :latitudine," +  
				"longitudine = :longitudine," +
				"email = :email," + 
				"telefono_1 = :telefono_1," + 
				"telefono_2 = :telefono_2," + 
				"cellulare = :cellulare," + 
				"note = :note," + 
				"account = :account," + 
				"id_tipo_contatto = :id_tipo_contatto," + 
				"blacklist = :blacklist," +
				"regione = :regione.nome," +
				"id_comune = :comune.id," +
				"id_provincia = :provincia.id," +
				"id_regione = :regione.id," +
				"cod_fiscale = upper(:cod_fiscale)," +
				"id_tipo_documento = :id_tipo_documento," +
				"num_documento = upper(:num_documento)," +
				"nato_a = :nato_a, " +
				"orario_apertura = :orario_apertura, " +
				"data_nascita = str_to_date(nullif(:data_nascita,''), '%d/%m/%Y') " +
				" WHERE id_contatto = :id_contatto";

		if(contatto.getComune() == null) {   
			query ="UPDATE an_contatti " + 
					"SET " + 
					"nome = :nome," + 
					"cognome = :cognome," + 
					"rag_sociale = :rag_sociale," + 
					"indirizzo = :indirizzo," + 
					"cap = :cap," + 
					"num_civico = :num_civico," +  
					"localita = :localita," +  
					"indirizzo_completo = :indirizzo_completo," +  
					"latitudine = :latitudine," +  
					"longitudine = :longitudine," + 
					"email = :email," + 
					"telefono_1 = :telefono_1," + 
					"telefono_2 = :telefono_2," + 
					"cellulare = :cellulare," + 
					"note = :note," + 
					"account = :account," + 
					"id_tipo_contatto = :id_tipo_contatto," + 
					"blacklist = :blacklist, " +
					"cod_fiscale = upper(:cod_fiscale)," +
					"id_tipo_documento = :id_tipo_documento," +
					"num_documento = upper(:num_documento)," +
					"nato_a = :nato_a, " +
					"orario_apertura = :orario_apertura, " +
					"data_nascita = str_to_date(nullif(:data_nascita,''), '%d/%m/%Y') " +
					" WHERE id_contatto = :id_contatto";
		}
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(contatto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		saveQualifiche(contatto);
		
		return contatto.getId_contatto();
	}
	
	/**
	 * @param idContatto
	 * @return
	 */
	@Transactional()
	public String deleteByID(String idContatto) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM an_contatti WHERE id_contatto = ?";
		jdbcTemplate.update(query, new Object[] { idContatto });
		return Def.STR_OK;
	}
	
	
	
	/**
	 * @param qualifica
	 * @return idQualifica
	 */
	@Transactional()
	public String saveOrUpdateQualifica(Qualifica qualifica) throws SQLIntegrityConstraintViolationException {
		String idMerce = "";

		if (qualifica.getId_qualifica() == null) {
			idMerce = save(qualifica);
			logger.info("Inserito turno: " + idMerce + " " + qualifica.getId_qualifica() );
		} else {
			idMerce = update(qualifica);
		}
		
		return idMerce;
	}
	
	/**
	 * @param qualifica
	 * @return idQualifica
	 */
	@Transactional()
	private String save(Qualifica qualifica) {

		String query = "INSERT INTO an_x_qualifica (qualifica) VALUES(:qualifica)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(qualifica);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder);

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param qualifica
	 * @return idQualifica
	 */
	@Transactional()
	private String update(Qualifica qualifica) {

		String query = "UPDATE an_x_qualifica " + 
				"SET qualifica = :qualifica " + 
				"WHERE id_qualifica = :id_qualifica";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(qualifica);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return qualifica.getId_qualifica();
	}
	
	/**
	 * @param contatto
	 * @return
	 */
	@Transactional()
	private void saveQualifiche(Contatto contatto) {
		String query = "DELETE FROM an_r_qualifiche_contatto WHERE id_contatto = ?";
		jdbcTemplate.update(query, contatto.getId_contatto());
		if(Def.NUM_UNO.equals(contatto.getId_tipo_contatto())) {
			for (Qualifica q : contatto.getQualifiche()) {
				if(q != null) {
					query = "INSERT INTO an_r_qualifiche_contatto (id_contatto, id_qualifica) VALUES  (?, ?)";
					jdbcTemplate.update(query, contatto.getId_contatto(), q.getId_qualifica() );
				}
			}
		}
	}
	
	/**
	 * @param idQualifica
	 * @return
	 */
	@Transactional()
	public String deleteQualificaByID(String idQualifica) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM an_x_qualifica WHERE id_qualifica = ?";
		jdbcTemplate.update(query, new Object[] { idQualifica });
		return Def.STR_OK;
	}
	
	private static class ContattoRowMapper extends BaseRowMapper<Contatto> {
		public ContattoRowMapper() {
		}		
		@Override
		public Contatto mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Contatto o = new Contatto();
			o.setNome(rs.getString("nome"));
			o.setAccount(rs.getString("account"));
			o.setCellulare(rs.getString("cellulare"));
			o.setCognome(rs.getString("cognome"));
			o.setDesc_comune(rs.getString("comune"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setEmail(rs.getString("email"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setIndirizzo(rs.getString("indirizzo"));
			o.setNote(rs.getString("note"));
			o.setDesc_provincia(rs.getString("provincia"));
			o.setDesc_regione(rs.getString("regione"));
			o.setRag_sociale(rs.getString("rag_sociale"));
			o.setTelefono_1(rs.getString("telefono_1"));
			o.setTelefono_2(rs.getString("telefono_2"));
			o.setId_tipo_contatto(rs.getString("id_tipo_contatto"));
			o.setBlacklist(rs.getString("blacklist"));
			o.setId_comune(rs.getString("id_comune"));
			o.setId_provincia(rs.getString("id_provincia"));
			o.setId_regione(rs.getString("id_regione"));
			o.setCod_fiscale(rs.getString("cod_fiscale"));
			o.setData_nascita(rs.getString("data_nascita"));
			o.setNato_a(rs.getString("nato_a"));
			o.setId_tipo_documento(rs.getString("id_tipo_documento"));
			o.setNum_documento(rs.getString("num_documento"));
			o.setOrario_apertura(rs.getString("orario_apertura"));
			o.setCap(rs.getString("cap"));
			
			o.setLatitudine(rs.getString("latitudine"));
			o.setLongitudine(rs.getString("longitudine"));
			o.setLocalita(rs.getString("localita"));
			o.setNum_civico(rs.getString("num_civico"));
			o.setIndirizzo_completo(rs.getString("indirizzo_completo"));
			return o;
		}
	}
	
	private static class ProprietarioRowMapper extends BaseRowMapper<Proprietario> {
		public ProprietarioRowMapper() {
		}		
		@Override
		public Proprietario mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Proprietario o = new Proprietario();
			o.setId_contatto(rs.getString("id_contatto"));
			o.setNome(rs.getString("nome"));
			o.setCognome(rs.getString("cognome"));
			o.setRag_sociale(rs.getString("rag_sociale"));
			o.setDesc_comune(rs.getString("comune"));
			o.setDesc_provincia(rs.getString("provincia"));
			o.setIndirizzo(rs.getString("indirizzo"));
			o.setEmail(rs.getString("email"));
			o.setTelefono_1(rs.getString("telefono_1"));
			o.setTelefono_2(rs.getString("telefono_2"));
			o.setCellulare(rs.getString("cellulare"));
			o.setBlacklist(rs.getString("blacklist"));
			o.setCap(rs.getString("cap"));
			o.setDesc_regione(rs.getString("regione"));
			o.setDt_da(rs.getString("dt_inserimento"));
			o.setCorrente(rs.getString("corrente"));
			o.setLatitudine(rs.getString("latitudine"));
			o.setLongitudine(rs.getString("longitudine"));
			o.setLocalita(rs.getString("localita"));
			o.setNum_civico(rs.getString("num_civico"));
			o.setIndirizzo_completo(rs.getString("indirizzo_completo"));
			return o;
		}
	}
	
	/**
	 * @param idContatto
	 * @return List<AttivitaContatto>
	 */
	@Transactional(readOnly = true)
	public List<AttivitaContatto> getAttivitaContattoByID(String idContatto) {
		String queryStr = "SELECT id_contatto, soggetto, evento, data_da, data_a from v_contatti_attivita where id_contatto = ? order by evento,  STR_TO_DATE(data_da,'%d/%m/%Y') desc, soggetto";
		try{
			return  jdbcTemplate.query(queryStr, new AttivitaContattoRowMapper(), new Object[] { idContatto });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	private static class PuntoRaccoltaRowMapper extends BaseRowMapper<Contatto> {
		public PuntoRaccoltaRowMapper() {
		}		
		@Override
		public Contatto mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Contatto o = new Contatto();
			o.setNome(rs.getString("nome"));
			o.setAccount(rs.getString("account"));
			o.setCellulare(rs.getString("cellulare"));
			o.setCognome(rs.getString("cognome"));
			o.setDesc_comune(rs.getString("comune"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setEmail(rs.getString("email"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setIndirizzo(rs.getString("indirizzo"));
			o.setNote(rs.getString("note"));
			o.setDesc_provincia(rs.getString("provincia"));
			o.setDesc_regione(rs.getString("regione"));
			o.setRag_sociale(rs.getString("rag_sociale"));
			o.setTelefono_1(rs.getString("telefono_1"));
			o.setTelefono_2(rs.getString("telefono_2"));
			o.setId_tipo_contatto(rs.getString("id_tipo_contatto"));
			o.setBlacklist(rs.getString("blacklist"));
			o.setId_comune(rs.getString("id_comune"));
			o.setId_provincia(rs.getString("id_provincia"));
			o.setId_regione(rs.getString("id_regione"));
			o.setOrario_apertura(rs.getString("orario_apertura"));
			o.setLatitudine(rs.getString("latitudine"));
			o.setLongitudine(rs.getString("longitudine"));
			o.setLocalita(rs.getString("localita"));
			o.setNum_civico(rs.getString("num_civico"));
			o.setIndirizzo_completo(rs.getString("indirizzo_completo"));
			return o;
		}
	}
	
	private static class TipoContattoRowMapper extends BaseRowMapper<TipoContatto> {
		public TipoContattoRowMapper() {
		}		
		@Override
		public TipoContatto mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoContatto o = new TipoContatto();
			o.setId_tipo_contatto(rs.getString("id_tipo_contatto"));
			o.setTipo_contatto(rs.getString("tipo_contatto"));
			return o;
		}
	}
	
	private static class QualificaRowMapper extends BaseRowMapper<Qualifica> {
		public QualificaRowMapper() {
		}		
		@Override
		public Qualifica mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Qualifica o = new Qualifica();
			o.setId_qualifica(rs.getString("id_qualifica"));
			o.setQualifica(rs.getString("qualifica"));
			return o;
		}
	}
	
	private static class AttivitaContattoRowMapper extends BaseRowMapper<AttivitaContatto> {
		public AttivitaContattoRowMapper() {
		}		
		@Override
		public AttivitaContatto mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			AttivitaContatto o = new AttivitaContatto();
			o.setId_contatto(rs.getString("id_contatto"));
			o.setEvento(rs.getString("evento"));
			o.setData_a(rs.getString("data_a"));
			o.setData_da(rs.getString("data_da"));
			o.setSoggetto(rs.getString("soggetto"));
			return o;
		}
	}

}
