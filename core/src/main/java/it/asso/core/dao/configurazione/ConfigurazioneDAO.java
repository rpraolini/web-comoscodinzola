package it.asso.core.dao.configurazione;

import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.configurazione.Configurazione;
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
public class ConfigurazioneDAO {

	private static Logger logger = LoggerFactory.getLogger(ConfigurazioneDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public ConfigurazioneDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	

	/**
	 * @param 
	 * @return List<Configurazione>
	 */
	@Transactional(readOnly = true)
	public List<Configurazione> getAllConfigurazioni(String tipo) {
		String query = "SELECT chiave, descrizione FROM x_configurazione where tipo_configurazione like ?";
		try{
			return jdbcTemplate.query(query, new ConfigurazioneRowMapper(), new Object[] { tipo });
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private static class ConfigurazioneRowMapper extends BaseRowMapper<Configurazione> {
		public ConfigurazioneRowMapper() {}		
		@Override
		public Configurazione mapRowImpl(ResultSet rs, int i) throws SQLException {
			Configurazione o = new Configurazione();
			o.setChiave(rs.getString("chiave"));
			o.setDescrizione(rs.getString("descrizione"));
			return o;
		}
	}
}
