package it.asso.core.dao.questionario;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.questionario.Questionario;
import it.asso.core.model.questionario.QuestionarioSezioni;
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
import java.util.List;

@Repository
public class QuestionarioDAO {

	private static Logger logger = LoggerFactory.getLogger(QuestionarioDAO.class);


    private final JdbcTemplate jdbcTemplate;

    public QuestionarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	

	/**
	 * @return List<Questionario>
	 */
	@Transactional()
	public List<Questionario> getQuestionarioByIdRichiesta(String idRichiesta) {
		
		
		String queryStr = "SELECT a.id_questionario, " + 
				"    a.id_richiesta, " + 
				"    a.id_domanda, " + 
				"    a.id_sezione, " + 
				"    a.ordine, " + 
				"    a.risposta, " + 
				"    b.domanda, " + 
				"    c.sezione " + 
				"FROM an_r_questionari a, an_x_quest_domande b, an_x_quest_sezioni c " + 
				"	WHERE a.id_domanda=b.id_domanda and a.id_sezione=c.id_sezione and a.id_richiesta = ? " + 
				"    ORDER BY a.id_sezione, a.ordine";

		try{
			return jdbcTemplate.query(queryStr, new QuestionarioRowMapper(), new Object[] { idRichiesta });

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
	public List<Questionario> getQuestionarioByIdRichiestaAndSezione(String idRichiesta, String idSezione) {
		
		
		String queryStr = "SELECT a.id_questionario, " + 
				"    a.id_richiesta, " + 
				"    a.id_domanda, " + 
				"    a.id_sezione, " + 
				"    a.ordine, " + 
				"    a.risposta, " + 
				"    b.domanda, " + 
				"    c.sezione " + 
				"FROM an_r_questionari a, an_x_quest_domande b, an_x_quest_sezioni c " + 
				"	WHERE a.id_domanda=b.id_domanda and a.id_sezione=c.id_sezione and a.id_richiesta = ? and a.id_sezione = ? " + 
				"    ORDER BY a.id_sezione, a.ordine";

		try{
			return jdbcTemplate.query(queryStr, new QuestionarioRowMapper(), new Object[] { idRichiesta, idSezione });

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
	public List<QuestionarioSezioni> getSezioniByIdRichiesta(String idRichiesta) {
		
		
		String queryStr = "SELECT  distinct a.id_sezione, c.sezione " + 
				"FROM an_r_questionari a, an_x_quest_domande b, an_x_quest_sezioni c " + 
				"	WHERE a.id_domanda=b.id_domanda and a.id_sezione=c.id_sezione and a.id_richiesta = ? " + 
				"    ORDER BY a.id_sezione";

		try{
			return jdbcTemplate.query(queryStr, new QuestionarioSezioniRowMapper(), new Object[] { idRichiesta });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param questionario
	 * @return
	 */
	
	@Transactional()
	public String updateQuestionario(Questionario questionario) {

		final String query = "UPDATE an_r_questionari" + 
				"	SET risposta = :risposta WHERE id_questionario = :id_questionario";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(questionario);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return questionario.getId_questionario();
	}
	
	/**
	 * @param idRichiesta
	 * @return
	 */
	
	/**
	 * Inizializza il questionario per un nuovo iter di preaffido,
	 * copiando tutte le domande dal master an_x_quest_domande.
	 */
	@Transactional()
	public void initQuestionario(String idRichiesta) {
		final String query =
			"INSERT INTO an_r_questionari (id_richiesta, id_domanda, id_sezione) " +
			"SELECT ?, id_domanda, id_sezione FROM an_x_quest_domande ORDER BY id_sezione, id_domanda";
		jdbcTemplate.update(query, idRichiesta);
	}

	@Transactional()
	public String inviaQuestionario(String  idRichiesta) {

		final String query = "UPDATE an_r_iter SET quest_f = 'R' WHERE id_iter = ?";

		jdbcTemplate.update(query, idRichiesta);

		return Def.STR_OK;
	}
	
	
	
	
	private static class QuestionarioRowMapper extends BaseRowMapper<Questionario> {
		public QuestionarioRowMapper() {
		}		
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * it.asso.webapps.repository.dao.BaseRowMapper#mapRowImpl(java.sql.
		 * ResultSet, int)
		 */
		@Override
		public Questionario mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Questionario o = new Questionario();
			o.setDomanda(rs.getString("domanda"));
			o.setId_domanda(rs.getString("id_domanda"));
			o.setId_questionario(rs.getString("id_questionario"));
			o.setId_sezione(rs.getString("id_sezione"));
			o.setOrdine(rs.getString("ordine"));
			o.setRisposta(rs.getString("risposta"));
			o.setSezione(rs.getString("sezione"));
			return o;
		}
	}
	
	private static class QuestionarioSezioniRowMapper extends BaseRowMapper<QuestionarioSezioni> {
		public QuestionarioSezioniRowMapper() {
		}		
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * it.asso.webapps.repository.dao.BaseRowMapper#mapRowImpl(java.sql.
		 * ResultSet, int)
		 */
		@Override
		public QuestionarioSezioni mapRowImpl(ResultSet rs, int i) throws SQLException {
			QuestionarioSezioni o = new QuestionarioSezioni();
			o.setId_sezione(rs.getString("id_sezione"));
			o.setSezione(rs.getString("sezione"));
			return o;
		}
	}
	

}
