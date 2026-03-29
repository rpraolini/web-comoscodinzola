package it.asso.core.dao.animali.animale;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.animali.animale.Colore;
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
public class ColoreDAO {

	private static Logger logger = LoggerFactory.getLogger(ColoreDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public ColoreDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	

	/**
	 * @param 
	 * @return List<Colore>
	 */
	@Transactional()
	public List<Colore> getColori() {
		
		
		String queryStr = "SELECT id_colore, descr_colore "
				+ " FROM  an_x_colore "
				+ " ORDER BY descr_colore";

		try{
			return jdbcTemplate.query(queryStr, new ColoriRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Colore>
	 */
	@Transactional()
	private int checkColore(String colore) {
		
		
		String queryStr = "SELECT count(descr_colore) FROM an_x_colore WHERE binary descr_colore = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { colore });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
		
	/**
	 * @param colore
	 * @return
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	
	@Transactional()
	public String saveOrUpdate(Colore colore) throws SQLIntegrityConstraintViolationException {
		
		if(checkColore(colore.getDescr_colore()) > 0) {
			throw new SQLIntegrityConstraintViolationException(); 
		}
		
		if (colore.getId_colore() == null) {
			return save(colore);
		}else {
			return update(colore);
		}
	}

	/**
	 * @param Colore
	 * @return
	 */
	
	@Transactional()
	public String save(Colore Colore) {

		final String query = "INSERT INTO an_x_colore  (descr_colore)  VALUES  (:descr_colore)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(Colore);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_colore" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param Colore
	 * @return
	 */
	
	@Transactional()
	public String update(Colore Colore) {

		final String query = "UPDATE an_x_colore  SET descr_colore = :descr_colore  WHERE  id_colore = :id_colore ";


		SqlParameterSource parameters = new BeanPropertySqlParameterSource(Colore);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return Def.STR_OK;
	}

		
	/**
	 * @param idColore
	 * @return
	 */
	
	@Transactional()
	public String deleteByID(String idColore) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM an_x_colore WHERE id_colore = ?";
		jdbcTemplate.update(query, new Object[] { idColore });
		return Def.STR_OK;
	}
	

	
	private static class ColoriRowMapper extends BaseRowMapper<Colore> {
		public ColoriRowMapper() {}		
		@Override
		public Colore mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Colore o = new Colore();
			o.setId_colore(rs.getString("id_colore"));
			o.setDescr_colore(rs.getString("descr_colore"));
			return o;
		}
	}


}
