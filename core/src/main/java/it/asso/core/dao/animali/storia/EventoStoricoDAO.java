package it.asso.core.dao.animali.storia;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.animali.storia.EventoStorico;
import it.asso.core.model.animali.storia.TipoEventoStorico;
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
public class EventoStoricoDAO{
	
private static Logger logger = LoggerFactory.getLogger(EventoStoricoDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public EventoStoricoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	/**
	 * @param 
	 * @return List<Luogo>
	 */
	@Transactional(readOnly = true)
	public List<EventoStorico> getEventiStoriciByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT a.id_evento, " + 
				"    a.id_animale, " + 
				"    a.id_tipo_evento, " + 
				"    nullif(DATE_FORMAT(a.dt_da, '%d/%m/%Y'),'') dt_da, " + 
				"    nullif(DATE_FORMAT(a.dt_a, '%d/%m/%Y'),'') dt_a, " + 
				"    a.id_contatto, " + 
				"    a.note, " + 
				"    a.account, " + 
				"    nullif(DATE_FORMAT(a.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, " + 
				"    b.evento, dt_da data_da, ct_gg, ct_mese " + 
				"FROM an_r_eventi_storico a, an_x_evento_storico b " + 
				"WHERE a.id_tipo_evento=b.id_tipo_evento and a.id_animale = ? ORDER BY a.dt_da asc";

		try{
			return jdbcTemplate.query(queryStr, new EventoStoricoRowMapper(), new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Luogo>
	 */
	@Transactional(readOnly = true)
	public int checkTipoEvento(String idAnimale, String idTipoEvento) {
		int idEvento = -1;
		String queryStr = "SELECT max(a.id_evento) id_evento FROM an_r_eventi_storico a where a.id_animale = ? and a.id_tipo_evento = ?";

		try{
			String o = jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { idAnimale, idTipoEvento });
			if(o != null) {
				idEvento = Integer.valueOf(o);
			}
			return idEvento;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return -1;
		}
	}
	
	/**
	 * @param 
	 * @return EventoStorico
	 */
	@Transactional(readOnly = true)
	public EventoStorico getEventiStoriciByIdEvento(String idEvento) {
		
		
		String queryStr = "SELECT a.id_evento, " + 
				"    a.id_animale, " + 
				"    a.id_tipo_evento, " + 
				"    nullif(DATE_FORMAT(a.dt_da, '%d/%m/%Y'),'') dt_da, " + 
				"    nullif(DATE_FORMAT(a.dt_a, '%d/%m/%Y'),'') dt_a, " + 
				"    a.id_contatto, " + 
				"    a.note, " + 
				"    a.account, " + 
				"    nullif(DATE_FORMAT(a.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, " + 
				"    b.evento, dt_da data_da, ct_gg, ct_mese " + 
				"FROM an_r_eventi_storico a, an_x_evento_storico b " + 
				"WHERE a.id_tipo_evento=b.id_tipo_evento and a.id_evento = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, new EventoStoricoRowMapper(), new Object[] { idEvento });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param luogo
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(EventoStorico luogo) {
		String idLuogo = "";
		
		if (luogo.getId_evento()  == null) {
			idLuogo = save(luogo);
		} else {
			idLuogo = update(luogo);
		}
		return idLuogo;
	}

	/**
	 * @param luogo
	 * @return
	 */
	
	@Transactional()
	public String save(EventoStorico luogo) {

		final String query = "INSERT INTO an_r_eventi_storico " + 
				"( " + 
				"id_animale, " + 
				"id_tipo_evento, " + 
				"dt_da, " + 
				"dt_a, " + 
				"id_contatto, " + 
				"note, " + 
				"account, ct_gg, ct_mese) " + 
				" VALUES " + 
				"( " + 
				":id_animale , " + 
				":id_tipo_evento , " + 
				"str_to_date(nullif(:dt_da,''), '%d/%m/%Y') , " + 
				"str_to_date(nullif(:dt_a,''), '%d/%m/%Y') , " + 
				":id_contatto , " + 
				":note , " + 
				":account, :ct_gg, :ct_mese )";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(luogo);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_evento" });

		return String.valueOf(keyHolder.getKey());
	}

	/**
	 * @param luogo
	 * @return
	 */
	
	@Transactional()
	public String update(EventoStorico luogo) {

		final String query = "UPDATE an_r_eventi_storico " + 
				" SET " + 
				" id_animale  =  :id_animale ," + 
				" id_tipo_evento  =  :id_tipo_evento ," + 
				" dt_da  =  str_to_date(nullif(:dt_da,''), '%d/%m/%Y') ," + 
				" dt_a  =  str_to_date(nullif(:dt_a,''), '%d/%m/%Y') ," + 
				" id_contatto  =  :id_contatto ," + 
				" note  =  :note ," + 
				" account  =  :account, ct_gg = :ct_gg, ct_mese = :ct_mese " + 
				"WHERE  id_evento  = :id_evento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(luogo);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return luogo.getId_evento();
	}
	
	/**
	 * @param luogo
	 * @return
	 */
	
	@Transactional()
	public String delete(EventoStorico luogo) {
		//TODO prima di eliminare verificare non ci siano costi per pensione e stallo
		final String query = "DELETE FROM an_r_eventi_storico WHERE id_evento = :id_evento";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(luogo);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return luogo.getId_evento();
	}
	
	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String delete(String id) {

		EventoStorico luogo = new EventoStorico();
		luogo.setId_evento(id);
		delete(luogo);

		return  Def.STR_OK;
	}
	
		
	private static class EventoStoricoRowMapper extends BaseRowMapper<EventoStorico> {
		public EventoStoricoRowMapper() {
		}		
		@Override
		public EventoStorico mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			EventoStorico o = new EventoStorico();
			o.setId_animale(rs.getString("id_animale"));
			o.setAccount(rs.getString("account"));
			o.setDt_a(rs.getString("dt_a"));
			o.setDt_da(rs.getString("dt_da"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setId_evento(rs.getString("id_evento"));
			o.setId_tipo_evento(rs.getString("id_tipo_evento"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setNote(rs.getString("note"));
			o.setEvento(rs.getString("evento"));
			o.setCt_gg(rs.getString("ct_gg"));
			o.setCt_mese(rs.getString("ct_mese"));
			return o;
		}
	}
	
	
	/**
	 * @param 
	 * @return List<Motivo>
	 */
	@Transactional(readOnly = true)
	public List<TipoEventoStorico> getTipiEventiStorici() {
		String queryStr = "SELECT a.id_tipo_evento, a.evento " + 
					"FROM an_x_evento_storico a " + 
					"ORDER BY a.evento";
		try{
			return jdbcTemplate.query(queryStr, new TipoEventoStoricoRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	private static class TipoEventoStoricoRowMapper extends BaseRowMapper<TipoEventoStorico> {
		public TipoEventoStoricoRowMapper() {
		}		
		@Override
		public TipoEventoStorico mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoEventoStorico o = new TipoEventoStorico();
			o.setId_tipo_evento(rs.getString("id_tipo_evento"));
			o.setTipo_evento(rs.getString("evento"));
			return o;
		}
	}
	
}
