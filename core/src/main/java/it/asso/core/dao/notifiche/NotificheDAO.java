package it.asso.core.dao.notifiche;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.notifiche.EventiNotifica;
import it.asso.core.model.notifiche.Notifica;
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
public class NotificheDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotificheDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public NotificheDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param id
	 * @return Notifica
	 */
	@Transactional(readOnly = true)
	public Notifica getByID(String id) {
		Notifica notifica = null;
		String queryStr = "SELECT a.id_notifica, a.titolo_notifica, a.descr_notifica, a.titolo_testo, a.descr_testo, a.attiva, nullif(DATE_FORMAT(a.dt_notifica, '%d/%m/%Y'),'') dt_notifica, b.evento, a.id_evento FROM org_notifiche a, org_notifiche_eventi b WHERE a.id_evento=b.id_evento and  a.id_notifica = ?";
		try{
			notifica =  jdbcTemplate.queryForObject(queryStr, new NotificaRowMapper(), new Object[] { id });
			return notifica;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return notifica;
		}
	}
	
	
	/**
	 * @param 
	 * @return List<Notifica>
	 */
	@Transactional(readOnly = true)
	public List<Notifica> getAll(String search) {
		List<Notifica> notifica = null;
		String queryStr = "SELECT a.id_notifica, a.titolo_notifica, a.descr_notifica, a.titolo_testo, a.descr_testo, a.attiva, nullif(DATE_FORMAT(a.dt_notifica, '%d/%m/%Y'),'') dt_notifica, b.evento, a.id_evento FROM org_notifiche a, org_notifiche_eventi b WHERE a.id_evento=b.id_evento and upper(concat(titolo_notifica, descr_notifica, titolo_testo,descr_testo)) like ? "
				+ " ORDER BY a.dt_notifica desc, a.id_notifica desc limit 30";
		try{
			notifica =  jdbcTemplate.query(queryStr, new NotificaRowMapper(), new Object[] { "%" + search.toUpperCase() + "%" });
			return notifica;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return notifica;
		}
	}
	
	/**
	 * @param 
	 * @return List<Notifica>
	 */
	@Transactional(readOnly = true)
	public List<Notifica> getRange(String offset, String limit) {

		List<Notifica> notifica = null;
		String queryStr = "SELECT a.id_notifica, a.titolo_notifica, a.descr_notifica, a.titolo_testo, a.descr_testo, a.attiva, nullif(DATE_FORMAT(a.dt_notifica, '%d/%m/%Y'),'') dt_notifica, b.evento, a.id_evento "
				+ " FROM org_notifiche a, org_notifiche_eventi b "
				+ " WHERE a.id_evento=b.id_evento"
				+ " ORDER BY a.dt_notifica desc, a.id_notifica desc limit " + offset + ","+ limit;
		try{
			notifica =  jdbcTemplate.query(queryStr, new NotificaRowMapper());
			return notifica;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return notifica;
		}
	}
	
	/**
	 * @param 
	 * @return List<String>
	 */
	@Transactional(readOnly = true)
	public List<EventiNotifica> getEventi() {
		List<EventiNotifica> notifica = new ArrayList<EventiNotifica>();
		String queryStr = "SELECT id_evento, evento, attivo FROM org_notifiche_eventi ORDER BY evento";
		try{
			notifica =  jdbcTemplate.query(queryStr, new EventiNotificaRowMapper());
			return notifica;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return notifica;
		}
	}
	
	/**
	 * @param org
	 * @return idNotifica
	 */
	@Transactional()
	public String saveOrUpdate(Notifica org) {
		String idNotifica = "";

		if (org.getId_notifica() == null) {
			idNotifica = save(org);
		} else {
			idNotifica = update(org);
		}

		return idNotifica;
	}
	
	/**
	 * @param org
	 * @return
	 */
	@Transactional()
	public String save(Notifica org) {
		final String query = "INSERT INTO org_notifiche (titolo_notifica, descr_notifica, titolo_testo, descr_testo, attiva, id_evento) " + 
				"VALUES (:titolo_notifica, :descr_notifica, :titolo_testo, :descr_testo, :attiva, :id_evento)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(org);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_notifica" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param notifica
	 * @return
	 */
	@Transactional()
	public String update(Notifica notifica) {

		final String query = "UPDATE org_notifiche " + 
				"SET " + 
					"titolo_notifica = :titolo_notifica, " +
					"descr_notifica = :descr_notifica, " + 
					"titolo_testo = :titolo_testo, " + 
					"descr_testo = :descr_testo, " + 
					"attiva = :attiva, " + 
					"id_evento = :id_evento " + 
				"WHERE id_notifica = :id_notifica";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(notifica);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return notifica.getId_notifica();
	}
	
	/**
	 * @param id
	 * @return
	 */
	@Transactional()
	public String deleteByID(String id) {
		String query = "DELETE FROM org_notifiche WHERE id_notifica = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;
	}
	
	private static class NotificaRowMapper extends BaseRowMapper<Notifica> {
		public NotificaRowMapper() {
		}		
		@Override
		public Notifica mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Notifica o = new Notifica();
			o.setId_notifica(rs.getString("id_notifica"));
			o.setAttiva(rs.getString("attiva"));
			o.setTitolo_notifica(rs.getString("titolo_notifica"));
			o.setDescr_notifica(rs.getString("descr_notifica"));
			o.setTitolo_testo(rs.getString("titolo_testo"));
			o.setDescr_testo(rs.getString("descr_testo"));
			o.setDt_notifica(rs.getString("dt_notifica"));
			o.setId_evento(rs.getString("id_evento"));
			o.setEvento(rs.getString("evento"));
			return o;
		}
	}
	
	private static class EventiNotificaRowMapper extends BaseRowMapper<EventiNotifica> {
		public EventiNotificaRowMapper() {
		}		
		@Override
		public EventiNotifica mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			EventiNotifica o = new EventiNotifica();
			o.setAttivo(rs.getString("attivo"));
			o.setId_evento(rs.getString("id_evento"));
			o.setEvento(rs.getString("evento"));
			return o;
		}
	}
	
	

}
