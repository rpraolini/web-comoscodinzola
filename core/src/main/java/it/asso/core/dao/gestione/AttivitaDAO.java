package it.asso.core.dao.gestione;

import it.asso.core.dao.documenti.VideoDAO;
import it.asso.core.model.gestione.Attivita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public class AttivitaDAO{
    private static Logger logger = LoggerFactory.getLogger(AttivitaDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public AttivitaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	
	/**
	 * @param attivita
	 * @return idAttivita
	 */
	@Transactional()
	public String saveOrUpdate(Attivita attivita) {
		return save(attivita);
	}
	
	
	/**
	 * @param attivita
	 * @return idAttivita
	 */
	@Transactional()
	private String save(Attivita attivita) {
		final String query = "INSERT INTO as_r_attivita (id_attivita_p, id_pratica, note_attivita, id_utente, account, id_stato_padre,  id_stato_precedente) " + 
				"VALUES (:id_attivita_p, :id_pratica, note_attivita, :id_utente, :account, :id_stato_padre, :id_stato_precedente)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(attivita);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_r_attivita_p" });

		return String.valueOf(keyHolder.getKey());
	}

	
}
