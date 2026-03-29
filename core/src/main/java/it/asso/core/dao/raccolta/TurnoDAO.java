package it.asso.core.dao.raccolta;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.raccolta.ContattiEvento;
import it.asso.core.model.raccolta.Turno;
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
import java.util.ArrayList;
import java.util.List;
@Repository
public class TurnoDAO {

	private static Logger logger = LoggerFactory.getLogger(TurnoDAO.class);
	
	private final ContattoDAO contattoDao;

    private final JdbcTemplate jdbcTemplate;

    public TurnoDAO(JdbcTemplate jdbcTemplate,ContattoDAO contattoDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.contattoDao = contattoDao;
    }

	/**
	 * @param idTurno
	 * @return Turno
	 */
	@Transactional(readOnly = true)
	public Turno getByID(String idTurno) {
		Turno evento = null;
		String queryStr = " SELECT id_turno, id_evento, orario_da, orario_a, ordine, note FROM rc_r_evento_turni WHERE id_turno = ?";
		try{
			evento =  jdbcTemplate.queryForObject(queryStr, new TurnoRowMapper(), new Object[] { idTurno });
			return evento;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return evento;
		}
	}
	
	/**
	 * @param idEvento
	 * @return List<Turno>
	 */
	@Transactional(readOnly = true)
	public List<Turno> getByIDEvento(String idEvento) {
		List<Turno> eventi = null;
		String queryStr = " SELECT id_turno, id_evento, orario_da, orario_a, ordine, note FROM rc_r_evento_turni WHERE id_evento = ?";
		try{
			eventi =  jdbcTemplate.query(queryStr, new TurnoRowMapper(), new Object[] { idEvento });
			return eventi;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return eventi;
		}
	}
	
	/**
	 * @param idTurno
	 * @return List<Turno>
	 */
	@Transactional(readOnly = true)
	public List<Contatto> getContattiByIDTurno(String idTurno) {
		List<Contatto> contatti = new ArrayList<Contatto>();
		List<ContattiEvento> ce = null;
		String queryStr = " SELECT id_turno, id_evento, id_contatto FROM rc_r_evento_contatti WHERE id_turno = ?";
		try{
			ce =  jdbcTemplate.query(queryStr, new ContattoEventoRowMapper(), new Object[] { idTurno });
			for(ContattiEvento c : ce) {
				contatti.add(contattoDao.getByID(c.getId_contatto(), true));
			}
			
			return contatti;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return contatti;
		}
	}
	
	/**
	 * @param turno
	 * @return idTurno
	 */
	@Transactional()
	public String saveOrUpdate(Turno turno) {
		String idTurno = "";

		if (turno.getId_turno() == null) {
			idTurno = save(turno);
			logger.info("Inserito turno: " + idTurno + " " + turno.getId_turno() );
		} else {
			idTurno = update(turno);
		}
		
		return idTurno;
	}
	
	/**
	 * @param turno
	 * @return idTurno
	 */
	@Transactional()
	private String save(Turno turno) {

		String query = "INSERT INTO rc_r_evento_turni (id_evento,orario_da,orario_a,ordine) VALUES(:id_evento,:orario_da,:orario_a,:ordine)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(turno);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_turno" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param turno
	 * @return idTurno
	 */
	@Transactional()
	private String update(Turno turno) {

		String query = "UPDATE rc_r_evento_turni " + 
				" SET id_evento = :id_evento, orario_da = :orario_da, orario_a = :orario_a, ordine = :ordine, note = :note " + 
				" WHERE id_turno = :id_turno";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(turno);
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		
		return turno.getId_turno();
	}
	
	/**
	 * @param idTurno
	 * @return String
	 */
	@Transactional()
	public String deleteByID(String idTurno) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM rc_r_evento_turni WHERE id_turno = ?";
		jdbcTemplate.update(query, new Object[] { idTurno });
		return Def.STR_OK;
	}
	
	/**
	 * @param idTurno
	 * @return String
	 */
	@Transactional()
	public String deleteContattoByID(String idTurno,  String idContatto,  String idEvento) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM rc_r_evento_contatti WHERE id_turno = ? and id_contatto = ? and id_evento = ?";
		jdbcTemplate.update(query, new Object[] { idTurno, idContatto, idEvento });
		return Def.STR_OK;
	}
	
	private static class TurnoRowMapper extends BaseRowMapper<Turno> {
		public TurnoRowMapper() {
		}		
		@Override
		public Turno mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Turno o = new Turno();
			o.setId_evento(rs.getString("id_evento"));
			o.setId_turno(rs.getString("id_turno"));
			o.setOrario_a(rs.getString("orario_a"));
			o.setOrario_da(rs.getString("orario_da"));
			o.setOrdine(rs.getString("ordine"));
			o.setNote(rs.getString("note"));
			return o;
		}
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
