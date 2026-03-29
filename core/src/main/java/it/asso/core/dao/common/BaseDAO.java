package it.asso.core.dao.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BaseDAO {

	public JdbcTemplate jdbcTemplate;
	 
	  public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
	        this.jdbcTemplate = jdbcTemplate;
	  }
	  
}
