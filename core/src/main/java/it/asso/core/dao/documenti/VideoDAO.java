package it.asso.core.dao.documenti;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.documenti.Video;
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
public class VideoDAO {

	private static Logger logger = LoggerFactory.getLogger(VideoDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public VideoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	
	/**
	 * @param idAnimale
	 * @return String
	 */
	@Transactional(readOnly = true)
	public int getCountByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT count(id_animale) " + 
				" FROM an_video " + 
				" WHERE id_animale = ? ";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param 
	 * @return List<Video>
	 */
	@Transactional(readOnly = true)
	public List<Video> getVideoByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT id_animale, " + 
				"    id_video, " + 
				"    nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
				"    account, " + 
				"    url, pubblico " +
				" FROM an_video " + 
				" WHERE id_animale = ? ";

		try{
			return jdbcTemplate.query(queryStr, new VideoRowMapper(), new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Video>
	 */
	@Transactional(readOnly = true)
	public List<Video> getVideoPubbliciByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT id_animale, " + 
				"    id_video, " + 
				"    nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
				"    account, " + 
				"    url, pubblico " +
				" FROM an_video " + 
				" WHERE id_animale = ? and pubblico = 1";

		try{
			return jdbcTemplate.query(queryStr, new VideoRowMapper(), new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idVideo
	 * @return Video
	 */
	@Transactional(readOnly = true)
	public Video getVideoById(String idVideo) {
		
		
		String queryStr = "SELECT id_animale, " + 
				"    id_video, " + 
				"    nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
				"    account, " + 
				"    url, pubblico " + 
				" FROM an_video " + 
				" WHERE id_video = ? ";

		try{
			return jdbcTemplate.queryForObject(queryStr, new VideoRowMapper(), new Object[] { idVideo });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	/**
	 * @param video
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(Video video) {
		String idVideo = "";
		
		
		if (video.getId_video() == null) {
			idVideo = save(video);
		} else {
			idVideo = update(video);
		}
		return idVideo;
	}

	/**
	 * @param video
	 * @return
	 */
	
	@Transactional()
	public String save(Video video) {

		final String query = "INSERT INTO an_video " + 
										"(id_animale, " +
										"account," + 
										"url, pubblico)" +
										" VALUES " + 
										"(:id_animale," + 
										":account," + 
										":url, :pubblico)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(video);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_video" });
		
		return String.valueOf(keyHolder.getKey());
	}

	/**
	 * @param video
	 * @return
	 */
	
	@Transactional()
	public String update(Video video) {

		final String query = "UPDATE an_video " + 
				" SET " + 
				"url = :url," + 
				"id_animale = :id_animale," +
				"account = :account , pubblico = :pubblico" +
				" WHERE id_video = :id_video";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(video);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		
		return video.getId_video();
	}
	

	/**
	 * @param video
	 * @return
	 */
	
	@Transactional()
	public String delete(Video video) {

		final String query = "DELETE FROM an_video WHERE id_video = :id_video";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(video);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return video.getId_video();
	}
	
	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String delete(String id) {

		Video video = new Video();
		video.setId_video(id);
		
		delete(video);
		
		return  Def.STR_OK;
	}
	
	private static class VideoRowMapper extends BaseRowMapper<Video> {
		public VideoRowMapper() {
		}		
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * it.asso.webapps.repository.dao.BaseRowMapper#mapRowImpl(java.sql.
		 * ResultSet, int)
		 */
		@Override
		public Video mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Video o = new Video();
			o.setId_animale(rs.getString("id_animale"));
			o.setAccount(rs.getString("account"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setId_video(rs.getString("id_video"));
			o.setUrl(rs.getString("url"));
			o.setPubblico(rs.getString("pubblico"));
			return o;
		}
	}
	
	
}
