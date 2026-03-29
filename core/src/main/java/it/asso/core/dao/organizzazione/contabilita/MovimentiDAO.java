package it.asso.core.dao.organizzazione.contabilita;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.organizzazione.RicercaDTO;
import it.asso.core.model.organizzazione.TotaliMovimento;
import it.asso.core.model.organizzazione.contabilita.*;
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
import java.util.ArrayList;
import java.util.List;

@Repository
public class MovimentiDAO{

    private static final Logger logger = LoggerFactory.getLogger(MovimentiDAO.class);
    private final JdbcTemplate jdbcTemplate;

    public MovimentiDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param id
	 * @return Movimento
	 */
	@Transactional(readOnly = true)
	public Movimento getByID(String id) {
		Movimento m = null;
		String queryStr = "SELECT m.id_movimento, m.id_tipo_movimento, " + 
				"m.id_organizzazione, m.codice, m.account, m.importo, " + 
				"nullif(DATE_FORMAT(m.dt_operazione,'%d/%m/%Y'),'') dt_operazione, " + 
				"nullif(DATE_FORMAT(m.dt_inserimento,'%d/%m/%Y %H:%i:%s'),'') dt_inserimento, " + 
				"nullif(DATE_FORMAT(m.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " +
				"m.id_destinazione, m.id_tipo_destinazione,  m.id_cc, m.id_causale, m.note, m.id_contatto, m.girofondo " + 
				"FROM org_movimenti m WHERE m.id_movimento = ?";
		try{
			m =  jdbcTemplate.queryForObject(queryStr, new MovimentoRowMapper(), new Object[] { id });
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param id, code
	 * @return Movimento
	 */
	@Transactional(readOnly = true)
	public Movimento getByID(String id, String code) {
		Movimento m = null;
		String queryStr = "SELECT m.id_movimento, m.id_tipo_movimento,  " + 
				"m.id_organizzazione, m.codice, m.account, m.importo,  " + 
				"nullif(DATE_FORMAT(m.dt_operazione,'%d/%m/%Y'),'') dt_operazione, " + 
				"nullif(DATE_FORMAT(m.dt_inserimento,'%d/%m/%Y %H:%i:%s'),'') dt_inserimento, " + 
				"nullif(DATE_FORMAT(m.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " +
				"m.id_destinazione, m.id_tipo_destinazione,  m.id_cc, m.id_causale, m.note, m.id_contatto, m.girofondo " + 
				"FROM org_movimenti m WHERE m.id_movimento != ? and m.codice = ?";
		try{
			m =  jdbcTemplate.queryForObject(queryStr, new MovimentoRowMapper(), new Object[] { id, code });
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param idOrganizzazione
	 * @return List<Movimento>
	 */
	@Transactional(readOnly = true)
	public List<Movimento> getByAnno(String anno, String idOrganizzazione) {
		List<Movimento> m = new ArrayList<Movimento>();
		String queryStr = "SELECT m.id_movimento, m.id_tipo_movimento,  " + 
				"m.id_organizzazione, m.codice, m.account, m.importo, " + 
				"nullif(DATE_FORMAT(m.dt_operazione,'%d/%m/%Y'),'') dt_operazione, " + 
				"nullif(DATE_FORMAT(m.dt_inserimento,'%d/%m/%Y %H:%i:%s'),'') dt_inserimento, " + 
				"nullif(DATE_FORMAT(m.dt_aggiornamento,'%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " +
				"m.id_destinazione, m.id_tipo_destinazione,  m.id_cc, m.id_causale, m.note, m.id_contatto. m.girofondo " + 
				"FROM org_movimenti m WHERE m.id_organizzazione = ? and year(m.dt_operazione) = ? order by m.dt_operazione";
		try{
			m =  jdbcTemplate.query(queryStr, new MovimentoRowMapper(), new Object[] { anno, idOrganizzazione });
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Movimento>
	 */
	@Transactional(readOnly = true)
	public List<MovimentoRestrict> getListByAnno(RicercaDTO ricerca, String idOrganizzazione) {
		List<MovimentoRestrict> m = new ArrayList<MovimentoRestrict>();
		
		String forPagination = "";
		
		if(!"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		
		if(ricerca.gettMovimento() == null || "".equals(ricerca.gettMovimento())) {
			ricerca.settMovimento(Def.STR_PERCENTAGE);
		}
		
		if(ricerca.gettDestinazione() == null || "".equals(ricerca.gettDestinazione())) {
			ricerca.settDestinazione(Def.STR_PERCENTAGE);
		}

		
		String queryStr = "Select"
				+ "    m.id_movimento,"
				+ "    m.id_tipo_movimento,"
				+ "    m.codice,"
				+ "    nullif(DATE_FORMAT(m.dt_operazione,'%d/%m/%Y'),'') dt_operazione,"
				+ "    m.importo,"
				+ "    tipo_movimento.descrizione tMovimento,"
				+ "    causali.descrizione causale,"
				+ "    dest.descrizione tDestinazione, dest.id_destinazione, "
				+ "    tipo_dest.descrizione tipoDestinazione, m.id_contatto, m.id_causale, m.id_tipo_destinazione, m.girofondo "
				+ " From"
				+ "    org_movimenti m Left Join"
				+ "    org_x_tipo_movimenti tipo_movimento On m.id_tipo_movimento = tipo_movimento.id_tipo_movimento Left Join"
				+ "    org_x_causali causali On m.id_causale = causali.id_causale Left Join"
				+ "    org_x_destinazione dest On m.id_destinazione = dest.id_destinazione Left Join"
				+ "    org_x_tipo_destinazione tipo_dest On tipo_dest.id_tipo_destinazione = m.id_tipo_destinazione"
				+ " Where"
				+ "    Year(m.dt_operazione) = ? And"
				+ "    m.id_organizzazione = ? And"
				+ "    tipo_movimento.id_tipo_movimento like(?) And"
				+ "    dest.id_destinazione like(?) "
				+ " Order By"
				+ "    m.dt_operazione Desc,"
				+ "    m.codice Desc "  + forPagination;
		try{
			m =  jdbcTemplate.query(queryStr, new MovimentoRestrictRowMapper() , new Object[] { ricerca.getAnno(), idOrganizzazione, ricerca.gettMovimento(), ricerca.gettDestinazione() });
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Movimento>
	 */
	@Transactional(readOnly = true)
	public int getCountBySearch(RicercaDTO ricerca, String idOrganizzazione) {
		int numMovimenti = 0;
		if(ricerca.gettMovimento() == null || "".equals(ricerca.gettMovimento())) {
			ricerca.settMovimento(Def.STR_PERCENTAGE);
		}
		
		if(ricerca.gettDestinazione() == null || "".equals(ricerca.gettDestinazione())) {
			ricerca.settDestinazione(Def.STR_PERCENTAGE);
		}
		
		String queryStr = "Select count(distinct m.id_movimento) "
				+ " From"
				+ "    org_movimenti m Left Join"
				+ "    org_x_tipo_movimenti tipo_movimento On m.id_tipo_movimento = tipo_movimento.id_tipo_movimento Left Join"
				+ "    org_x_causali causali On m.id_causale = causali.id_causale Left Join"
				+ "    org_x_destinazione dest On m.id_destinazione = dest.id_destinazione Left Join"
				+ "    org_x_tipo_destinazione tipo_dest On tipo_dest.id_tipo_destinazione = m.id_tipo_destinazione"
				+ " Where"
				+ "    Year(m.dt_operazione) = ? And"
				+ "    m.id_organizzazione = ? And"
				+ "    tipo_movimento.id_tipo_movimento like(?) And"
				+ "    dest.id_destinazione like(?) ";
		try{
			numMovimenti =  jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] {ricerca.getAnno(), idOrganizzazione, ricerca.gettMovimento(), ricerca.gettDestinazione()});			
			return numMovimenti;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return numMovimenti;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Movimento>
	 */
	@Transactional(readOnly = true)
	public List<TotaliMovimento> getTotali(RicercaDTO ricerca, String idOrganizzazione) {
		List<TotaliMovimento> m = new ArrayList<TotaliMovimento>();

		
		String queryStr = "Select "
				+ "    m.id_tipo_movimento, "
				+ "    sum(m.importo) importo, "
				+ "    null tipo "
				+ "From "
				+ "    org_movimenti m Left Join "
				+ "    org_x_tipo_movimenti tipo_movimento On m.id_tipo_movimento = tipo_movimento.id_tipo_movimento Left Join "
				+ "    org_x_causali causali On m.id_causale = causali.id_causale Left Join "
				+ "    org_x_destinazione dest On m.id_destinazione = dest.id_destinazione Left Join "
				+ "    org_x_tipo_destinazione tipo_dest On tipo_dest.id_tipo_destinazione = m.id_tipo_destinazione "
				+ "Where "
				+ "    Year(m.dt_operazione) = ? And "
				+ "    m.id_organizzazione = ?"
				+ "Group by m.id_tipo_movimento "
				+ "union "
				+ "Select "
				+ "    m.id_tipo_movimento, "
				+ "    Sum(m.importo) importo, "
				+ "    dest.id_destinazione "
				+ "From "
				+ "    org_movimenti m Left Join "
				+ "    org_x_tipo_movimenti tipo_movimento On m.id_tipo_movimento = tipo_movimento.id_tipo_movimento Left Join "
				+ "    org_x_causali causali On m.id_causale = causali.id_causale Left Join "
				+ "    org_x_destinazione dest On m.id_destinazione = dest.id_destinazione Left Join "
				+ "    org_x_tipo_destinazione tipo_dest On tipo_dest.id_tipo_destinazione = m.id_tipo_destinazione "
				+ "Where "
				+ "    Year(m.dt_operazione) = ? And "
				+ "    m.id_organizzazione = ? "
				+ "Group By "
				+ "    m.id_tipo_movimento, "
				+ "    dest.id_destinazione";
		try{
			m =  jdbcTemplate.query(queryStr, new TotaliRowMapper(), new Object[] {ricerca.getAnno(), idOrganizzazione, ricerca.getAnno(), idOrganizzazione});			
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	
	/**
	 * @param search
	 * @return List<String>
	 */
	@Transactional(readOnly = true)
	public List<String> getListDestinatari(String search) {
		List<String> m = new ArrayList<String>();
		String queryStr = "Select distinct destinatario from org_movimenti where upper(destinatario) like ('%" + search.toUpperCase() + "%') order by destinatario" ;
		try{
			m =  jdbcTemplate.queryForList(queryStr, String.class);
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param id
	 * @return List<Fattura>
	 */
	@Transactional(readOnly = true)
	public List<Fattura> getFattureMovimentoByID(String id) {
		List<Fattura> o = new ArrayList<Fattura>();
		String queryStr = "Select "
				+ "    f.id_fattura, "
				+ "    f.tipo_fattura, "
				+ "    f.codice, "
				+ "    f.numero, "
				+ "    f.id_contatto, "
				+ "    NullIf(Date_Format(f.dt_emissione,'%d/%m/%Y'), '') dt_emissione, "
				+ "    NullIf(Date_Format(f.dt_scadenza,'%d/%m/%Y'), '') dt_scadenza, "
				+ "    f.imponibile, "
				+ "    f.iva, "
				+ "    f.importo, "
				+ "    Concat(a.rag_sociale, IfNull(Concat(' (', a.cognome, ' ', a.nome, ') '), ''), IfNull(Concat(' - Piva/CF ', "
				+ "    a.cod_fiscale), '')) contatto "
				+ "From "
				+ "    org_fatture f Inner Join "
				+ "    an_contatti a On f.id_contatto = a.id_contatto Inner Join "
				+ "    org_movimenti_fatture mf On mf.id_fattura = f.id_fattura "
				+ "Where "
				+ "    f.id_fattura In (Select "
				+ "         If(org_movimenti_fatture.id_fattura Is Null, -1, org_movimenti_fatture.id_fattura) "
				+ "     From "
				+ "         org_movimenti_fatture) "
				+ "         and mf.id_movimento = ? "
				+ "Order By "
				+ "    f.codice Desc";
		try{
			o =  jdbcTemplate.query(queryStr, new FatturaSimpleRowMapper(), new Object[] { id });
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	
	/**
	 * @param m
	 * @return
	 */
	@Transactional()
	public String saveOrUpdate(Movimento m) {
		String idMovimento = "";

		if (m.getId_movimento() == null) {
			idMovimento = save(m);
		} else {
			idMovimento = update(m);
		}

		return idMovimento;
	}

	/**
	 * @param idMovimento, idFattura
	 * @return
	 */
	@Transactional()
	public String saveMovimentoFattura(String idMovimento, String idFattura) {
		String query = "insert into org_movimenti_fatture (id_movimento, id_fattura) values (?,?)";
		jdbcTemplate.update(query, new Object[] { idMovimento, idFattura });

		return Def.STR_OK;
	}
	
	/**
	 * @param idMovimento
	 * @return
	 */
	@Transactional()
	public String deleteFattureByIDMovimento(String idMovimento) {
		String query = "delete from  org_movimenti_fatture where id_movimento = ?";
		jdbcTemplate.update(query, new Object[] { idMovimento });
		return Def.STR_OK;
	}
	
	/**
	 * @param id
	 * @return TipoMovimento
	 */
	@Transactional(readOnly = true)
	public List<DettaglioMovimento> getDettaglioMovimentoByID(String id) {
		List<DettaglioMovimento> m = null;
		String queryStr = "SELECT id_movimento_dettaglio, id_movimento, id_cr_sottovoce, importo FROM org_movimenti_dettaglio where id_movimento = ?";
		try{
			m =  jdbcTemplate.query(queryStr, new DettaglioMovimentoRowMapper(), new Object[] { id });
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param dettaglio
	 * @return idDettaglioMovimento
	 */
	@Transactional()
	public String saveOrUpdateDettaglioMovimento(DettaglioMovimento dettaglio) {
		String idDettaglioMovimento = "";

		if (dettaglio.getId_movimento_dettaglio() == null) {
			idDettaglioMovimento = saveDettaglioMovimento(dettaglio);
		} else {
			idDettaglioMovimento = updateDettaglioMovimento(dettaglio);
		}

		return idDettaglioMovimento;
	}
	
	/**
	 * @param dettaglio
	 * @return id
	 */
	@Transactional()
	public String saveDettaglioMovimento(DettaglioMovimento dettaglio) {
		final String query = "INSERT INTO org_movimenti_dettaglio (id_movimento, id_cr_sottovoce, importo) " + 
				"VALUES (:id_movimento, :id_cr_sottovoce,  :importo)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(dettaglio);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_movimento_dettaglio" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param dettaglio
	 * @return id
	 */
	@Transactional()
	public String updateDettaglioMovimento(DettaglioMovimento dettaglio) {
		final String query = "UPDATE org_movimenti_dettaglio " + 
				"SET " + 
				"id_movimento = :id_movimento,  " + 
				"id_cr_sottovoce = :id_cr_sottovoce, " + 
				"importo = :importo " +
				"WHERE id_movimento_dettaglio = :id_movimento_dettaglio";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(dettaglio);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return dettaglio.getId_movimento_dettaglio();
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteDettaglioMovimentoByID(String id) {
		String query = "DELETE FROM org_movimenti_dettaglio WHERE id_movimento_dettaglio = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteDettaglioMovimentoByIDMovimento(String id) {
		String query = "DELETE FROM org_movimenti_dettaglio WHERE id_movimento = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	/**
	 * @param m
	 * @return
	 */
	@Transactional()
	public String save(Movimento m) {
		final String query = "INSERT INTO org_movimenti (id_tipo_movimento, id_organizzazione, codice, dt_operazione, account, importo, " +
				"id_destinazione, id_tipo_destinazione, id_cc, id_causale, note, id_contatto, girofondo ) " + 
				"VALUES (:id_tipo_movimento, :id_organizzazione, :codice, str_to_date(nullif(:dt_operazione,''), '%d/%m/%Y'), :account, :importo, " +
				":id_destinazione, :id_tipo_destinazione, :id_cc,:id_causale, :note, :id_contatto, :girofondo)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(m);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_movimento" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param m
	 * @return
	 */
	@Transactional()
	public String update(Movimento m) {

		final String query = "UPDATE org_movimenti " + 
				"SET " + 
				"id_tipo_movimento = :id_tipo_movimento,  " + 
				"id_organizzazione = :id_organizzazione, " + 
				"codice = :codice, " + 
				"dt_operazione = str_to_date(nullif(:dt_operazione,''), '%d/%m/%Y'), " + 
				"account = :account, importo = :importo, " + 
				"id_causale = :id_causale, note = :note,  id_contatto = :id_contatto, " +
				"id_destinazione = :id_destinazione, id_tipo_destinazione = :id_tipo_destinazione, id_cc = :id_cc, girofondo = :girofondo " +
				"WHERE id_movimento = :id_movimento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(m);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return m.getId_movimento();
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteByID(String id) {
		String query = "DELETE FROM org_movimenti_fatture WHERE id_movimento = ?";
		jdbcTemplate.update(query, new Object[] { id });
		query = "DELETE FROM org_movimenti_dettaglio WHERE id_movimento = ?";
		jdbcTemplate.update(query, new Object[] { id });
		query = "DELETE FROM org_movimenti WHERE girofondo = ?";
		jdbcTemplate.update(query, new Object[] { id });
		query = "DELETE FROM org_movimenti WHERE id_movimento = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	/**
	 * @param code
	 * @return
	 */
	@Transactional()
	public String deleteByCode(String code) {
		String query = "DELETE FROM org_movimenti WHERE codice = ?";
		jdbcTemplate.update(query, new Object[] { code });
		return Def.STR_OK;
	}
	
	/**
	 * @param 
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getCodiceMovimento(String anno) {
		String m = null;
		String queryStr = "SELECT max(concat(" + anno + ",'/',lpad((CONVERT(substr(codice,6,9), SIGNED INTEGER) + 1), 4, 0))) codice " + 
				"FROM org_movimenti " + 
				"where substr(codice,1,4) = " + anno ;
		try{
			m =  jdbcTemplate.queryForObject(queryStr, String.class);
			
			if(m == null) {
				m = anno + "/0001";
			}
			
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			if(m == null) {
				m = anno + "/0001";
			}
			return m;
		}
	}
	
	
	/*----------------------------------------------------------------------------------------------------------*/
	
	/**
	 * @param id
	 * @return TipoMovimento
	 */
	@Transactional(readOnly = true)
	public TipoMovimento getTipoMovimentoByID(String id) {
		TipoMovimento m = null;
		String queryStr = "SELECT id_tipo_movimento, descrizione FROM org_x_tipo_movimenti where id_tipo_movimento = ?";
		try{
			m =  jdbcTemplate.queryForObject(queryStr, new TipoMovimentoRowMapper(), new Object[] { id });
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param 
	 * @return List<TipoMovimento>
	 */
	@Transactional(readOnly = true)
	public List<TipoMovimento> getTipoMovimento() {
		List<TipoMovimento> m = null;
		String queryStr = "SELECT id_tipo_movimento, descrizione FROM org_x_tipo_movimenti";
		try{
			m =  jdbcTemplate.query(queryStr, new TipoMovimentoRowMapper());
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
/*----------------------------------------------------------------------------------------------------------*/
	
	/**
	 * @param id
	 * @return Causale
	 */
	@Transactional(readOnly = true)
	public List<Causale> getCausaleByID(String id) {
		List<Causale> m = null;
		String queryStr = "SELECT id_causale, descrizione, id_tipo_movimento FROM org_x_causali where id_tipo_movimento = ? and attivo = 1";
		try{
			m =  jdbcTemplate.query(queryStr, new CausaleRowMapper(), new Object[] { id });
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param 
	 * @return List<Causale>
	 */
	@Transactional(readOnly = true)
	public List<Causale> getCausali() {
		List<Causale> m = null;
		String queryStr = "SELECT id_causale, descrizione, id_tipo_movimento FROM org_x_causali where attivo = 1";
		try{
			m =  jdbcTemplate.query(queryStr, new CausaleRowMapper());
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------*/
	
	/**
	 * @param 
	 * @return List<Destinazione>
	 */
	@Transactional(readOnly = true)
	public List<Destinazione> getDestinazioni() {
		List<Destinazione> m = null;
		String queryStr = "SELECT m.id_destinazione, m.descrizione " + 
				"FROM org_x_destinazione m ";
		try{
			m =  jdbcTemplate.query(queryStr, new DestinazioneRowMapper());
			for (Destinazione destinazione : m) {
				destinazione.setTipi(getTipoDestinazioniById(destinazione.getId_destinazione()));
			}
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param id
	 * @return Destinazione
	 */
	@Transactional(readOnly = true)
	public Destinazione getDestinazioneByID(String id) {
		Destinazione m = null;
		String queryStr = "SELECT m.id_destinazione, m.descrizione " + 
				"FROM org_x_destinazione m where id_destinazione = ?";
		try{
			m =  jdbcTemplate.queryForObject(queryStr, new DestinazioneRowMapper(), new Object[] { id });
			m.setTipoDestinazione(getTipoDestinazioneById(m.getId_destinazione()));
			m.setTipi(getTipoDestinazioniById(id));
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	private static class DestinazioneRowMapper extends BaseRowMapper<Destinazione> {
		public DestinazioneRowMapper() {
		}		
		@Override
		public Destinazione mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Destinazione o = new Destinazione();
			o.setId_destinazione(rs.getString("id_destinazione"));
			o.setDescrizione(rs.getString("descrizione"));
			return o;
		}
	}
	
	/**
	 * @param 
	 * @return List<Destinazione>
	 */
	@Transactional(readOnly = true)
	public List<TipoDestinazione> getTipoDestinazioniById(String id) {
		List<TipoDestinazione> m = null;
		String queryStr = "SELECT m.id_tipo_destinazione, m.descrizione " + 
				"FROM org_x_tipo_destinazione m where m.id_destinazione =  ? ";
		try{
			m =  jdbcTemplate.query(queryStr, new TipoDestinazioneRowMapper(), new Object[] {id});
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @param id
	 * @return TipoDestinazione
	 */
	@Transactional(readOnly = true)
	public TipoDestinazione getTipoDestinazioneById(String id) {
		TipoDestinazione m = null;
		String queryStr = "SELECT m.id_tipo_destinazione, m.descrizione " + 
				"FROM org_x_tipo_destinazione m where m.id_tipo_destinazione =  ? ";
		try{
			m =  jdbcTemplate.queryForObject(queryStr, new TipoDestinazioneRowMapper(), new Object[] {id});
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	private static class TipoDestinazioneRowMapper extends BaseRowMapper<TipoDestinazione> {
		public TipoDestinazioneRowMapper() {
		}		
		@Override
		public TipoDestinazione mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoDestinazione o = new TipoDestinazione();
			o.setId_tipo_destinazione(rs.getString("id_tipo_destinazione"));
			o.setDescrizione(rs.getString("descrizione"));
			return o;
		}
	}
	
/* ---------------------------------- VOCI IN MOVIMENTO ----------------------------------------------*/
	
	/**
	 * @param id
	 * @return VoceMovimento
	 */
	@Transactional(readOnly = true)
	public VoceMovimento getVoceMovimentoById(String id) {
		VoceMovimento o = null;
		String queryStr = "SELECT id_vm, descrizione, id_cr_sottovoce FROM org_x_voci_movimento WHERE id_vm = ?";
		try{
			o =  jdbcTemplate.queryForObject(queryStr, new VoceMovimentoSimpleRowMapper(), new Object[] { id });
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @return List<VoceMovimento>
	 */
	@Transactional(readOnly = true)
	public List<VoceMovimento> getAllVoceMovimento() {
		List<VoceMovimento> o = new ArrayList<VoceMovimento>();
		String queryStr = "SELECT  " + 
				"    vf.id_vm, " + 
				"    vf.descrizione, " + 
				"    sv.descrizione sottovoce, vf.id_cr_sottovoce " + 
				"From " + 
				"    org_x_voci_movimento vf Inner Join " + 
				"    org_rnd_cr_sottovoci sv On vf.id_cr_sottovoce = sv.id_cr_sottovoce";
		try{
			o =  jdbcTemplate.query(queryStr,  new VoceMovimentoRowMapper());
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @param search
	 * @return List<VoceMovimento>
	 */
	@Transactional(readOnly = true)
	public List<VoceMovimento> getVociMovimentoBySearch(String search) {
		List<VoceMovimento> o = new ArrayList<VoceMovimento>();
		String queryStr = "SELECT  " + 
				"    vf.id_vm, " + 
				"    vf.descrizione, " + 
				"    sv.descrizione sottovoce, vf.id_cr_sottovoce " + 
				"From " + 
				"    org_x_voci_movimento vf Inner Join " + 
				"    org_rnd_cr_sottovoci sv On vf.id_cr_sottovoce = sv.id_cr_sottovoce " +
				" Where upper(vf.descrizione) like '%" + search.toUpperCase() + "%'";
		try{
			o =  jdbcTemplate.query(queryStr,  new VoceMovimentoRowMapper());
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	
	/**
	 * @param vf
	 * @return id
	 */
	@Transactional()
	public String saveOrUpdateVoceMovimento(VoceMovimento vf) {
		String id = "";
		if (vf.getId_vm() == null) {
			id = save(vf);
		} else {
			id = update(vf);
		}
		return id;
	}

	/**
	 * @param vf
	 * @return id
	 */
	@Transactional()
	public String save(VoceMovimento vf) {
		String descrizione = "(" + vf.getId_cr_sotto_voce() + ") - " + vf.getDescrizione();
		final String query = "INSERT INTO org_x_voci_movimento (id_cr_sottovoce, descrizione) " + 
				"VALUES (:id_cr_sotto_voce, '" + descrizione + "')";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(vf);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_vm" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param vf
	 * @return id
	 */
	@Transactional()
	public String update(VoceMovimento vf) {
		String descrizione = "(" + vf.getId_cr_sotto_voce() + ") - " + vf.getDescrizione().substring(vf.getDescrizione().indexOf("- ")+2,vf.getDescrizione().length());
		vf.setDescrizione(descrizione);
		
		final String query = "UPDATE org_x_voci_movimento " + 
				"SET " + 
				"id_cr_sottovoce = :id_cr_sotto_voce, " + 
				"descrizione = :descrizione " + 
				"WHERE id_vm = :id_vm";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(vf);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return vf.getId_vm();
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteVoceMovimentoByID(String id) {
		String query = "DELETE FROM org_x_voci_movimento WHERE id_vm = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	private static class VoceMovimentoRowMapper extends BaseRowMapper<VoceMovimento> {
		public VoceMovimentoRowMapper() { }		
		@Override
		public VoceMovimento mapRowImpl(ResultSet rs, int i) throws SQLException {
			VoceMovimento o = new VoceMovimento();
			o.setId_vm(rs.getString("id_vm"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_cr_sotto_voce(rs.getString("id_cr_sottovoce"));
			o.setSottovoce(rs.getString("sottovoce"));
			return o;
		}
	}
	
	private static class VoceMovimentoSimpleRowMapper extends BaseRowMapper<VoceMovimento> {
		public VoceMovimentoSimpleRowMapper() { }		
		@Override
		public VoceMovimento mapRowImpl(ResultSet rs, int i) throws SQLException {
			VoceMovimento o = new VoceMovimento();
			o.setId_vm(rs.getString("id_vm"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_cr_sotto_voce(rs.getString("id_cr_sottovoce"));
			return o;
		}
	}
	
	private static class TotaliRowMapper extends BaseRowMapper<TotaliMovimento> {
		public TotaliRowMapper() { }		
		@Override
		public TotaliMovimento mapRowImpl(ResultSet rs, int i) throws SQLException {
			TotaliMovimento o = new TotaliMovimento();
			o.setMovimento(rs.getString("id_tipo_movimento"));
			o.setImporto(rs.getString("importo"));
			o.setDestinazione(rs.getString("tipo"));
			return o;
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------*/
	
	private static class MovimentoRowMapper extends BaseRowMapper<Movimento> {
		public MovimentoRowMapper() {
		}		
		@Override
		public Movimento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Movimento o = new Movimento();
			o.setId_movimento(rs.getString("id_movimento"));
			o.setId_organizzazione(rs.getString("id_organizzazione"));
			o.setId_tipo_movimento(rs.getString("id_tipo_movimento"));
			o.setCodice(rs.getString("codice"));
			o.setDt_operazione(rs.getString("dt_operazione"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setAccount(rs.getString("account"));
			o.setImporto(rs.getString("importo"));
			o.setId_causale(rs.getString("id_causale"));
			o.setNote(rs.getString("note"));
			o.setId_cc(rs.getString("id_cc"));
			o.setId_destinazione(rs.getString("id_destinazione"));
			o.setId_tipo_destinazione(rs.getString("id_tipo_destinazione"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setGirofondo(rs.getInt("girofondo"));
			return o;
		}
	}
	
	private static class MovimentoRestrictRowMapper extends BaseRowMapper<MovimentoRestrict> {
		public MovimentoRestrictRowMapper() {
		}		
		@Override
		public MovimentoRestrict mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			MovimentoRestrict o = new MovimentoRestrict();
			o.setId_movimento(rs.getString("id_movimento"));
			o.setCodice(rs.getString("codice"));
			o.setDt_operazione(rs.getString("dt_operazione"));
			o.setImporto(rs.getString("importo"));
			o.settDestinazione(rs.getString("tDestinazione"));
			o.settMovimento(rs.getString("tMovimento"));
			o.setCausale(rs.getString("causale"));
			o.setId_tipo_movimento(rs.getString("id_tipo_movimento"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setId_causale(rs.getString("id_causale"));
			o.setId_tipo_destinazione(rs.getString("id_tipo_destinazione"));
			o.setId_destinazione(rs.getString("id_destinazione"));
			o.setGirofondo(rs.getInt("girofondo"));
			return o;
		}
	}
	
	private static class TipoMovimentoRowMapper extends BaseRowMapper<TipoMovimento> {
		public TipoMovimentoRowMapper() {
		}		
		@Override
		public TipoMovimento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoMovimento o = new TipoMovimento();
			o.setId_tipo_movimento(rs.getString("id_tipo_movimento"));
			o.setDescrizione(rs.getString("descrizione"));
			return o;
		}
	}
	
	private static class CausaleRowMapper extends BaseRowMapper<Causale> {
		public CausaleRowMapper() {
		}		
		@Override
		public Causale mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Causale o = new Causale();
			o.setId_tipo_movimento(rs.getString("id_tipo_movimento"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_causale(rs.getString("id_causale"));
			return o;
		}
	}
	
	private static class DettaglioMovimentoRowMapper extends BaseRowMapper<DettaglioMovimento> {
		public DettaglioMovimentoRowMapper() {
		}		
		@Override
		public DettaglioMovimento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			DettaglioMovimento o = new DettaglioMovimento();
			o.setId_movimento_dettaglio(rs.getString("id_movimento_dettaglio"));
			o.setId_movimento(rs.getString("id_movimento"));
			o.setId_cr_sottovoce(rs.getString("id_cr_sottovoce"));
			o.setImporto(rs.getString("importo"));
			return o;
		}
	}
	
	private static class FatturaSimpleRowMapper extends BaseRowMapper<Fattura> {
		public FatturaSimpleRowMapper() { }		
		@Override
		public Fattura mapRowImpl(ResultSet rs, int i) throws SQLException {
			Fattura o = new Fattura();
			o.setCodice(rs.getString("codice"));
			o.setDt_emissione(rs.getString("dt_emissione"));
			o.setDt_scadenza(rs.getString("dt_scadenza"));
			o.setId_fattura(rs.getString("id_fattura"));
			o.setImporto(rs.getString("importo"));
			o.setNumero(rs.getString("numero"));
			o.setTipo_fattura(rs.getString("tipo_fattura"));
			o.setContatto(rs.getString("contatto"));
			o.setId_contatto(rs.getString("id_contatto"));
			return o;
		}
	}

}
