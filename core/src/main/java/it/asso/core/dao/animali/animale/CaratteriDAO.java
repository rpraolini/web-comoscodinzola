package it.asso.core.dao.animali.animale;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.animali.animale.Carattere;
import it.asso.core.model.animali.animale.Caratteri;
import it.asso.core.model.animali.animale.TipoCarattere;
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
public class CaratteriDAO {

	private static Logger logger = LoggerFactory.getLogger(CaratteriDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public CaratteriDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	

	/**
	 * @param 
	 * @return List<Caratteri>
	 */
	@Transactional()
	public List<Caratteri> getCaratteriByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT " + 
				"a.id_caratteri,a.id_animale,a.id_carattere,a.note,b.carattere,b.id_tipo_carattere,c.contesto,c.icona " + 
				"FROM " + 
				"  an_r_caratteri a LEFT JOIN an_x_caratteri b ON a.id_carattere = b.id_carattere " + 
				"  LEFT JOIN an_x_tipo_carattere c ON b.id_tipo_carattere = c.id_tipo_carattere " + 
				"WHERE " + 
				"  a.id_animale = ?";

		try{
			//checkCaratteriByIdAnimale(idAnimale);
			List<Caratteri> c = jdbcTemplate.query(queryStr, new CaratteriRowMapper(), new Object[] { idAnimale });
			return c;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Caratteri>
	 */
	@Transactional()
	public List<Carattere> getCaratteriByTipo(String idTipoCarattere) {
		
		
		String queryStr = "SELECT a.id_carattere, a.carattere, a.id_tipo_carattere, b.icona, b.contesto  " + 
				" FROM an_x_caratteri a, an_x_tipo_carattere b " + 
				" WHERE a.id_tipo_carattere = b.id_tipo_carattere and a.id_tipo_carattere = ? " + 
				" ORDER BY a.carattere;";

		try{
			return jdbcTemplate.query(queryStr, new CarattereRowMapper(), new Object[] { idTipoCarattere });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param carattere
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(Caratteri carattere) {

		if (carattere.getId_caratteri() == null) {
			return save(carattere);
		}else {
			return update(carattere);
		}
	}
	
	/**
	 * @param carattere
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdateTipoCarattere(Carattere carattere) {
		
		if (carattere.getId_carattere() == null) {
			return saveTipoCarattere(carattere);
		}else {
			return updateTipoCarattere(carattere);
		}
	}

	/**
	 * @param carattere
	 * @return
	 */
	
	@Transactional()
	public String save(Caratteri carattere) {

		final String query = "INSERT INTO an_r_caratteri " + 
										"(id_animale, " +
										"id_carattere," + 
										"note)" +
										" VALUES " + 
										"(:id_animale," +
										":id_carattere, " +
										":note " +
										")";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(carattere);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_caratteri" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param carattere
	 * @return
	 */
	
	@Transactional()
	public String update(Caratteri carattere) {

		final String query = "UPDATE an_r_caratteri SET id_carattere = :id_carattere, note = :note WHERE id_caratteri = :id_caratteri";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(carattere);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return Def.STR_OK;
	}
	
	/**
	 * @param carattere
	 * @return
	 */
	
	@Transactional()
	public String saveTipoCarattere(Carattere carattere) {

		final String query = "INSERT INTO an_x_caratteri (carattere, id_tipo_carattere) VALUES (:carattere, :id_tipo_carattere)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(carattere);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_carattere" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param carattere
	 * @return
	 */
	
	@Transactional()
	public String updateTipoCarattere(Carattere carattere) {

		final String query = "UPDATE an_x_caratteri SET carattere = :carattere, id_tipo_carattere = :id_tipo_carattere WHERE id_carattere = :id_carattere";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(carattere);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return Def.STR_OK;
	}

		
	/**
	 * @param idCaratteri
	 * @return
	 */
	
	@Transactional()
	public String deleteByID(String idCaratteri) {
		final String query = "DELETE FROM an_r_caratteri WHERE id_caratteri = ?";
		jdbcTemplate.update(query, new Object[] { idCaratteri });
		return Def.STR_OK;
	}
	

	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String deleteTipoCarattereByID(String id) {
		final String query = "DELETE FROM an_x_caratteri WHERE id_carattere = ?";
		jdbcTemplate.update(query, new Object[] { id });
		return Def.STR_OK;

	}
	
	
	private static  class CaratteriRowMapper extends BaseRowMapper<Caratteri> {
		public CaratteriRowMapper() {
		}		
		@Override
		public Caratteri mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Caratteri o = new Caratteri();
			o.setId_animale(rs.getString("id_animale"));
			o.setCarattere(rs.getString("carattere"));
			o.setId_carattere(rs.getString("id_carattere"));
			o.setId_caratteri(rs.getString("id_caratteri"));
			o.setNote(rs.getString("note"));
			o.setIcona(rs.getString("icona"));
			o.setContesto(rs.getString("contesto"));
			o.setId_tipo_carattere(rs.getString("id_tipo_carattere"));
			return o;
		}
	}
		
	
	/**
	 * @param 
	 * @return List<Carattere>
	 */
	@Transactional(readOnly = true)
	public List<Carattere> getTipoCaratteri() {
		
		String queryStr = "SELECT a.id_carattere,  a.carattere, b.icona, b.contesto, a.id_tipo_carattere " +
				" FROM an_x_caratteri a, an_x_tipo_carattere b WHERE a.id_tipo_carattere=b.id_tipo_carattere" + 
				" ORDER BY a.carattere";

		try{
			return jdbcTemplate.query(queryStr, new CarattereRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private static class CarattereRowMapper extends BaseRowMapper<Carattere> {
		public CarattereRowMapper() {
		}		
		@Override
		public Carattere mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Carattere o = new Carattere();
			o.setCarattere(rs.getString("carattere"));
			o.setId_carattere(rs.getString("id_carattere"));
			o.setIcona(rs.getString("icona"));
			o.setContesto(rs.getString("contesto"));
			o.setId_tipo_carattere(rs.getString("id_tipo_carattere"));
			return o;
		}
	}
	
	/**
	 * @param 
	 * @return List<TipoCarattere>
	 */
	@Transactional(readOnly = true)
	public List<TipoCarattere> getTipoCarattere() {
		
		String queryStr = "SELECT id_tipo_carattere, icona, contesto " +
				" FROM an_x_tipo_carattere  " + 
				" ORDER BY contesto";

		try{
			return jdbcTemplate.query(queryStr, new TipoCarattereRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	private static class TipoCarattereRowMapper extends BaseRowMapper<TipoCarattere> {
		public TipoCarattereRowMapper() {
		}		
		@Override
		public TipoCarattere mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoCarattere o = new TipoCarattere();
			o.setId_tipo_carattere(rs.getString("id_tipo_carattere"));
			o.setIcona(rs.getString("icona"));
			o.setContesto(rs.getString("contesto"));
			return o;
		}
	}
	

}
