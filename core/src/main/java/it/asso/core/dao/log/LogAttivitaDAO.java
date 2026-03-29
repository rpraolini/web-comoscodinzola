package it.asso.core.dao.log;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.log.AttivitaSintetico;
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
public class LogAttivitaDAO {
    private static final Logger logger = LoggerFactory.getLogger(LogAttivitaDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public LogAttivitaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	
	/**
	 * @param utente
	 * @return String
	 */
	@Transactional()
	public void save(String attivita, String utente, String note, String session) {
		String query = "INSERT INTO log_attivita(attivita, note, utente, session_id) VALUES(?,?,?,?);";
		jdbcTemplate.update(query, new Object[] { attivita, note, utente, session });

	}
	

	/**
	 * @param 
	 * @return List<AttivitaSintetico>
	 */
	@Transactional()
	public List<AttivitaSintetico> getCountAttivita() {
		String queryStr = "SELECT count(id_attivita) numero, attivita, note " + 
				"FROM log_attivita " + 
				"GROUP BY attivita, note " + 
				"ORDER BY 1 desc, note;";
		try{
			return jdbcTemplate.query(queryStr, new AttivitaRowMapper());
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<AttivitaSintetico>
	 */
	@Transactional()
	public List<AttivitaSintetico> getCountAttivita(String arg) {
		String where = "";
		if(arg.equals(Def.NUM_UNO)) {
			where = "dt_attivita >= CURDATE() && dt_attivita < (CURDATE() + INTERVAL 1 DAY) ";
		}else if(arg.equals(Def.NUM_DUE)) {
			where = "dt_attivita between date_sub(now(),INTERVAL 1 WEEK) and now() ";
		}else if(arg.equals(Def.NUM_TRE)) {
			where = "dt_attivita BETWEEN DATE_ADD(NOW(), INTERVAL -1 MONTH ) and now() ";
		}
		String queryStr = "SELECT count(id_attivita) numero, attivita, note " + 
				"FROM log_attivita " + 
				"WHERE " + where +
				"GROUP BY attivita, note " + 
				"ORDER BY 1 desc, note;";
		try{
			return jdbcTemplate.query(queryStr, new AttivitaRowMapper());
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private static class AttivitaRowMapper extends BaseRowMapper<AttivitaSintetico> {
		public AttivitaRowMapper() {}		
		@Override
		public AttivitaSintetico mapRowImpl(ResultSet rs, int i) throws SQLException {
			AttivitaSintetico o = new AttivitaSintetico();
			o.setNumero(rs.getString("numero"));
			o.setAttivita(rs.getString("attivita"));
			o.setNote(rs.getString("note"));
			return o;
		}
	}
	

	

}
