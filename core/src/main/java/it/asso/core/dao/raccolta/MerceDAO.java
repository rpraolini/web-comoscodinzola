package it.asso.core.dao.raccolta;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.raccolta.Merce;
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
import java.util.Map;


@Repository
public class MerceDAO {

	private static final Logger logger = LoggerFactory.getLogger(MerceDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public MerceDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	/**
	 * @param idMerce
	 * @return Merce
	 */
	@Transactional(readOnly = true)
	public Merce getByID(String idMerce) {
		Merce evento = null;
		String queryStr = "SELECT a.id_merce,a.descrizione,a.peso,a.tipo_merce,a.id_tipo_animale,b.descr_tipo_animale " + 
				"FROM rc_x_merceologia a, an_x_tipo_animale b " + 
				"WHERE b.id_tipo_animale = a.id_tipo_animale and a.id_merce = ?";
		try{
			evento =  jdbcTemplate.queryForObject(queryStr, new MerceRowMapper(), new Object[] { idMerce });
			return evento;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return evento;
		}
	}
	
	/**
	 * @param idMerce
	 * @return Merce
	 */
	@Transactional(readOnly = true)
	public Merce getByIDEvento(String idMerce, String idEvento) {
		Merce evento = null;
		String queryStr = "SELECT a.id_merce,a.descrizione,a.peso,a.tipo_merce,a.id_tipo_animale,b.descr_tipo_animale " + 
				"FROM rc_r_evento_merce c, rc_x_merceologia a, an_x_tipo_animale b  " + 
				"WHERE b.id_tipo_animale = a.id_tipo_animale and a.id_merce=c.id_merce " + 
				"and  a.id_merce = ? and c.id_evento = ?";
		try{
			evento =  jdbcTemplate.queryForObject(queryStr, new MerceRowMapper(), new Object[] { idMerce, idEvento });
			return evento;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return evento;
		}
	}
	
	/**
	 * @param strToSearch
	 * @return List<Merce>
	 */
	@Transactional(readOnly = true)
	public List<Merce> getByStrToSearch(String strToSearch) {
		List<Merce> merci = null;
		strToSearch = "%" + strToSearch + "%";
		String queryStr = "SELECT a.id_merce,concat(b.descr_tipo_animale,' ',a.tipo_merce, ' ',a.descrizione) descrizione,a.peso,a.tipo_merce,a.id_tipo_animale,b.descr_tipo_animale " + 
				"FROM rc_x_merceologia a, an_x_tipo_animale b " + 
				"WHERE b.id_tipo_animale = a.id_tipo_animale " + 
				"and concat(upper(a.descrizione),  upper(a.tipo_merce), upper(b.descr_tipo_animale)) like ?";
		try{
			merci =  jdbcTemplate.query(queryStr, new MerceRowMapper(), new Object[] { strToSearch.toUpperCase() });
			return merci;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return merci;
		}
	}
	
	/**
	 * @param 
	 * @return List<Merce>
	 */
	@Transactional(readOnly = true)
	public List<Merce> getMerci() {
		List<Merce> merci = null;
		String queryStr = "SELECT a.id_merce,a.descrizione,a.peso,a.tipo_merce,a.id_tipo_animale,b.descr_tipo_animale " + 
								"FROM rc_x_merceologia a, an_x_tipo_animale b  " + 
								"WHERE b.id_tipo_animale = a.id_tipo_animale order by a.descrizione";
		try{
			return jdbcTemplate.query(queryStr, new MerceRowMapper());
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return merci;
		}
	}
	
	
	/**
	 * @param merce
	 * @return idMerce
	 */
	@Transactional()
	public String saveOrUpdate(Merce merce) throws SQLIntegrityConstraintViolationException {
		String idMerce = "";

		if (merce.getId_merce() == null && Def.NUM_NOVANTANOVE.equals(merce.getId_tipo_animale())) {
			merce.setId_tipo_animale(Def.NUM_UNO);
			idMerce = save(merce);
			merce.setId_tipo_animale(Def.NUM_DUE);
			idMerce = save(merce);
		}else if (merce.getId_merce() != null && Def.NUM_NOVANTANOVE.equals(merce.getId_tipo_animale())) {
			String id = merce.getId_merce();
			String queryStr = "SELECT id_merce, id_tipo_animale FROM rc_x_merceologia " + 
					"	where descrizione = (SELECT descrizione FROM rc_x_merceologia where id_merce=" + id + ") " + 
					"	and peso = (SELECT peso FROM rc_x_merceologia where id_merce=" + id + ") " + 
					"	and tipo_merce = (SELECT tipo_merce FROM rc_x_merceologia where id_merce=" + id + ")";
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr);

			for (Map<String,Object> m : results){
				merce.setId_merce(m.get("id_merce").toString());
				merce.setId_tipo_animale(m.get("id_tipo_animale").toString());
				idMerce = update(merce);
			} 
		}else if (merce.getId_merce() == null) {
			idMerce = save(merce);
			logger.info("Inserito turno: " + idMerce + " " + merce.getId_merce() );
		} else {
			idMerce = update(merce);
		}
		
		return idMerce;
	}
	
	/**
	 * @param merce
	 * @return idMerce
	 */
	@Transactional()
	private String save(Merce merce) {

		String query = "INSERT INTO rc_x_merceologia (descrizione, peso, tipo_merce, id_tipo_animale) VALUES(upper(:descrizione), :peso, :tipo_merce, :id_tipo_animale)";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		SqlParameterSource parameters = new BeanPropertySqlParameterSource(merce);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_merce" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param merce
	 * @return idMerce
	 */
	@Transactional()
	private String update(Merce merce) {

		String query = "UPDATE rc_x_merceologia " + 
				"SET descrizione = upper(:descrizione), peso = :peso, tipo_merce = :tipo_merce, id_tipo_animale = :id_tipo_animale " + 
				"WHERE id_merce = :id_merce";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(merce);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return merce.getId_merce();
	}
	
	
	/**
	 * @param idMerce
	 * @return String
	 */
	@Transactional()
	public String deleteByID(String idMerce) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM rc_x_merceologia WHERE id_merce = ?";
		jdbcTemplate.update(query, new Object[] { idMerce });
		return Def.STR_OK;
	}
	
	private static class MerceRowMapper extends BaseRowMapper<Merce> {
		public MerceRowMapper() {}		
		@Override
		public Merce mapRowImpl(ResultSet rs, int i) throws SQLException {
			Merce o = new Merce();
			o.setId_merce(rs.getString("id_merce"));
			o.setDescrizione(rs.getString("descrizione"));
			o.setPeso(rs.getString("peso"));
			o.setTipo_merce(rs.getString("tipo_merce"));
			o.setId_tipo_animale(rs.getString("id_tipo_animale"));
			o.setTipo_animale(rs.getString("descr_tipo_animale"));
			return o;
		}
	}
}
