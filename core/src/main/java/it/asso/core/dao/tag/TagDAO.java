package it.asso.core.dao.tag;

import it.asso.core.common.Def;
import it.asso.core.model.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
public class TagDAO {

	private static final Logger logger = LoggerFactory.getLogger(TagDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public TagDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @return List<Tag>
	 */
	@Transactional(readOnly = true)
	public List<Tag> getTags() {
		
		String queryStr = "SELECT id_tag, tag FROM an_x_tag order by tag";

		try{
			return  jdbcTemplate.query(queryStr, new TagRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @return Tag
	 */
	@Transactional(readOnly = true)
	public Tag getTagByID(String idTag) {
		
		String queryStr = "SELECT id_tag, tag FROM an_x_tag WHERE id_tag = ?";

		try{
			return  jdbcTemplate.queryForObject(queryStr,new TagRowMapper(), new Object[] {idTag});

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Tag>
	 */
	@Transactional(readOnly = true)
	public List<Tag> getTagsByIDAnimale(String idAnimale) {
		
		String queryStr = "SELECT b.id_tag,b.tag FROM an_r_animale_tags a, an_x_tag b where a.id_tag=b.id_tag and a.id_animale = ?;";

		try{
			return  jdbcTemplate.query(queryStr,new TagRowMapper(), new Object[] {idAnimale} );

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	/**
	 * @param tag
	 * @return Tag
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	
	@Transactional()
	public Tag saveOrUpdate(Tag tag) throws SQLIntegrityConstraintViolationException {
			
		if (tag.getId_tag() == null) {
			return save(tag);
		}else {
			return update(tag);
		}
	}

	/**
	 * @param tag
	 * @return
	 */
	
	@Transactional()
	private Tag save(Tag tag) {

		final String query = "INSERT INTO an_x_tag  (tag)  VALUES  (upper(:tag))";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tag);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_tag" });

		return getTagByID(String.valueOf(keyHolder.getKey()));
	}
	
	/**
	 * @param tag
	 * @return
	 */
	
	@Transactional()
	private Tag update(Tag tag) {

		final String query = "UPDATE an_x_tag  SET tag = upper(:tag)  WHERE  id_tag = :id_tag ";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(tag);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return getTagByID(tag.getId_tag());
	}
	
	/**
	 * @param idAnimale
	 * @return
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	
	@Transactional()
	public String saveOrUpdateForAnimale(String idAnimale, String idTag) throws SQLIntegrityConstraintViolationException {
			
		if (checkTagForAnimale(idTag, idAnimale) == 0) {
			return insertTagForAnimale(idTag, idAnimale);
		}
		
		return "";
	}
	
	/**
	 * @param idAnimale
	 * @return
	 */
	
	@Transactional()
	private int checkTagForAnimale(String idTag, String idAnimale) {

		final String query = "select count(*) FROM an_r_animale_tags WHERE id_tag = ? and id_animale = ?";

		return jdbcTemplate.queryForObject(query, Integer.class, new Object[] { idTag, idAnimale });


	}
	
	/**
	 * @param idTag
	 * @return
	 */
	
	@Transactional()
	private String insertTagForAnimale(String idTag, String idAnimale) {

		final String query = "INSERT INTO an_r_animale_tags (id_tag, id_animale) values (?, ?)";

		jdbcTemplate.update(query, new Object[] { idTag, idAnimale });
		
		return Def.STR_OK;
	}
	
	
	/**
	 * @param idTag
	 * @return
	 */
	
	@Transactional()
	public String deleteTagForAnimale(String idAnimale, String idTag) {

		final String query = "DELETE FROM an_r_animale_tags  WHERE  id_tag = ? and id_animale = ?";

		jdbcTemplate.update(query, new Object[] { idTag, idAnimale });

		return Def.STR_OK;
	}

		
	/**
	 * @param idTag
	 * @return
	 */
	
	@Transactional()
	public String deleteByID(String idTag) throws SQLIntegrityConstraintViolationException {
		final String query = "DELETE FROM an_x_tag WHERE id_tag = ?";
		jdbcTemplate.update(query, new Object[] { idTag });
		return Def.STR_OK;
	}
	
	
	
	private static final class TagRowMapper implements RowMapper<Tag> {
		public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
			Tag o = new Tag();
			o.setId_tag(rs.getString("id_tag"));
			o.setTag(rs.getString("tag"));
			return o;
		}
	}
	
}
