package it.asso.core.dao.documenti;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.documenti.TipoDocumento;
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
public class TipoDocumentoDAO {

	private static Logger logger = LoggerFactory.getLogger(TipoDocumentoDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public TipoDocumentoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param id_tipo_documento
	 * @return id_tipo_documento
	 */
	@Transactional()
	public TipoDocumento getTipoDocumentoByID(String id_tipo_documento) {
		
		
		String queryStr = "SELECT id_tipo_documento, documento, ambito, prefix_filename "
				+ " FROM  an_x_tipo_documento "
				+ " WHERE id_tipo_documento = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, new TipoDocumentoRowMapper(), new Object[] { id_tipo_documento });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	

	/**
	 * @param ambito
	 * @return List<TipoDocumento>
	 */
	@Transactional()
	public List<TipoDocumento> getTipoDocumento(String ambito) {
		
		
		String queryStr = "SELECT id_tipo_documento, documento, ambito, prefix_filename "
				+ " FROM  an_x_tipo_documento WHERE ambito = ?"
				+ " ORDER BY documento";

		try{
			return jdbcTemplate.query(queryStr,new TipoDocumentoRowMapper(), new Object[] { ambito });

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
	private int checkTipoDocumento(String documento) {
		
		
		String queryStr = "SELECT count(documento) FROM an_x_tipo_documento WHERE binary documento = ?";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { documento });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
		
	/**
	 * @param tipoDocumento
	 * @return
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	
	@Transactional()
	public String saveOrUpdate(TipoDocumento tipoDocumento) throws SQLIntegrityConstraintViolationException {
		
		if(checkTipoDocumento(tipoDocumento.getDocumento()) > 0) {
			throw new SQLIntegrityConstraintViolationException(); 
		}
		
		if (tipoDocumento.getId_tipo_documento() == null) {
			return save(tipoDocumento);
		}else {
			return update(tipoDocumento);
		}
	}

	/**
	 * @param tipoDocumento
	 * @return
	 */
	
	@Transactional()
	public String save(TipoDocumento tipoDocumento) {

		final String query = "INSERT INTO an_x_tipo_documento  (documento, ambito)  VALUES  (:documento, :ambito)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tipoDocumento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_tipo_documento" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param tipoDocumento
	 * @return
	 */
	
	@Transactional()
	public String update(TipoDocumento tipoDocumento) {

		final String query = "UPDATE an_x_tipo_documento  SET documento = :documento, ambito = :ambito  WHERE  id_tipo_documento = :id_tipo_documento ";


		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tipoDocumento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return Def.STR_OK;
	}

		
	/**
	 * @param idTipoDocumento
	 * @return
	 */
	
	@Transactional()
	public String deleteByID(String idTipoDocumento) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM an_x_tipo_documento WHERE id_tipo_documento = ?";
		jdbcTemplate.update(query, new Object[] { idTipoDocumento });
		return Def.STR_OK;
	}
	

	private static class TipoDocumentoRowMapper extends BaseRowMapper<TipoDocumento> {
		public TipoDocumentoRowMapper() {}		
		@Override
		public TipoDocumento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoDocumento o = new TipoDocumento();
			o.setId_tipo_documento(rs.getString("id_tipo_documento"));
			o.setDocumento(rs.getString("documento"));
			o.setAmbito(rs.getString("ambito"));
			o.setPrefix_filename(rs.getString("prefix_filename"));
			return o;
		}
	
	}
	


}
