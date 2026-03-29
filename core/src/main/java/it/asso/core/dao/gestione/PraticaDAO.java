package it.asso.core.dao.gestione;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.gestione.Pratica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

@Repository
public class PraticaDAO{

    private static final Logger logger = LoggerFactory.getLogger(PraticaDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public PraticaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param idPratica
	 * @return Pratica
	 */
	@Transactional(readOnly = true)
	public Pratica getByID(String idPratica) {
		Pratica pratica = null;
		String queryStr = "SELECT p.id_pratica, p.id_animale, p.id_stato, p.account, p.dt_aggiornamento, s.descr_stato, s.macro_stato FROM as_pratiche p, as_x_stati s WHERE p.id_stato=s.id_stato_p and p.id_pratica = ?";
		try{
			pratica =  jdbcTemplate.queryForObject(queryStr, new PraticaRowMapper(), new Object[] { idPratica });
			return pratica;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return pratica;
		}
	}
	
	/**
	 * @param idAnimale
	 * @return List<Pratica>
	 */
	@Transactional(readOnly = true)
	public List<Pratica> getPraticheByIDAnimale(String idAnimale) {
		List<Pratica> pratiche = null;
		String queryStr = "SELECT p.id_pratica, p.id_animale, p.id_stato, p.account, p.dt_aggiornamento, s.descr_stato, s.macro_stato FROM as_pratiche p, as_x_stati s WHERE p.id_stato=s.id_stato_p and id_animale = ? order by p.dt_aggiornamento desc";
		try{
			pratiche =  jdbcTemplate.query(queryStr, new PraticaRowMapper(), new Object[] { idAnimale });
			return pratiche;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return pratiche;
		}
	}
	
	
	/**
	 * @param pratica
	 * @return idPratica
	 */
	
	@Transactional()
	public String saveOrUpdate(Pratica pratica) {
		String idPratica = "";

		if (pratica.getId_pratica() == null) {
			idPratica = save(pratica);
		} else {
			idPratica = update(pratica);
		}

		return idPratica;
	}
	
	
	/**
	 * @param pratica
	 * @return idPratica
	 */
	@Transactional()
	private String save(Pratica pratica) {
		pratica.setId_pratica(getCodice());
		final String query = "INSERT INTO as_pratiche(id_pratica, id_animale, id_stato, account) " + 
										"VALUES (:id_pratica, :id_animale, :id_stato, :account)";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(pratica);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		return pratica.getId_pratica();
	}

	/**
	 * @param pratica
	 * @return idPratica
	 */
	@Transactional()
	private String update(Pratica pratica) {

		final String query = "UPDATE as_pratiche SET id_animale = :id_animale, id_stato = :id_stato, account = :account WHERE id_pratica = :id_pratica";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(pratica);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return pratica.getId_pratica();
	}
	
	/**
	 * @param idPratica
	 * @return String
	 */
	@Transactional()
	public String close(String idPratica) {
		final String query = "UPDATE as_pratiche SET id_stato = 99 WHERE id_pratica = ?";
		jdbcTemplate.update(query, new Object[] { idPratica });
		return Def.STR_OK;
	}
	
	/**
	 * @param idPratica
	 * @return
	 */
	@Transactional()
	public void deleteByID(String idPratica) {
		String query = "DELETE FROM as_r_attivita WHERE id_pratica = ?";
		jdbcTemplate.update(query, new Object[] { idPratica });
		query = "DELETE FROM as_pratiche WHERE id_pratica = ?";
		jdbcTemplate.update(query, new Object[] { idPratica });
	}
	
	
	/**
	 * @param idAnimale
	 * @return idPratica
	 */
	@Transactional(readOnly = true)
	public String getExistPraticaAttivaByIDAnimale(String idAnimale) {
		String queryStr = "SELECT id_pratica FROM as_pratiche WHERE id_animale = ? and id_stato != 99";
		try{
			List<String> listaPratiche =  jdbcTemplate.queryForList(queryStr, String.class, new Object[] { idAnimale });
			if(!listaPratiche.isEmpty()) {
				return listaPratiche.get(0);
			}else {
				return null;
			}
			
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	
	private String getCodice() {
		String queryStr = "SELECT coalesce(max(CONVERT(SUBSTRING(id_pratica, 7, 5),UNSIGNED INTEGER)),0) + 1 FROM as_pratiche";
		try{
			int count = jdbcTemplate.queryForObject(queryStr, Integer.class);
			
			int anno = Year.now().getValue();
			String codice = jdbcTemplate.queryForObject("SELECT concat('" + anno + ".', LPAD(" + count + ",5,'0') )", String.class);
			return  codice;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return Def.STR_KO;
		}
	}
	
	private static class PraticaRowMapper extends BaseRowMapper<Pratica> {
		public PraticaRowMapper() {
		}		
		@Override
		public Pratica mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Pratica o = new Pratica();
			o.setId_pratica(rs.getString("id_pratica"));
			o.setId_animale(rs.getString("id_animale"));
			o.setId_stato(rs.getString("id_stato"));
			o.setAccount(rs.getString("account"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setStato(rs.getString("descr_stato"));
			o.setMacro_stato(rs.getString("macro_stato"));
			return o;
		}
	}

}
