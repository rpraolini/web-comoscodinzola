package it.asso.core.dao.organizzazione.contabilita;

import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.organizzazione.contabilita.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RendicontoDAO{

    private static final Logger logger = LoggerFactory.getLogger(RendicontoDAO.class);


    private final JdbcTemplate jdbcTemplate;

    public RendicontoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
/* ---------------------------------- RENDICONTO ----------------------------------------------*/
	
	/**
	 * @param arg
	 * @return Rendiconto
	 */
	@Transactional(readOnly = true)
	public Rendiconto getRendicontoBySottoVoce(String arg) {
		Rendiconto o = null;
		String queryStr = "Select " + 
				"    a.id_sezione, " + 
				"    b.id_sotto_sezione, " + 
				"    c.id_cr_voce, " + 
				"    d.id_cr_sottovoce, " + 
				"    a.descrizione descr_sezione, " + 
				"    b.descrizione descr_sotto_sezione, " + 
				"    c.descrizione descr_voce, " + 
				"    d.descrizione descr_sotto_voce, " + 
				"    b.tipo_sotto_sezione " + 
				"From " + 
				"    org_rnd_cr_sottovoci d Inner Join " + 
				"    org_rnd_cr_voci c On d.id_cr_voce = c.id_cr_voce Inner Join " + 
				"    org_rnd_sotto_sezioni b On c.id_sotto_sezione = b.id_sotto_sezione Inner Join " + 
				"    org_rnd_sezioni a On b.id_sezione = a.id_sezione " + 
				"Where " + 
				"    d.id_cr_sottovoce = ?";
		try{
			o =  jdbcTemplate.queryForObject(queryStr, new RendicontoRowMapper(), new Object[] { arg });
			return o;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return o;
		}
	}
	
	private static class RendicontoRowMapper extends BaseRowMapper<Rendiconto> {
		public RendicontoRowMapper() { }		
		@Override
		public Rendiconto mapRowImpl(ResultSet rs, int i) throws SQLException {
			Rendiconto o = new Rendiconto();
			o.setId_sezione(rs.getString("id_sezione"));
			o.setId_sotto_sezione(rs.getString("id_sotto_sezione"));
			o.setId_cr_voce(rs.getString("id_cr_voce"));
			o.setId_cr_sotto_voce(rs.getString("id_cr_sottovoce"));
			
			o.setDescr_sezione(rs.getString("descr_sezione"));
			o.setDescr_sotto_sezione(rs.getString("descr_sotto_sezione"));
			o.setDescr_voce(rs.getString("descr_voce"));
			o.setDescr_sotto_voce(rs.getString("descr_sotto_voce"));
			o.setTipo_sotto_sezione(rs.getString("tipo_sotto_sezione"));
			return o;
		}
	}
	
	/* ---------------------------------- SEZIONI ----------------------------------------------*/
	
	/**
	 * @param 
	 * @return List<RndSezioni>
	 */
	@Transactional(readOnly = true)
	public List<RndSezioni> getRndSezioniAll() {
		List<RndSezioni> org = null;
		String queryStr = "SELECT id_sezione, descrizione FROM org_rnd_sezioni where id_sezione='A'";
		try{
			org =  jdbcTemplate.query(queryStr, new RndSezioniRowMapper());
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	private static class RndSezioniRowMapper extends BaseRowMapper<RndSezioni> {
		public RndSezioniRowMapper() { }		
		@Override
		public RndSezioni mapRowImpl(ResultSet rs, int i) throws SQLException {
			RndSezioni o = new RndSezioni();
			o.setId_sezione(rs.getString("id_sezione"));
			o.setDescrizione(rs.getString("descrizione"));
			return o;
		}
	}
	
/* ---------------------------------- SOTTO SEZIONI ----------------------------------------------*/
	
	/**
	 * @param 
	 * @return List<RndSottoSezioni>
	 */
	@Transactional(readOnly = true)
	public List<RndSottoSezioni> getRndSottoSezioniAll(String idSezione, String idTipoMovimento) {
		if("".equals(idTipoMovimento)) {
			idTipoMovimento = "%";
		}
		List<RndSottoSezioni> org = null;
		String queryStr = "SELECT id_sotto_sezione, id_sezione, descrizione FROM org_rnd_sotto_sezioni WHERE id_sezione = ? and tipo_sotto_sezione like (?) ORDER BY descrizione";
		try{
			org =  jdbcTemplate.query(queryStr, new RndSottoSezioniRowMapper(), new Object[] {idSezione, idTipoMovimento});
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	private static class RndSottoSezioniRowMapper extends BaseRowMapper<RndSottoSezioni> {
		public RndSottoSezioniRowMapper() { }		
		@Override
		public RndSottoSezioni mapRowImpl(ResultSet rs, int i) throws SQLException {
			RndSottoSezioni o = new RndSottoSezioni();
			o.setId_sezione(rs.getString("id_sezione"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_sotto_sezione(rs.getString("id_sotto_sezione"));
			return o;
		}
	}

/* ---------------------------------- CASSA RENDICONTO VOCI ----------------------------------------------*/
	
	/**
	 * @param 
	 * @return List<RndCrVoci>
	 */
	@Transactional(readOnly = true)
	public List<RndCrVoci> getRndCrVociAll(String idSottoSezione) {
		List<RndCrVoci> org = null;
		String queryStr = "SELECT id_sotto_sezione, id_cr_voce, descrizione FROM org_rnd_cr_voci WHERE id_sotto_sezione = ? ORDER BY descrizione";
		try{
			org =  jdbcTemplate.query(queryStr, new RndCrVociRowMapper(),new Object[] {idSottoSezione});
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	private static class RndCrVociRowMapper extends BaseRowMapper<RndCrVoci> {
		public RndCrVociRowMapper() { }		
		@Override
		public RndCrVoci mapRowImpl(ResultSet rs, int i) throws SQLException {
			RndCrVoci o = new RndCrVoci();
			o.setId_cr_voce(rs.getString("id_cr_voce"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_sotto_sezione(rs.getString("id_sotto_sezione"));
			return o;
		}
	}
	
/* ---------------------------------- CASSA RENDICONTO SOTTO VOCI ----------------------------------------------*/
	
	/**
	 * @param 
	 * @return List<RndCrSottoVoci>
	 */
	@Transactional(readOnly = true)
	public List<RndCrSottoVoci> getRndCrSottoVociAll(String idVoce) {
		List<RndCrSottoVoci> org = null;
		String queryStr = "SELECT id_cr_sottovoce, id_cr_voce, descrizione FROM org_rnd_cr_sottovoci WHERE id_cr_voce = ? ORDER BY descrizione";
		try{
			org =  jdbcTemplate.query(queryStr,new RndCrSottoVociRowMapper(), new Object[] {idVoce});
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	private static class RndCrSottoVociRowMapper extends BaseRowMapper<RndCrSottoVoci> {
		public RndCrSottoVociRowMapper() { }		
		@Override
		public RndCrSottoVoci mapRowImpl(ResultSet rs, int i) throws SQLException {
			RndCrSottoVoci o = new RndCrSottoVoci();
			o.setId_cr_voce(rs.getString("id_cr_voce"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_cr_sotto_voce(rs.getString("id_cr_sottovoce"));
			return o;
		}
	}
	
/* ---------------------------------- CASSA PATRIMONIALE VOCI ----------------------------------------------*/
	
	/**
	 * @param 
	 * @return List<RndCpVoci>
	 */
	@Transactional(readOnly = true)
	public List<RndCpVoci> getRndCpVociAll() {
		List<RndCpVoci> org = null;
		String queryStr = "SELECT id_sotto_sezione, id_cp_voce, descrizione FROM org_rnd_cp_voci ORDER BY descrizione";
		try{
			org =  jdbcTemplate.query(queryStr, new RndCpVociRowMapper());
			return org;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return org;
		}
	}
	
	private static class RndCpVociRowMapper extends BaseRowMapper<RndCpVoci> {
		public RndCpVociRowMapper() { }		
		@Override
		public RndCpVoci mapRowImpl(ResultSet rs, int i) throws SQLException {
			RndCpVoci o = new RndCpVoci();
			o.setId_cp_voce(rs.getString("id_cp_voce"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setId_sotto_sezione(rs.getString("id_sotto_sezione"));
			return o;
		}
	}
	
}
