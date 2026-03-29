package it.asso.core.dao.animali.animale;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.animali.animale.Razza;
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
public class RazzaDAO {

	private static Logger logger = LoggerFactory.getLogger(RazzaDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public RazzaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	

	/**
	 * @param 
	 * @return List<Razza>
	 */
	@Transactional()
	public List<Razza> getRazze() {
		
		
		String queryStr = "SELECT id_razza, razza "
				+ " FROM  an_x_razze "
				+ " ORDER BY razza";

		try{
			return jdbcTemplate.query(queryStr, new RazzaRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return int
	 */
	@Transactional()
	private int checkRazza(String razza) {
		
		
		String queryStr = "SELECT count(razza) FROM an_x_razze WHERE binary razza = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { razza });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
		
	/**
	 * @param razza
	 * @return
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	
	@Transactional()
	public String saveOrUpdate(Razza razza) throws SQLIntegrityConstraintViolationException {
		
		if(checkRazza(razza.getRazza()) > 0) {
			throw new SQLIntegrityConstraintViolationException(); 
		}
		
		if (razza.getId_razza() == null) {
			return save(razza);
		}else {
			return update(razza);
		}
	}

	/**
	 * @param razza
	 * @return
	 */
	
	@Transactional()
	public String save(Razza razza) {

		final String query = "INSERT INTO an_x_razze  (razza)  VALUES  (:razza)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(razza);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_razza" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param razza
	 * @return
	 */
	
	@Transactional()
	public String update(Razza razza) {

		final String query = "UPDATE an_x_razze  SET razza = :razza  WHERE  id_razza = :id_razza ";


		SqlParameterSource parameters = new BeanPropertySqlParameterSource(razza);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return Def.STR_OK;
	}

		
	/**
	 * @param idRazza
	 * @return
	 */
	
	@Transactional()
	public String deleteByID(String idRazza) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM an_x_razze WHERE id_razza = ?";
		jdbcTemplate.update(query, new Object[] { idRazza });
		return Def.STR_OK;
	}
	

	
	private static class RazzaRowMapper extends BaseRowMapper<Razza> {
		public RazzaRowMapper() {}		
		@Override
		public Razza mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Razza o = new Razza();
			o.setId_razza(rs.getString("id_razza"));
			o.setRazza(rs.getString("razza"));
			return o;
		}
	}


}
