package it.asso.core.dao.organizzazione.contabilita;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.organizzazione.RicercaDTO;
import it.asso.core.model.organizzazione.TotaliMovimento;
import it.asso.core.model.organizzazione.contabilita.DettaglioFattura;
import it.asso.core.model.organizzazione.contabilita.Fattura;
import it.asso.core.model.organizzazione.contabilita.VoceFattura;
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
public class FattureDAO {
    private static final Logger logger = LoggerFactory.getLogger(FattureDAO.class);
    private final JdbcTemplate jdbcTemplate;

    public FattureDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
/* ---------------------------------- FATTURA ----------------------------------------------*/
	/**
	 * @param id
	 * @return Fattura
	 */
	@Transactional(readOnly = true)
	public Fattura getById(String id) {
		Fattura o = null;
		String queryStr = "SELECT f.id_fattura, f.tipo_fattura, f.codice, f.numero, f.id_contatto,  " + 
				"	NullIf(Date_Format(f.dt_emissione,'%d/%m/%Y'), '') dt_emissione, " + 
				"	NullIf(Date_Format(f.dt_scadenza,'%d/%m/%Y'), '') dt_scadenza, " + 
				"	f.imponibile, f.iva, f.importo, f.account, " + 
				"	NullIf(Date_Format(f.dt_aggiornamento,'%d/%m/%Y'), '') dt_aggiornamento, " + 
				"	nullif(DATE_FORMAT(f.dt_inserimento,'%d/%m/%Y'),'') dt_inserimento, " + 
				"	f.note, " + 
			    "   m.id_movimento da_pagare, f.rit_acc_fattura " + 
			    " From " + 
			    "    org_fatture f Left Join " + 
			    "    org_movimenti_fatture m On m.id_fattura = f.id_fattura " + 
			    "    WHERE f.id_fattura = ?";
		try{
			o =  jdbcTemplate.queryForObject(queryStr, new FatturaRowMapper(), new Object[] { id });
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @param id
	 * @return Fattura
	 */
	@Transactional(readOnly = true)
	public Fattura getFatturaCollegataById(String id) {
		Fattura o = null;
		String queryStr = "SELECT f.id_fattura, f.tipo_fattura, f.codice, f.numero, f.id_contatto,  " + 
				"	NullIf(Date_Format(f.dt_emissione,'%d/%m/%Y'), '') dt_emissione, " + 
				"	NullIf(Date_Format(f.dt_scadenza,'%d/%m/%Y'), '') dt_scadenza, " + 
				"	f.imponibile, f.iva, f.importo, f.account, " + 
				"	NullIf(Date_Format(f.dt_aggiornamento,'%d/%m/%Y'), '') dt_aggiornamento, " + 
				"	nullif(DATE_FORMAT(f.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
				"	f.note, " + 
			    "   m.id_movimento da_pagare, f.rit_acc_fattura " + 
			    " From " + 
			    "    org_fatture f Left Join " + 
			    "    org_movimenti_fatture m On m.id_fattura = f.id_fattura " + 
			    "    WHERE f.rit_acc_fattura = ?";
		try{
			o =  jdbcTemplate.queryForObject(queryStr, new FatturaRowMapper(), new Object[] { id });
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Fattura>
	 */
	@Transactional(readOnly = true)
	public List<Fattura> getAll(RicercaDTO ricerca, String idOrganizzazione) {
		List<Fattura> o = new ArrayList<Fattura>();
		
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
				+ "    a.cod_fiscale), '')) contatto, "
				+ "    f.rit_acc_fattura, "
				+ "    If(mf.id_movimento Is Null, 1, 0) da_pagare "
				+ "From "
				+ "    org_fatture f Left Join "
				+ "    an_contatti a On f.id_contatto = a.id_contatto Left Join "
				+ "    org_movimenti_fatture mf On mf.id_fattura = f.id_fattura "
				+ "Where "
				+ "    f.id_organizzazione = ? And "
				+ "    Year(f.dt_emissione) = ? And "
				+ "    f.tipo_fattura Like (?) and "
				+ "    If(mf.id_movimento Is Null, 1, 0) like (?) "
				+ "Order By "
				+ "    f.codice Desc " + forPagination;;
		try{
			o =  jdbcTemplate.query(queryStr,  new FatturaSimpleRowMapper(), new Object[] {idOrganizzazione, ricerca.getAnno(), ricerca.gettMovimento(), ricerca.gettDestinazione()});
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @param ricerca
	 * @return List<Movimento>
	 */
	@Transactional(readOnly = true)
	public int getCountBySearch(RicercaDTO ricerca, String idOrganizzazione) {
		int numFatture = 0;
		if(ricerca.gettMovimento() == null || "".equals(ricerca.gettMovimento())) {
			ricerca.settMovimento(Def.STR_PERCENTAGE);
		}
		
		if(ricerca.gettDestinazione() == null || "".equals(ricerca.gettDestinazione())) {
			ricerca.settDestinazione(Def.STR_PERCENTAGE);
		}
		
		String queryStr = "Select count(distinct f.id_fattura) "
				+ "From "
				+ "    org_fatture f Left Join "
				+ "    an_contatti a On f.id_contatto = a.id_contatto Left Join "
				+ "    org_movimenti_fatture mf On mf.id_fattura = f.id_fattura "
				+ "Where "
				+ "    f.id_organizzazione = ? And "
				+ "    Year(f.dt_emissione) = ? And "
				+ "    f.tipo_fattura Like (?) and "
				+ "    If(mf.id_movimento Is Null, 1, 0) like (?) ";
		try{
			numFatture =  jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] {idOrganizzazione, ricerca.getAnno(), ricerca.gettMovimento(), ricerca.gettDestinazione()});			
			return numFatture;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return numFatture;
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
				+ "    f.tipo_fattura id_tipo_movimento, "
				+ "    sum(f.importo) importo, "
				+ "    If(mf.id_movimento Is Null, 1, 0) tipo "
				+ "From "
				+ "    org_fatture f Left Join "
				+ "    an_contatti a On f.id_contatto = a.id_contatto Left Join "
				+ "    org_movimenti_fatture mf On mf.id_fattura = f.id_fattura "
				+ "Where "
				+ "    f.id_organizzazione=? and "
				+ "    Year(f.dt_emissione) = ? "
				+ "Group by f.tipo_fattura,If(mf.id_movimento Is Null, 1, 0)";
		try{
			m =  jdbcTemplate.query(queryStr, new TotaliRowMapper(), new Object[] {idOrganizzazione, ricerca.getAnno()});			
			return m;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return m;
		}
	}
	
	/**
	 * @return List<Fattura>
	 */
	@Transactional(readOnly = true)
	public List<Fattura> getFattureNonAssociate() {
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
				+ "    f.rit_acc_fattura, "
				+ "    Concat(a.rag_sociale, IfNull(Concat(' (', a.cognome, ' ', a.nome, ') '), ''), IfNull(Concat(' - Piva/CF ', "
				+ "    a.cod_fiscale), '')) contatto, "
				+ "    If(mf.id_fattura Is Null, 1, 0) da_pagare "
				+ "From "
				+ "    org_fatture f Inner Join "
				+ "    an_contatti a On f.id_contatto = a.id_contatto Left Join "
				+ "    org_movimenti_fatture mf On mf.id_fattura = f.id_fattura "
				+ "Where "
				+ "    f.rit_acc_fattura Is Null And "
				+ "    If(mf.id_fattura Is Null, 1, 0) = 1 And "
				+ "    f.id_organizzazione = 1 "
				+ "Order By "
				+ "    f.codice Desc";
		try{
			o =  jdbcTemplate.query(queryStr, new FatturaSimpleRowMapper());
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @return List<Fattura>
	 */
	@Transactional(readOnly = true)
	public List<Fattura> getRicevuteAccontoNonAssociate() {
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
				+ "    f.rit_acc_fattura, "
				+ "    Concat(a.rag_sociale, IfNull(Concat(' (', a.cognome, ' ', a.nome, ') '), ''), IfNull(Concat(' - Piva/CF ', "
				+ "    a.cod_fiscale), '')) contatto, "
				+ "    If(mf.id_fattura Is Null, 1, 0) da_pagare "
				+ "From "
				+ "    org_fatture f Inner Join "
				+ "    an_contatti a On f.id_contatto = a.id_contatto Left Join "
				+ "    org_movimenti_fatture mf On mf.id_fattura = f.id_fattura "
				+ "Where "
				+ "    f.rit_acc_fattura Is Not Null And "
				+ "    If(mf.id_fattura Is Null, 1, 0) = 1 And "
				+ "    f.id_organizzazione = 1 "
				+ "Order By "
				+ "    f.codice Desc";
		try{
			o =  jdbcTemplate.query(queryStr, new FatturaSimpleRowMapper());
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
	public String saveOrUpdate(Fattura vf) {
		String id = "";
		if (vf.getId_fattura() == null) {
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
	public String save(Fattura vf) {
		final String query = "INSERT INTO org_fatture " + 
				"( tipo_fattura, codice, numero, id_contatto, dt_emissione, dt_scadenza, imponibile, " + 
				"iva, importo, account, note, rit_acc_fattura, id_organizzazione) " + 
				"VALUES ( :tipo_fattura, :codice, :numero, :id_contatto, str_to_date(nullif(:dt_emissione,''), '%d/%m/%Y'), " + 
				"str_to_date(nullif(:dt_scadenza,''), '%d/%m/%Y'), :imponibile, :iva, :importo, :account, :note, :ritAccFattura, :id_organizzazione)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(vf);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_fattura" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param vf
	 * @return id
	 */
	@Transactional()
	public String update(Fattura vf) {
		final String query = "UPDATE org_fatture " + 
				"SET " + 
					"tipo_fattura = :tipo_fattura, " + 
					"codice = :codice, " + 
					"numero = :numero, " + 
					"id_contatto = :id_contatto, " + 
					"dt_emissione = str_to_date(nullif(:dt_emissione,''), '%d/%m/%Y'), " + 
					"dt_scadenza = str_to_date(nullif(:dt_scadenza,''), '%d/%m/%Y'), " + 
					"imponibile = :imponibile, " + 
					"iva = :iva, " + 
					"importo = :importo, " + 
					"account = :account, " + 
					"dt_aggiornamento = str_to_date(nullif(:dt_aggiornamento,''), '%d/%m/%Y'), " +
					"rit_acc_fattura = :ritAccFattura, " +
					"note = :note " +
				"WHERE id_fattura = :id_fattura";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(vf);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return vf.getId_fattura();
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteByID(String id) {	
		String query = "DELETE FROM org_fatture WHERE id_fattura = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	/**
	 * @param 
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getCodiceFattura(String anno) {
		String m = null;
		String queryStr = "SELECT max(concat(" + anno + ",'/',lpad((CONVERT(substr(codice,6,9), SIGNED INTEGER) + 1), 4, 0))) codice " + 
				"FROM org_fatture " + 
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
	
	/**
	 * @param idFattura
	 * @return List<DettaglioFattura>
	 */
	@Transactional(readOnly = true)
	public List<DettaglioFattura> getDettagliFattura(String idFattura) {
		List<DettaglioFattura> o = new ArrayList<DettaglioFattura>();
		String queryStr = "SELECT fd.id_fd,fd.id_fattura,fd.id_vf,fd.imponibile,fd.iva,fd.importo,vf.descrizione from org_fatture_dettaglio fd Inner Join org_x_voci_fattura vf On fd.id_vf = vf.id_vf WHERE fd.id_fattura = ?";
		try{
			o =  jdbcTemplate.query(queryStr,  new DettaglioFatturaRowMapper(), new Object[] {idFattura});
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @param d
	 * @return id
	 */
	@Transactional()
	public String saveOrUpdate(DettaglioFattura d) {
		String id = "";
		if (d.getId_fd() == null) {
			id = save(d);
		} else {
			id = update(d);
		}
		return id;
	}
	
	/**
	 * @param d
	 * @return id
	 */
	@Transactional()
	public String save(DettaglioFattura d) {
		final String query = "INSERT INTO org_fatture_dettaglio (id_fattura, id_vf, imponibile, iva, importo) " + 
				"VALUES (:id_fattura, :id_vf, :imponibile, :iva, :importo)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(d);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_df" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param d
	 * @return id
	 */
	@Transactional()
	public String update(DettaglioFattura d) {
		final String query = "UPDATE org_fatture_dettaglio " + 
				"SET " + 
					"id_fattura = :id_fattura, id_vf = :id_vf, imponibile = :imponibile, iva = :iva, importo = :importo " +
				"WHERE id_fd = :id_fd";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(d);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return d.getId_fd();
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteDettaglioFatturaByID(String id) {	
		String query = "DELETE FROM org_fatture_dettaglio WHERE id_fd = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteDettaglioFatturaByIDFattura(String id) {	
		String query = "DELETE FROM org_fatture_dettaglio WHERE id_fattura = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	private static class DettaglioFatturaRowMapper extends BaseRowMapper<DettaglioFattura> {
		public DettaglioFatturaRowMapper() { }		
		@Override
		public DettaglioFattura mapRowImpl(ResultSet rs, int i) throws SQLException {
			DettaglioFattura o = new DettaglioFattura();
			o.setId_fattura(rs.getString("id_fattura"));
			o.setImporto(rs.getString("importo"));
			o.setId_fd(rs.getString("id_fd"));
			o.setId_vf(rs.getString("id_vf"));
			o.setImponibile(rs.getString("imponibile"));
			o.setIva(rs.getString("iva"));
			o.setDescrizione(rs.getString("descrizione"));
			return o;
		}
	}
	
	private static class FatturaRowMapper extends BaseRowMapper<Fattura> {
		public FatturaRowMapper() { }		
		@Override
		public Fattura mapRowImpl(ResultSet rs, int i) throws SQLException {
			Fattura o = new Fattura();
			o.setAccount(rs.getString("account"));
			o.setCodice(rs.getString("codice"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setDt_emissione(rs.getString("dt_emissione"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setDt_scadenza(rs.getString("dt_scadenza"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setId_fattura(rs.getString("id_fattura"));
			o.setImponibile(rs.getString("imponibile"));
			o.setImporto(rs.getString("importo"));
			o.setIva(rs.getString("iva"));
			o.setNumero(rs.getString("numero"));
			o.setTipo_fattura(rs.getString("tipo_fattura"));
			o.setNote(rs.getString("note"));
			o.setDaPagare(rs.getString("da_pagare"));
			o.setRitAccFattura(rs.getString("rit_acc_fattura"));
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
			o.setDaPagare(rs.getString("da_pagare"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setRitAccFattura(rs.getString("rit_acc_fattura"));
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
	
	
/* ---------------------------------- VOCI IN FATTURA ----------------------------------------------*/
	
	/**
	 * @param id
	 * @return VoceFattura
	 */
	@Transactional(readOnly = true)
	public VoceFattura getVoceFatturaById(String id) {
		VoceFattura o = null;
		String queryStr = "SELECT id_vf, descrizione, id_cr_sottovoce, locked FROM org_x_voci_fattura WHERE id_vf = ?";
		try{
			o =  jdbcTemplate.queryForObject(queryStr, new VoceFatturaSimpleRowMapper(), new Object[] { id });
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @return List<VoceFattura>
	 */
	@Transactional(readOnly = true)
	public List<VoceFattura> getAllVoceFattura() {
		List<VoceFattura> o = new ArrayList<VoceFattura>();
		String queryStr = "SELECT  " + 
				"    vf.id_vf, " + 
				"    vf.descrizione, " + 
				"    sv.descrizione sottovoce, vf.id_cr_sottovoce, vf.locked " + 
				"From " + 
				"    org_x_voci_fattura vf Inner Join " + 
				"    org_rnd_cr_sottovoci sv On vf.id_cr_sottovoce = sv.id_cr_sottovoce";
		try{
			o =  jdbcTemplate.query(queryStr,  new VoceFatturaRowMapper());
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	/**
	 * @param search
	 * @return List<VoceFattura>
	 */
	@Transactional(readOnly = true)
	public List<VoceFattura> getVociFattureBySearch(String search) {
		List<VoceFattura> o = new ArrayList<VoceFattura>();
		String queryStr = "SELECT  " + 
				"    vf.id_vf, " + 
				"    vf.descrizione, " + 
				"    sv.descrizione sottovoce, vf.id_cr_sottovoce, vf.locked " + 
				"From " + 
				"    org_x_voci_fattura vf Inner Join " + 
				"    org_rnd_cr_sottovoci sv On vf.id_cr_sottovoce = sv.id_cr_sottovoce " +
				" Where upper(vf.descrizione) like '%" + search.toUpperCase() + "%'";
		try{
			o =  jdbcTemplate.query(queryStr,  new VoceFatturaRowMapper());
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
	public String saveOrUpdateVoceFattura(VoceFattura vf) {
		String id = "";
		if (vf.getId_vf() == null) {
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
	public String save(VoceFattura vf) {
		String descrizione = "(" + vf.getId_cr_sotto_voce() + ") - " + vf.getDescrizione();
		final String query = "INSERT INTO org_x_voci_fattura (id_cr_sottovoce, descrizione) " + 
				"VALUES (:id_cr_sotto_voce, '" + descrizione + "')";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(vf);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_vf" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param vf
	 * @return id
	 */
	@Transactional()
	public String update(VoceFattura vf) {
		
		String descrizione = "(" + vf.getId_cr_sotto_voce() + ") - " + vf.getDescrizione().substring(vf.getDescrizione().indexOf("- ")+2,vf.getDescrizione().length());
		vf.setDescrizione(descrizione);
		final String query = "UPDATE org_x_voci_fattura " + 
				"SET " + 
				"id_cr_sottovoce = :id_cr_sotto_voce, " + 
				"descrizione = :descrizione " + 
				"WHERE id_vf = :id_vf";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(vf);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return vf.getId_vf();
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteVoceFatturaByID(String id) {
		String query = "DELETE FROM org_x_voci_fattura WHERE id_vf = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	private static class VoceFatturaRowMapper extends BaseRowMapper<VoceFattura> {
		public VoceFatturaRowMapper() { }		
		@Override
		public VoceFattura mapRowImpl(ResultSet rs, int i) throws SQLException {
			VoceFattura o = new VoceFattura();
			o.setId_vf(rs.getString("id_vf"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_cr_sotto_voce(rs.getString("id_cr_sottovoce"));
			o.setSottovoce(rs.getString("sottovoce"));
			o.setLocked(rs.getInt("locked"));
			return o;
		}
	}
	
	private static class VoceFatturaSimpleRowMapper extends BaseRowMapper<VoceFattura> {
		public VoceFatturaSimpleRowMapper() { }		
		@Override
		public VoceFattura mapRowImpl(ResultSet rs, int i) throws SQLException {
			VoceFattura o = new VoceFattura();
			o.setId_vf(rs.getString("id_vf"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_cr_sotto_voce(rs.getString("id_cr_sottovoce"));
			o.setLocked(rs.getInt("locked"));
			return o;
		}
	}

}
