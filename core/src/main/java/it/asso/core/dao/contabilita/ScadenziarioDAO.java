package it.asso.core.dao.contabilita;

import it.asso.core.dao.contabilita.sqlquery.ScadenziarioSqlQuery;
import it.asso.core.model.contabilita.Scadenziario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.object.SqlQuery;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class ScadenziarioDAO{

    private final JdbcTemplate jdbcTemplate;

    public ScadenziarioDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	 @Autowired
	private DataSource dataSource;
	private SqlQuery<Scadenziario> scadenziarioSqlQuery;
	    
	public List<Scadenziario> getScadenziario() {
        List<Scadenziario> result = scadenziarioSqlQuery.execute();
        return result;
    }
	
	
    @SuppressWarnings("unused")
	private void postConstruct() {
		scadenziarioSqlQuery = new ScadenziarioSqlQuery(dataSource, "SELECT " +
				"  sp.contatto, " + 
				"  date_format(sp.dt_rata,'%d/%m/%Y') dt_rata, " + 
				"  Sum(sp.importo_rata) AS importo, case when sp.dt_rata <= curdate() then 0 else 1 end prossima " + 
				"FROM " + 
				"  v_scadenze_pensione sp " + 
				"GROUP BY " + 
				"  sp.contatto, " + 
				"  sp.dt_rata, case when sp.dt_rata <= curdate() then 0 else 1 end ORDER BY sp.contatto, sp.dt_rata asc");
    }
	
}
