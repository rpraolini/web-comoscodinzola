package it.asso.core.dao.organizzazione.contabilita;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.organizzazione.contabilita.ContoCorrente;
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
import java.util.List;
@Repository
public class ContoCorrenteDAO {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ContoCorrenteDAO.class);

    public ContoCorrenteDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param id
	 * @return ContoCorrente
	 */
	@Transactional(readOnly = true)
	public ContoCorrente getByID(String id) {
		ContoCorrente cc = null;
		String queryStr = "SELECT id_cc, id_organizzazione, num_cc, iban, ente, nullif(DATE_FORMAT(dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, preferito FROM org_conti_correnti where id_cc = ?";
		try{
			cc =  jdbcTemplate.queryForObject(queryStr, new ContoCorrenteRowMapper(), new Object[] { id });
			return cc;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return cc;
		}
	}
	
	/**
	 * @return ContoCorrente
	 */
	@Transactional(readOnly = true)
	public ContoCorrente getActual() {
		ContoCorrente cc = null;
		String queryStr = "SELECT id_cc, id_organizzazione, num_cc, iban, ente, nullif(DATE_FORMAT(dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, preferito FROM org_conti_correnti where preferito = 1";
		try{
			cc =  jdbcTemplate.queryForObject(queryStr, new ContoCorrenteRowMapper());
			return cc;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return cc;
		}
	}
	
	/**
	 * @param id
	 * @return List<ContoCorrente>
	 */
	@Transactional(readOnly = true)
	public List<ContoCorrente> getContiByIDOrganizzazione(String id) {
		List<ContoCorrente>  cc = null;
		String queryStr = "SELECT id_cc, id_organizzazione, num_cc, iban, ente, nullif(DATE_FORMAT(dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, preferito FROM org_conti_correnti where id_organizzazione = ?";
		try{
			cc =  jdbcTemplate.query(queryStr, new ContoCorrenteRowMapper(), new Object[] { id });
			return cc;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return cc;
		}
	}
	
	/**
	 * @param cc
	 * @return
	 */
	@Transactional()
	public String saveOrUpdate(ContoCorrente cc) {
		String idContoCorrente = "";
		if(Def.NUM_UNO.equals(cc.getPreferito())) {
			resetPreferito(cc.getId_organizzazione());
		}
		if (cc.getId_cc() == null) {
			idContoCorrente = save(cc);
		} else {
			idContoCorrente = update(cc);
		}

		return idContoCorrente;
	}

	/**
	 * @param cc
	 * @return
	 */
	@Transactional()
	public String save(ContoCorrente cc) {
		final String query = "INSERT INTO org_conti_correnti (id_organizzazione, num_cc, iban, ente, preferito) " + 
				"VALUES (:id_organizzazione, :num_cc, :iban, :ente, :preferito)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(cc);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_cc" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param cc
	 * @return
	 */
	@Transactional()
	public String update(ContoCorrente cc) {

		final String query = "UPDATE org_conti_correnti " + 
				"SET " + 
				"id_organizzazione = :id_organizzazione, " + 
				"num_cc = :num_cc, " + 
				"iban = :iban, " + 
				"ente = :ente, " + 
				"preferito = :preferito " + 
				"WHERE id_cc = :id_cc";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(cc);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return cc.getId_cc();
	}
	
	/**
	 * @param idOrganizzazione
	 * @return
	 */
	@Transactional()
	public void resetPreferito(String idOrganizzazione) {

		final String query = "UPDATE org_conti_correnti " + 
				"SET " + 
				"preferito = '0' " + 
				"WHERE id_organizzazione = ?";
		jdbcTemplate.update(query, new Object[] { idOrganizzazione });
		
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteByID(String id) {
		String query = "DELETE FROM org_conti_correnti WHERE id_cc = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	
	private static class ContoCorrenteRowMapper extends BaseRowMapper<ContoCorrente> {
		public ContoCorrenteRowMapper() {
		}		
		@Override
		public ContoCorrente mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			ContoCorrente o = new ContoCorrente();
			o.setId_organizzazione(rs.getString("id_organizzazione"));
			o.setId_cc(rs.getString("id_cc"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setEnte(rs.getString("ente"));
			o.setIban(rs.getString("iban"));
			o.setNum_cc(rs.getString("num_cc"));
			o.setPreferito(rs.getString("preferito"));
			return o;
		}
	}
}
