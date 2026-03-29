package it.asso.core.dao.raccolta;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.raccolta.ContattiEvento;
import it.asso.core.model.raccolta.Evento;
import it.asso.core.model.raccolta.MerceEvento;
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
public class EventoDAO {

	private static final Logger logger = LoggerFactory.getLogger(EventoDAO.class);


    private final JdbcTemplate jdbcTemplate;

    public EventoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	/**
	 * @param idEvento
	 * @return Evento
	 */
	@Transactional(readOnly = true)
	public Evento getByID(String idEvento) {
		Evento evento = null;
		String queryStr = " SELECT a.id_evento, a.id_punto_raccolta, nullif(date_format(a.dt_evento, '%d/%m/%Y'),'') dt_evento, a.note, nullif(DATE_FORMAT(a.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, a.account, " + 
				"(select sum(peso) from rc_r_evento_merce where id_evento = a.id_evento) totale " + 
				" FROM rc_evento a " + 
				" WHERE a.id_evento =  ?";
		try{
			evento =  jdbcTemplate.queryForObject(queryStr, new EventoRowMapper(), new Object[] { idEvento });
			return evento;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return evento;
		}
	}
	
	/**
	 * @param idEvento
	 * @return String
	 */
	@Transactional(readOnly = true)
	public String getRaccoltoTotaleByID(String idEvento) {
		String totale = null;
		String queryStr = " SELECT sum(a.peso) FROM rc_r_evento_merce a WHERE a.id_evento = ?";
		try{
			totale =  jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { idEvento });
			return totale;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return totale;
		}
	}
	
	
	/**
	 * @param 
	 * @return List<Evento>
	 */
	@Transactional(readOnly = true)
	public List<Evento> getAll() {
		List<Evento> eventi = null;
		String queryStr = " SELECT a.id_evento, a.id_punto_raccolta, nullif(date_format(a.dt_evento, '%d/%m/%Y'),'') dt_evento, a.note, " + 
				"nullif(DATE_FORMAT(a.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, a.account, " + 
				"(select sum(peso) from rc_r_evento_merce where id_evento = a.id_evento) totale " + 
				"FROM rc_evento a " + 
				"ORDER BY a.dt_evento desc";
		try{
			eventi =  jdbcTemplate.query(queryStr, new EventoListRowMapper());
			return eventi;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return eventi;
		}
	}
	
	
	
	/**
	 * @param evento
	 * @return idEvento
	 */
	@Transactional()
	public String saveOrUpdate(Evento evento) {
		String idEvento = "";

		if (evento.getId_evento() == null) {
			idEvento = save(evento);
			logger.info("Inserito evento: " + idEvento + " " + evento.getId_evento() );
		} else {
			idEvento = update(evento);
		}
		
		return idEvento;
	}
	
	/**
	 * @param evento
	 * @return idEvento
	 */
	@Transactional()
	private String save(Evento evento) {

		String query = "INSERT INTO rc_evento (id_punto_raccolta, dt_evento, note, account) " + 
				"VALUES (:id_punto_raccolta, str_to_date(nullif(:dt_evento,''), '%d/%m/%Y'), :note, :account)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(evento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_evento" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param evento
	 * @return idEvento
	 */
	@Transactional()
	private String update(Evento evento) {

		String query = "UPDATE rc_evento " + 
				"SET " + 
				"id_punto_raccolta = :id_punto_raccolta, " + 
				"dt_evento = str_to_date(nullif(:dt_evento,''), '%d/%m/%Y'), " + 
				"note = :note, " + 
				"account = :account " + 
				"WHERE id_evento = :id_evento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(evento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		
		return evento.getId_evento();
	}
	
	
	/**
	 * @param idEvento
	 * @return String
	 */
	@Transactional()
	public String deleteByID(String idEvento) throws SQLIntegrityConstraintViolationException {
		String query = "delete from rc_r_evento_turni WHERE id_evento =?";
		jdbcTemplate.update(query, new Object[] { idEvento });
		query = "DELETE FROM rc_evento WHERE id_evento = ?";
		jdbcTemplate.update(query, new Object[] { idEvento });
		return Def.STR_OK;
	}
	
	
	
	private static class EventoRowMapper extends BaseRowMapper<Evento> {
		public EventoRowMapper() {
		}		
		@Override
		public Evento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Evento o = new Evento();
			o.setAccount(rs.getString("account"));
			o.setDt_evento(rs.getString("dt_evento"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setId_evento(rs.getString("id_evento"));
			o.setId_punto_raccolta(rs.getString("id_punto_raccolta"));
			o.setNote(rs.getString("note"));
			o.setTotale(rs.getString("totale"));
			return o;
		}
	}
	
	private static class EventoListRowMapper extends BaseRowMapper<Evento> {
		public EventoListRowMapper() {
		}		
		@Override
		public Evento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Evento o = new Evento();
			o.setAccount(rs.getString("account"));
			o.setDt_evento(rs.getString("dt_evento"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setId_evento(rs.getString("id_evento"));
			o.setId_punto_raccolta(rs.getString("id_punto_raccolta"));
			o.setNote(rs.getString("note"));
			o.setTotale(rs.getString("totale"));
			return o;
		}
	}
	
	
	/* --------------------------------- MERCE EVENTO ------------------------------------*/
	
	/**
	 * @param idEvento
	 * @return List<MerceEvento>
	 */
	@Transactional(readOnly = true)
	public List<MerceEvento> getMerceByIDEvento(String idEvento) {
		List<MerceEvento> merceEvento = null;
		String queryStr = "SELECT a.id_evento,a.id_merce,a.quantita,b.peso,b.descrizione,a.peso AS pesoTot,b.tipo_merce,c.descr_tipo_animale " + 
				"FROM rc_r_evento_merce a, rc_x_merceologia b , an_x_tipo_animale c " + 
				"WHERE a.id_merce = b.id_merce and b.id_tipo_animale = c.id_tipo_animale and a.id_evento = ?";
		try{
			merceEvento =  jdbcTemplate.query(queryStr, new MerceEventoRowMapper(), new Object[] { idEvento });
			return merceEvento;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return merceEvento;
		}
	}
	
	/**
	 * @param idEvento, idMerce
	 * @return int
	 */
	@Transactional(readOnly = true)
	private int exist(String idEvento, String idMerce) {
		int check = 0;
		String queryStr = "SELECT count(*) FROM rc_r_evento_merce WHERE id_evento = ? and id_merce = ?";
		try{
			check =  jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { idEvento, idMerce });
			return check;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return check;
		}
	}
	
	/**
	 * @param merceEvento
	 * @return idMerce
	 */
	@Transactional()
	public String saveOrUpdate(MerceEvento merceEvento) {
		String idMerceEvento = "";

		if (exist(merceEvento.getId_evento(), merceEvento.getId_merce()) == 0) {
			idMerceEvento = save(merceEvento);
			logger.info("Inserito Merce Evento: idMerce " + merceEvento.getId_merce() + " evento " + merceEvento.getId_evento() + " " );
		} else {
			idMerceEvento = update(merceEvento);
		}
		
		return idMerceEvento;
	}
	
	/**
	 * @param pesoTot
	 * @return idMerce
	 */
	@Transactional()
	public String saveOrUpdate(String idEvento, String idMerce, String quantita, String pesoTot) {
		String idMerceEvento = "";
		MerceEvento merceEvento = new MerceEvento();
		merceEvento.setId_evento(idEvento);
		merceEvento.setId_merce(idMerce);
		merceEvento.setQuantita(quantita);
		merceEvento.setPesoTot(pesoTot);
		saveOrUpdate(merceEvento);
		
		return idMerceEvento;
	}
	
	/**
	 * @param merceEvento
	 * @return idMerce
	 */
	@Transactional()
	private String save(MerceEvento merceEvento) {

		String query = "INSERT INTO rc_r_evento_merce (id_evento,id_merce,quantita,peso) " + 
				"VALUES (:id_evento, :id_merce, :quantita, :pesoTot)";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(merceEvento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return Def.STR_OK;
	}
	
	/**
	 * @param merceEvento
	 * @return idMerce
	 */
	@Transactional()
	private String update(MerceEvento merceEvento) {

		String query = "UPDATE rc_r_evento_merce " + 
				"SET quantita = :quantita, peso = :pesoTot " + 
				"WHERE id_evento = :id_evento and id_merce = :id_merce";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(merceEvento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return Def.STR_OK;
	}
	
	/**
	 * @param merceEvento
	 * @return String
	 */
	@Transactional()
	public String deleteEventoMerce(MerceEvento merceEvento) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM rc_r_evento_merce WHERE id_merce = :id_merce and id_evento = :id_evento";
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(merceEvento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		return Def.STR_OK;
	}
	
	/**
	 * @param idEvento,idMerce
	 * @return String
	 */
	@Transactional()
	public String deleteEventoMerce(String idEvento, String idMerce) throws SQLIntegrityConstraintViolationException {
		MerceEvento merceEvento = new MerceEvento();
		merceEvento.setId_evento(idEvento);
		merceEvento.setId_merce(idMerce);
		deleteEventoMerce(merceEvento);
		return Def.STR_OK;
	}
	
	private static class MerceEventoRowMapper extends BaseRowMapper<MerceEvento> {
		public MerceEventoRowMapper() {
		}		
		@Override
		public MerceEvento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			MerceEvento o = new MerceEvento();
			o.setId_merce(rs.getString("id_merce"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setPeso(rs.getString("peso"));
			o.setId_evento(rs.getString("id_evento"));
			o.setPesoTot(rs.getString("pesoTot"));
			o.setQuantita(rs.getString("quantita"));
			o.setTipo_animale(rs.getString("descr_tipo_animale"));
			o.setTipo_merce(rs.getString("tipo_merce"));
			return o;
		}
	}
	
/* --------------------------------- EVENTO CONTATTI ------------------------------------*/
	
	/**
	 * @param idEvento
	 * @return List<ContattiEvento>
	 */
	@Transactional(readOnly = true)
	public List<ContattiEvento> getContattiByIDEvento(String idEvento) {
		List<ContattiEvento> contattiEvento = null;
		String queryStr = "SELECT id_evento, id_contatto, id_turno FROM rc_r_evento_contatti "
				+ " WHERE id_evento = ?";
		try{
			contattiEvento =  jdbcTemplate.query(queryStr, new ContattoEventoRowMapper(), new Object[] { idEvento });
			return contattiEvento;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return contattiEvento;
		}
	}
	
	/**
	 * @param idEvento, idContatto, idTurno
	 * @return int
	 */
	@Transactional(readOnly = true)
	private int exist(String idEvento, String idContatto, String idTurno) {
		int check = 0;
		String queryStr = "SELECT count(*) FROM rc_r_evento_contatti WHERE id_evento = ? and id_contatto = ? and id_turno = ?";
		try{
			check =  jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { idEvento, idContatto, idTurno });
			return check;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return check;
		}
	}
	
	/**
	 * @param contattiEvento
	 * @return result
	 */
	@Transactional()
	public String saveOrUpdate(ContattiEvento contattiEvento) {
		String result = "";

		if (exist(contattiEvento.getId_evento(), contattiEvento.getId_contatto(), contattiEvento.getId_turno()) == 0) {
			result = save(contattiEvento);
			logger.info("Inserito Merce Evento: contatto " + contattiEvento.getId_contatto() + " evento " + contattiEvento.getId_evento() + " " );
		}		
		return result;
	}
	
	/**
	 * @param contattiEvento
	 * @return idMerce
	 */
	@Transactional()
	private String save(ContattiEvento contattiEvento) {

		String query = "INSERT INTO rc_r_evento_contatti (id_evento, id_contatto, id_turno ) " + 
				"VALUES (:id_evento, :id_contatto, :id_turno)";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(contattiEvento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return Def.STR_OK;
	}
		
	/**
	 * @param contattiEvento
	 * @return String
	 */
	@Transactional()
	public String deleteEventoMerce(ContattiEvento contattiEvento) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM rc_r_evento_contatti WHERE WHERE id_evento = :id_evento and id_contatto = :id_contatto and id_turno = :id_turno";
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(contattiEvento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		return Def.STR_OK;
	}
	
	
	private static class ContattoEventoRowMapper extends BaseRowMapper<ContattiEvento> {
		public ContattoEventoRowMapper() {
		}		
		@Override
		public ContattiEvento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			ContattiEvento o = new ContattiEvento();
			o.setId_evento(rs.getString("id_evento"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setId_turno(rs.getString("id_turno"));
			return o;
		}
	}
	
}
