package it.asso.core.dao.organizzazione;

import it.asso.core.common.IsNull;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.organizzazione.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Repository
public class OrganizzazioneDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrganizzazioneDAO.class);
    private final JdbcTemplate jdbcTemplate;

    public OrganizzazioneDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param id
	 * @return Organizzazione
	 */
	@Transactional(readOnly = true)
	public Organizzazione getByID(String id) {
		Organizzazione org = null;
		String queryStr = "SELECT id_organizzazione, id_tipo_organizzazione, rag_sociale, indirizzo, cf, telefono, sigla, telefono_1, email, url, tenant, iscrizione FROM org_anagrafica WHERE id_organizzazione = ?";
		try{
			org =  jdbcTemplate.queryForObject(queryStr, new OrganizzazioneRowMapper(), new Object[] { id });
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	/**
	 * @param tenant
	 * @return Organizzazione
	 */
	@Transactional(readOnly = true)
	public Organizzazione getByTenant(String tenant) {
		Organizzazione org = null;
		String queryStr = "SELECT id_organizzazione, id_tipo_organizzazione, rag_sociale, indirizzo, cf, telefono, sigla, telefono_1, email, url, tenant, iscrizione FROM org_anagrafica WHERE tenant = ?";
		try{
			org =  jdbcTemplate.queryForObject(queryStr, new OrganizzazioneRowMapper(), new Object[] { tenant });
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	/**
	 * @param 
	 * @return List<Organizzazione>
	 */
	@Transactional(readOnly = true)
	public List<Organizzazione> getAll() {
		List<Organizzazione> org = null;
		String queryStr = "SELECT id_organizzazione, id_tipo_organizzazione, rag_sociale, indirizzo, cf, telefono, sigla, telefono_1, email, url,tenant, iscrizione FROM org_anagrafica ORDER BY rag_sociale";
		try{
			org =  jdbcTemplate.query(queryStr, new OrganizzazioneRowMapper());
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	/**
	 * @param org
	 * @return
	 */
	@Transactional()
	public String saveOrUpdate(Organizzazione org) {
		String idOrganizzazione = "";

		if (org.getId_organizzazione() == null) {
			idOrganizzazione = save(org);
		} else {
			idOrganizzazione = update(org);
		}

		return idOrganizzazione;
	}

	/**
	 * @param org
	 * @return
	 */
	@Transactional()
	public String save(Organizzazione org) {
		final String query = "INSERT INTO org_anagrafica (rag_sociale, indirizzo, cf, telefono, sigla, telefono_1, email, url, tenant, iscrizione, id_tipo_organizzazione) " + 
				"VALUES (:rag_sociale, :indirizzo, :cf, :telefono, :sigla, :telefono_1, :email, :url, :tenant, :iscrizione, :id_tipo_organizzazione)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(org);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_animale" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param org
	 * @return
	 */
	@Transactional()
	public String update(Organizzazione org) {

		final String query = "UPDATE org_anagrafica " + 
				"SET " + 
					"rag_sociale = :rag_sociale, " + 
					"indirizzo = :indirizzo, " + 
					"cf = :cf, " + 
					"telefono = :telefono, " + 
					"sigla = :sigla, " + 
					"telefono_1 = :telefono_1, " + 
					"email = :email, " + 
					"url = :url, " + 
					"tenant = :tenant, " + 
					"iscrizione = :iscrizione, id_tipo_organizzazione = :id_tipo_organizzazione " + 
				"WHERE id_organizzazione = :id_organizzazione";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(org);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return org.getId_organizzazione();
	}
	
	private static class OrganizzazioneRowMapper extends BaseRowMapper<Organizzazione> {
		public OrganizzazioneRowMapper() {
		}		
		@Override
		public Organizzazione mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Organizzazione o = new Organizzazione();
			o.setCf(rs.getString("cf"));
			o.setId_organizzazione(rs.getString("id_organizzazione"));
			o.setIndirizzo(rs.getString("indirizzo"));
			o.setRag_sociale(rs.getString("rag_sociale"));
			o.setSigla(rs.getString("sigla"));
			o.setTelefono(rs.getString("telefono"));
			o.setTelefono_1(rs.getString("telefono_1"));
			o.setEmail(rs.getString("email"));
			o.setUrl(rs.getString("url"));
			o.setTenant(rs.getString("tenant"));
			o.setIscrizione(rs.getString("iscrizione"));
			o.setId_tipo_organizzazione(rs.getString("id_tipo_organizzazione"));
			return o;
		}
	}
	
	/**************************************************************************************************************/
	
	
	/**
	 * @param ricerca
	 * @return List<Protocollo>
	 */
	@Transactional(readOnly = true)
	public List<Protocollo> getProtocolli(RicercaDTO ricerca) {
		List<Protocollo> org = null;
		String forPagination = "";
		
		if(!"".equals(ricerca.getLimit()) && !"".equals(ricerca.getOffset())) {
			forPagination = "limit " + ricerca.getLimit() + "," + ricerca.getOffset();
		}
		String strToSearch = "%" + IsNull.thenBlank(ricerca.getSearch().trim()) + "%";
		String queryStr = "SELECT id_protocollo, "
				+ "    codice, "
				+ "    id_documento, "
				+ "    documento, "
				+ "    DATE_FORMAT(dt_protocollo, '%d/%m/%Y %H:%i:%s') dt_protocollo, "
				+ "    oggetto, "
				+ "    mittente, "
				+ "    destinatario "
				+ "FROM org_protocollo "
				+ "WHERE upper(oggetto) like upper(?) "
				+ "ORDER BY dt_protocollo desc " + forPagination;
		try{
			org =  jdbcTemplate.query(queryStr,  new ProtocolloRowMapper(),new Object[] { strToSearch });
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	/**
	 * @param ricerca
	 * @return int
	 */
	@Transactional(readOnly = true)
	public int getProtocolliCountBySearch(RicercaDTO ricerca) {
		int count = 0;
		String strToSearch = "%" + IsNull.thenBlank(ricerca.getSearch().trim()) + "%";
		String queryStr = "SELECT count(id_protocollo) "
				+ "FROM org_protocollo "
				+ "WHERE upper(oggetto) like upper(?) ";

		try{
			count =  jdbcTemplate.queryForObject(queryStr,  Integer.class,new Object[] { strToSearch });
			return count;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return count;
		}
	}
	
	/**
	 * @param idProtocollo
	 * @return Protocollo
	 */
	@Transactional(readOnly = true)
	public Protocollo getProtocolloByID(String idProtocollo) {
		Protocollo protocollo = null;
		String queryStr = "SELECT id_protocollo, "
				+ "    codice, "
				+ "    id_documento, "
				+ "    documento, "
				+ "    DATE_FORMAT(dt_protocollo, '%d/%m/%Y %H:%i:%s') dt_protocollo, "
				+ "    oggetto, "
				+ "    mittente, "
				+ "    destinatario "
				+ "FROM org_protocollo "
				+ "WHERE id_protocollo = ? ";
		try{
			protocollo =  jdbcTemplate.queryForObject(queryStr,  new ProtocolloRowMapper(),new Object[] { idProtocollo });
			return protocollo;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return protocollo;
		}
	}
	
	

	/**
	 * @param protocollo
	 * @return
	 */
	@Transactional()
	public String save(Protocollo protocollo) {
		int anno = Year.now().getValue();
		String query = "SELECT coalesce(max(CONVERT(SUBSTRING(codice, 6, 5),UNSIGNED INTEGER)),0) + 1 cod FROM org_protocollo where year(dt_protocollo) =  " + anno ;
		int maxCodice = jdbcTemplate.queryForObject(query, Integer.class);
		String codice = anno + "." + StringUtils.leftPad(String.valueOf(maxCodice),5,"0");
		protocollo.setCodice(codice);
		
		query = "INSERT INTO org_protocollo (codice, id_documento, documento, oggetto, mittente, destinatario) " + 
				                               "VALUES (:codice, :id_documento, :documento, :oggetto, :mittente, :destinatario)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(protocollo);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_protocollo" });

		return String.valueOf(keyHolder.getKey());
	}
	

	
	
	private static class ProtocolloRowMapper extends BaseRowMapper<Protocollo> {
		public ProtocolloRowMapper() {
		}		
		@Override
		public Protocollo mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Protocollo o = new Protocollo();
			o.setCodice(rs.getString("codice"));
			o.setDestinatario(rs.getString("destinatario"));
			o.setDocumento(rs.getString("documento"));
			o.setDt_protocollo(rs.getString("dt_protocollo"));
			o.setId_documento(rs.getString("id_documento"));
			o.setId_protocollo(rs.getString("id_protocollo"));
			o.setMittente(rs.getString("mittente"));
			o.setOggetto(rs.getString("oggetto"));
			return o;
		}
	}
	
	/**************************************************************************************************************/
	
	/**
	 * @param 
	 * @return GraficoABarre
	 */
	@Transactional(readOnly = true)
	public GraficoABarre getEntrateUscite() {
		GraficoABarre gb = new GraficoABarre();
		String queryStr = "Select "
				+ "    ifnull(Sum(m.importo),0) data, "
				+ "    x.label, "
				+ "    x.anno "
				+ "From "
				+ "    org_movimenti m Right Join "
				+ "    (Select Distinct "
				+ "         tm.descrizione label, "
				+ "         tm.id_tipo_movimento id_tipo_movimento, "
				+ "         Year(m.dt_operazione) anno "
				+ "     From "
				+ "         org_movimenti m, "
				+ "         org_x_tipo_movimenti tm) x On m.id_tipo_movimento = x.id_tipo_movimento "
				+ "            And x.anno = Year(m.dt_operazione)     "
				+ "Group By "
				+ "    x.label, "
				+ "    x.anno "
				+ "order by x.anno, x.label";
		try{
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(queryStr);
			gb = getDatoGrafico(rowSet);
			return gb;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return gb;
		}
	}
	
	/**
	 * @param 
	 * @return GraficoABarre
	 */
	@Transactional(readOnly = true)
	public GraficoABarre getStalliPensione() {
		GraficoABarre gb = new GraficoABarre();
		String queryStr = "Select "
				+ "    Count(es.id_evento) data, "
				+ "    e.evento label, "
				+ "    Year(es.dt_aggiornamento) anno "
				+ "From "
				+ "    an_x_evento_storico e Inner Join "
				+ "    an_r_eventi_storico es On es.id_tipo_evento = e.id_tipo_evento "
				+ "Where e.id_tipo_evento in (3,5) "
				+ "Group By "
				+ "    e.evento, "
				+ "    Year(es.dt_aggiornamento), "
				+ "    e.id_tipo_evento "
				+ "order by anno, label";
		try{
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(queryStr);
			gb = getDatoGrafico(rowSet);
			return gb;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return gb;
		}
	}
	
	/**
	 * @param 
	 * @return GraficoABarre
	 */
	@Transactional(readOnly = true)
	public GraficoABarre getDocumentiAnno() {
		GraficoABarre gb = new GraficoABarre();
		String queryStr = "Select Distinct "
				+ "    Count(p.id_protocollo) data, "
				+ "    Year(p.dt_protocollo) anno, "
				+ "    'Documenti' label "
				+ "From "
				+ "    org_protocollo p "
				+ "Group By "
				+ "    anno, label "
				+ "Order By anno, label";
		try{
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(queryStr);
			gb = getDatoGrafico(rowSet);
			return gb;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return gb;
		}
	}
	
	/**
	 * @param 
	 * @return GraficoABarre
	 */
	@Transactional(readOnly = true)
	public GraficoABarre getContattiAnno() {
		GraficoABarre gb = new GraficoABarre();
		String queryStr = "Select "
				+ "    count(c.id_contatto) data, "
				+ "    Year(c.dt_aggiornamento) anno, "
				+ "    'Contatti' label "
				+ "From "
				+ "    an_contatti c "
				+ "    group by anno "
				+ "    order by anno, label";
		try{
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(queryStr);
			gb = getDatoGrafico(rowSet);
			return gb;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return gb;
		}
	}
	
	/**
	 * @param 
	 * @return GraficoABarre
	 */
	@Transactional(readOnly = true)
	public GraficoABarre getAdozioniAnno() {
		GraficoABarre gb = new GraficoABarre();
		String queryStr = "Select  "
				+ "				 count(x.id_animale) data,  "
				+ "				    case when Year(x.dt_consegna)is null then  "
				+ "				    (select year(max(dt_attivita)) from an_r_attivita where id_animale = x.id_animale and id_attivita = 11)  "
				+ "				    else  "
				+ "				    Year(x.dt_consegna)  "
				+ "				    end label, "
				+ "				    case when Month(x.dt_consegna)is null then  "
				+ "				    (select Month(max(dt_attivita)) from an_r_attivita where id_animale = x.id_animale and id_attivita = 11)  "
				+ "				    else  "
				+ "				    Month(x.dt_consegna)  "
				+ "				    end anno "  
				+ "				From  "
				+ "				    an_r_iter x  "
				+ "				Where  "
				+ "				    x.id_tipo_iter = 3  "
				+ "				group by label ,anno "
				+ "				order by label, anno";
		try{
			SqlRowSet rowSet = jdbcTemplate.queryForRowSet(queryStr);
			gb = getDatoGrafico(rowSet);
			return gb;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return gb;
		}
	}
	
	private GraficoABarre getDatoGrafico(SqlRowSet rowSet) {
		GraficoABarre gb = new GraficoABarre();
		List<String> labels = new ArrayList<String>();
		
		Map<String,List<String>> data = new HashMap<String, List<String>>();
		while(rowSet.next())
		{
			if(data.get(rowSet.getString("label")) == null || data.get(rowSet.getString("label")).isEmpty()) {
				List<String> label = new ArrayList<String>();
				label.add(rowSet.getString("data"));
				data.put(rowSet.getString("label"), label);
			}else {
				data.get(rowSet.getString("label")).add(rowSet.getString("data"));
			}
				
			if(!labels.contains(rowSet.getString("anno"))){
				labels.add(rowSet.getString("anno"));
			}

		}
		gb.setLabels(labels);
		List<DataSet> dataset = new ArrayList<DataSet>();
		for(Entry<String, List<String>> entry: data.entrySet()) {
	      DataSet ds = new DataSet();
	      ds.setLabel(entry.getKey());
	      ds.setData(entry.getValue());
	      dataset.add(ds);
	    }
		gb.setData(dataset);
		return gb;
	}

	
}
