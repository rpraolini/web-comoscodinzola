package it.asso.core.dao.organizzazione;

import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.organizzazione.TipoOrganizzazione;
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
public class TipoOrganizzazioneDAO{

    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TipoOrganizzazioneDAO.class);

    public TipoOrganizzazioneDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param id
	 * @return TipoOrganizzazione
	 */
	@Transactional(readOnly = true)
	public TipoOrganizzazione getByID(String id) {
		TipoOrganizzazione to = null;
		String queryStr = "SELECT id_tipo_organizzazione, sigla, descrizione FROM org_x_tipo_organizzazione where id_tipo_organizzazione = ?";
		try{
			to =  jdbcTemplate.queryForObject(queryStr, new TipoOrganizzazioneRowMapper(), new Object[] { id });
			return to;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return to;
		}
	}
	
	/**
	 * @return List<TipoOrganizzazione>
	 */
	@Transactional(readOnly = true)
	public List<TipoOrganizzazione> getTipoOrganizzazione() {
		List<TipoOrganizzazione>  cc = null;
		String queryStr = "SELECT id_tipo_organizzazione, sigla, descrizione FROM org_x_tipo_organizzazione";
		try{
			cc =  jdbcTemplate.query(queryStr, new TipoOrganizzazioneRowMapper());
			return cc;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return cc;
		}
	}
	
	
	private static class TipoOrganizzazioneRowMapper extends BaseRowMapper<TipoOrganizzazione> {
		public TipoOrganizzazioneRowMapper() {
		}		
		@Override
		public TipoOrganizzazione mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoOrganizzazione o = new TipoOrganizzazione();
			o.setId_tipo_organizzazione(rs.getString("id_tipo_organizzazione"));
			o.setSigla(rs.getString("sigla"));
			o.setDescrizione(rs.getString("descrizione"));
			return o;
		}
	}

}
