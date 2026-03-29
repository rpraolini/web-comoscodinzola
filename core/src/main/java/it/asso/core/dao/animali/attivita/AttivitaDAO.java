package it.asso.core.dao.animali.attivita;

import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.animali.attivita.Attivita;
import it.asso.core.model.animali.attivita.Stato;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Repository("animaleAttivitaDAO")
public class AttivitaDAO {

	private static Logger logger = LoggerFactory.getLogger(AttivitaDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public AttivitaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	@Autowired
	private AnimaleDAO animaleDao;
	
	/**
	 * @param attivita
	 * @return
	 */
	
	@Transactional()
	public String save(Attivita attivita) {

		final String query = "INSERT INTO an_r_attivita " + 
				"(id_attivita, " + 
				"id_animale, " + 
				"note_attivita, " + 
				"id_utente, " + 
				"account, " + 
				"id_stato_padre, " + 
				"id_stato_precedente) " + 
				" VALUES " + 
				"(:id_attivita," + 
				":id_animale," + 
				":note_attivita," + 
				":id_utente," + 
				":account," + 
				":id_stato_padre," + 
				":id_stato_precedente)";

		/* Setto lo stato precedente dell'istanza */
		attivita.setId_stato_precedente(animaleDao.getStato(attivita.getId_animale()));
		/* Recupero lo stato padre dell'attivita*/
		attivita.setId_stato_padre(getStatoPadre(attivita.getId_attivita()));
		
		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(attivita);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_r_attivita" });

		/* salvo lo stato dell'istanza */
		setStato(attivita.getId_animale(), attivita.getId_attivita());
		
		return String.valueOf(keyHolder.getKey());
	}
	
	
	/**
	 * @param idAttivita
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getStatoPadre(String idAttivita) {
		
		String queryStr = "SELECT id_stato_padre " + 
				" FROM an_x_attivita " + 
				" WHERE id_attivita = ?";

		try{
			return  jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { idAttivita });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param idAnimale
	 * @return List<Attivita>
	 */
	@Transactional(readOnly = true)
	public List<Attivita> getAttivitaByAnimale(String idAnimale) {
		
		String queryStr = "SELECT a.id_r_attivita," + 
				"    a.id_attivita," + 
				"    a.id_animale," + 
				"    nullif(DATE_FORMAT(a.dt_attivita, '%d/%m/%Y %H:%i:%s'),'') dt_attivita," + 
				"    a.note_attivita," + 
				"    a.id_utente," + 
				"    a.account," + 
				"    a.id_stato_padre," + 
				"    a.id_stato_precedente," + 
				"    b.descr_attivita" + 
				" FROM an_r_attivita a, an_x_attivita b" + 
				" WHERE a.id_attivita = b.id_attivita" + 
				"	and a.id_animale = ? ORDER BY a.dt_attivita";

		try{
			return  jdbcTemplate.query(queryStr, new AttivitaRowMapper(), new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Stato>
	 */
	@Transactional(readOnly = true)
	public List<Stato> getStati() {
		
		String queryStr = "SELECT id_stato, descr_stato FROM an_x_stati order by descr_stato";

		try{
			return  jdbcTemplate.query(queryStr, new StatoRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	private static class AttivitaRowMapper extends BaseRowMapper<Attivita> {
		public AttivitaRowMapper() {
		}		

		@Override
		public Attivita mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Attivita o = new Attivita();
			o.setId_r_attivita(rs.getString("id_r_attivita"));
			o.setId_attivita(rs.getString("id_attivita"));
			o.setId_animale(rs.getString("id_animale"));
			o.setId_utente(rs.getString("id_utente"));
			o.setId_stato_precedente(rs.getString("id_stato_precedente"));
			o.setId_stato_padre(rs.getString("id_stato_padre"));
			o.setNote_attivita(rs.getString("note_attivita"));
			o.setAccount(rs.getString("account"));
			o.setDt_attivita(rs.getString("dt_attivita"));
			o.setDescr_attivita(rs.getString("descr_attivita"));
			return o;
		}
	}
	

	private static class StatoRowMapper extends BaseRowMapper<Stato> {
		public StatoRowMapper() {
		}		

		@Override
		public Stato mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Stato o = new Stato();
			o.setId_stato(rs.getString("id_stato"));
			o.setDescr_stato(rs.getString("descr_stato"));
			return o;
		}
	}
	
	/**
	 * @param idAnimale, idStato
	 * @return int
	 */
	@Transactional()
	public void setStato(String idAnimale, String idAttivita) {
		String idStato = jdbcTemplate.queryForObject("select id_stato_figlio from an_x_attivita where id_attivita = ?", String.class, new Object[] { idAttivita });
		String queryStr = "UPDATE an_animale SET id_stato = ? WHERE id_animale = ? ";
		jdbcTemplate.update(queryStr, idStato, idAnimale);
	}

	
	

}
