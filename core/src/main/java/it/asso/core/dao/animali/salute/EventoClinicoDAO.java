package it.asso.core.dao.animali.salute;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.animali.salute.EventoClinico;
import it.asso.core.model.animali.salute.TipoEvento;
import it.asso.core.model.animali.salute.TipoEventoClinico;
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
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Repository
public class EventoClinicoDAO{
	
private static final Logger logger = LoggerFactory.getLogger(EventoClinicoDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public EventoClinicoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	

	/**
	 * @param id
	 * @return EventoClinico
	 */
	@Transactional(readOnly = true)
	public EventoClinico getEventoClinicoById(String id) {
		
		
		String queryStr = "SELECT a.id_evento, a.id_animale, " + 
				"    a.id_tipo_evento, " + 
				"    nullif(DATE_FORMAT(a.dt_evento,'%d/%m/%Y'),'') dt_evento, " + 
				"    nullif(DATE_FORMAT(a.dt_richiamo,'%d/%m/%Y'),'') dt_richiamo, " + 
				"    b.evento, a.note, c.id_tipo_evento_clinico, c.tipo_evento_clinico, b.note_tipo_evento " +
				" FROM an_r_evento_clinico a, an_x_evento_clinico b, an_x_tipo_evento_clinico c " + 
				" WHERE a.id_tipo_evento=b.id_tipo_evento and b.id_tipo_evento_clinico = c.id_tipo_evento_clinico and a.id_evento = ? ";

		try{
			return jdbcTemplate.queryForObject(queryStr, new EventoClinicoRowMapper(), new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<EventoClinico>
	 */
	@Transactional(readOnly = true)
	public List<EventoClinico> getEventiCliniciByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT a.id_evento, a.id_animale, " + 
				"    a.id_tipo_evento, " + 
				"    nullif(DATE_FORMAT(a.dt_evento,'%d/%m/%Y'),'') dt_evento, " + 
				"    nullif(DATE_FORMAT(a.dt_richiamo,'%d/%m/%Y'),'') dt_richiamo, " + 
				"    b.evento, a.note, c.id_tipo_evento_clinico, c.tipo_evento_clinico, b.note_tipo_evento " +
				" FROM an_r_evento_clinico a, an_x_evento_clinico b, an_x_tipo_evento_clinico c " + 
				" WHERE a.id_tipo_evento=b.id_tipo_evento and b.id_tipo_evento_clinico = c.id_tipo_evento_clinico and a.id_animale = ? " +
				" ORDER BY a.dt_evento";

		try{
			return jdbcTemplate.query(queryStr, new EventoClinicoRowMapper(), new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	/**
	 * @param eventoClinico
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(EventoClinico eventoClinico) {
		String id = "";
		
		if (eventoClinico.getId_evento()  == null) {
			id = save(eventoClinico);
		} else {
			id = update(eventoClinico);
		}
		return id;
	}

	/**
	 * @param eventoClinico
	 * @return
	 */
	
	@Transactional()
	public String save(EventoClinico eventoClinico) {

		final String query = "INSERT INTO an_r_evento_clinico " + 
										"(id_animale, " +
										"id_tipo_evento," + 
										"dt_evento, dt_richiamo, note)" +
										" VALUES " + 
										"(:id_animale," +
										":id_tipo_evento, " +
										"str_to_date(nullif(:dt_evento,''), '%d/%m/%Y')," +
										"str_to_date(nullif(:dt_richiamo,''), '%d/%m/%Y'), :note" +
										")";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(eventoClinico);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_evento" });

		return String.valueOf(keyHolder.getKey());
	}

	/**
	 * @param eventoClinico
	 * @return
	 */
	
	@Transactional()
	public String update(EventoClinico eventoClinico) {

		final String query = "UPDATE an_r_evento_clinico " + 
				" SET " + 
				"id_animale = :id_animale," +
				"id_tipo_evento = :id_tipo_evento, " +
				"dt_evento = str_to_date(nullif(:dt_evento,''), '%d/%m/%Y'), " +
				"dt_richiamo = str_to_date(nullif(:dt_richiamo,''), '%d/%m/%Y'), " +
				"note = :note " +
				" WHERE id_evento = :id_evento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(eventoClinico);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return eventoClinico.getId_evento();
	}
	
	/**
	 * @param eventoClinico
	 * @return
	 */
	
	@Transactional()
	public String delete(EventoClinico eventoClinico) {

		final String query = "DELETE FROM an_r_evento_clinico WHERE id_evento = :id_evento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(eventoClinico);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return eventoClinico.getId_evento();
	}
	
	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String delete(String id) {

		EventoClinico eventoClinico = new EventoClinico();
		eventoClinico.setId_evento(id);

		return delete(eventoClinico);
	}
	
	
	/**
	 * @param 
	 * @return List<EventoClinico>
	 */
	@Transactional(readOnly = true)
	public boolean isSterilizzato(String idAnimale) {
		boolean check = false;
		
		String queryStr = "SELECT count(id_animale) FROM an_r_evento_clinico where id_animale= ? and id_tipo_evento = " + Def.EVE_STERILIZZATO;

		try{
			int count =  jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { idAnimale });
			check = count > 0 ? true : false;
			return check;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return false;
		}
	}
	
	
	/**
	 * @param tipoEventoClinico
	 * @return
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	
	@Transactional()
	public String saveOrUpdate(TipoEvento tipoEventoClinico) throws SQLIntegrityConstraintViolationException {
		String id = "";
		
		if(checkTipoEventoClinico(tipoEventoClinico.getEvento()) > 0) {
			throw new SQLIntegrityConstraintViolationException(); 
		}
		
		if (tipoEventoClinico.getId_tipo_evento()  == null) {
			id = save(tipoEventoClinico);
		} else {
			id = update(tipoEventoClinico);
		}
		return id;
	}
	
	/**
	 * @param tipoEventoClinico
	 * @return
	 */
	
	@Transactional()
	public String save(TipoEvento tipoEventoClinico) {

		final String query = "INSERT INTO an_x_evento_clinico (evento, id_tipo_evento_clinico, note_tipo_evento)" +
										" VALUES (:evento, :id_tipo_evento_clinico, :note_tipo_evento)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tipoEventoClinico);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_tipo_evento" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param tipoEventoClinico
	 * @return
	 */
	
	@Transactional()
	public String update(TipoEvento tipoEventoClinico) {

		final String query = "UPDATE an_x_evento_clinico " + 
				" SET evento = :evento, id_tipo_evento_clinico = :id_tipo_evento_clinico, note_tipo_evento = :note_tipo_evento WHERE id_tipo_evento = :id_tipo_evento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tipoEventoClinico);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return tipoEventoClinico.getId_tipo_evento();
	}
	
	
	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String deleteTipoEvento(String id) throws SQLIntegrityConstraintViolationException{

		final String query = "DELETE FROM an_x_evento_clinico WHERE id_tipo_evento = ?";

		jdbcTemplate.update(query, new Object[] { id });

		return id;
	}
	
	/**
	 * @param evento
	 * @return int
	 */
	@Transactional()
	private int checkTipoEvento(String evento) {
		
		String queryStr = "SELECT count(evento) FROM an_x_evento_clinico WHERE binary evento = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { evento });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
		
	
	private static class EventoClinicoRowMapper extends BaseRowMapper<EventoClinico> {
		public EventoClinicoRowMapper() {
		}		
		@Override
		public EventoClinico mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			EventoClinico o = new EventoClinico();
			o.setId_evento(rs.getString("id_evento"));
			o.setId_animale(rs.getString("id_animale"));
			o.setId_tipo_evento(rs.getString("id_tipo_evento"));
			o.setDt_evento(rs.getString("dt_evento"));
			o.setDt_richiamo(rs.getString("dt_richiamo"));
			o.setEvento(rs.getString("evento"));
			o.setNote(rs.getString("note"));
			o.setId_tipo_evento_clinico(rs.getString("id_tipo_evento_clinico"));
			o.setEvento_clinico(rs.getString("tipo_evento_clinico"));
			o.setNote_tipo_evento(rs.getString("note_tipo_evento"));
			return o;
		}
	}
	
	
	/**
	 * @param id_tipo_evento_clinico
	 * @return List<TipoEvento>
	 */
	@Transactional(readOnly = true)
	public List<TipoEvento> getTipiEventi(String id_tipo_evento_clinico) {
		
		String queryStr = "SELECT a.id_tipo_evento,  a.evento, b.id_tipo_evento_clinico, b.tipo_evento_clinico, a.note_tipo_evento " +
				" FROM an_x_evento_clinico a, an_x_tipo_evento_clinico b " +
				" WHERE a.id_tipo_evento_clinico = b.id_tipo_evento_clinico and a.id_tipo_evento_clinico like ?" + 
				" ORDER BY evento";

		try{
			return jdbcTemplate.query(queryStr, new TipoEventoRowMapper(),  new Object[] { id_tipo_evento_clinico });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<TipoEvento>
	 */
	@Transactional(readOnly = true)
	public List<TipoEvento> getTipiEventi() {
		
		String queryStr = "SELECT a.id_tipo_evento,  a.evento, b.id_tipo_evento_clinico, b.tipo_evento_clinico, a.note_tipo_evento " +
				" FROM an_x_evento_clinico a, an_x_tipo_evento_clinico b " +
				" WHERE a.id_tipo_evento_clinico = b.id_tipo_evento_clinico " + 
				" ORDER BY evento";

		try{
			return jdbcTemplate.query(queryStr,  new TipoEventoRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private static class TipoEventoRowMapper extends BaseRowMapper<TipoEvento> {
		public TipoEventoRowMapper() {
		}		
		@Override
		public TipoEvento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoEvento o = new TipoEvento();
			o.setId_tipo_evento(rs.getString("id_tipo_evento"));
			o.setEvento(rs.getString("evento"));
			o.setId_tipo_evento_clinico(rs.getString("id_tipo_evento_clinico"));
			o.setEvento_clinico(rs.getString("tipo_evento_clinico"));
			o.setNote_tipo_evento(rs.getString("note_tipo_evento"));
			return o;
		}
	}
	
	/**
	 * @param 
	 * @return List<TipoEventoClinico>
	 */
	@Transactional(readOnly = true)
	public List<TipoEventoClinico> getTipiEventiClinici() {
		
		String queryStr = "SELECT id_tipo_evento_clinico,  tipo_evento_clinico, colore " +
				" FROM an_x_tipo_evento_clinico " + 
				" ORDER BY tipo_evento_clinico";

		try{
			return jdbcTemplate.query(queryStr,  new TipoEventoClinicoRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return int
	 */
	@Transactional()
	private int checkTipoEventoClinico(String id) {
		
		
		String queryStr = "SELECT count(tipo_evento_clinico) FROM an_x_tipo_evento_clinico WHERE binary tipo_evento_clinico = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	

	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String deleteTipoEventoClinico(String id) throws SQLIntegrityConstraintViolationException{

		final String query = "DELETE FROM an_x_tipo_evento_clinico WHERE id_tipo_evento_clinico = ?";

		jdbcTemplate.update(query, new Object[] { id });

		return id;
	}
	
	private static class TipoEventoClinicoRowMapper extends BaseRowMapper<TipoEventoClinico> {
		public TipoEventoClinicoRowMapper() {
		}		
		@Override
		public TipoEventoClinico mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoEventoClinico o = new TipoEventoClinico();
			o.setId_tipo_evento_clinico(rs.getString("id_tipo_evento_clinico"));
			o.setEvento_clinico(rs.getString("tipo_evento_clinico"));
			o.setColore(rs.getString("colore"));
			return o;
		}
	}
}
