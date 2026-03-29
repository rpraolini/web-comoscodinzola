package it.asso.core.dao.log;

import it.asso.core.common.Def;
import it.asso.core.model.log.LogMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LogMailDAO {

	private static Logger logger = LoggerFactory.getLogger(LogMailDAO.class);


    private final JdbcTemplate jdbcTemplate;

    public LogMailDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	
	/**
	 * @param logMail
	 * @return String
	 */
	@Transactional()
	public String save(LogMail logMail) {

		String query = "INSERT INTO log_mail(mail_to, note) VALUES(:mail_to, :note);";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(logMail);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		logger.info("Inserita mail {}", logMail.getMail_to());
		return Def.STR_OK;
	}
	
	@Transactional()
	public String save(String mailTo, String note) {
		
		LogMail log = new LogMail();
		log.setMail_to(mailTo);
		log.setNote(note);
		return save(log);
	}
	

}
