package it.asso.core.dao.contabilita.sqlquery;

import java.sql.ResultSet;
import java.util.Map;

import javax.sql.DataSource;

import it.asso.core.model.contabilita.Scadenziario;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.object.SqlQuery;


public class ScadenziarioSqlQuery extends SqlQuery<Scadenziario>{

	 public ScadenziarioSqlQuery(DataSource dataSource, String sql) {
	        super(dataSource, sql);
	    }
	 
	@Override
	protected RowMapper<Scadenziario> newRowMapper(Object[] arg0, Map<?, ?> arg1) {
		return (ResultSet rs, int rowNum) -> {
			Scadenziario o = new Scadenziario();
            o.setContatto(rs.getString("contatto"));
            o.setDt_rata(rs.getString("dt_rata"));
            o.setImporto(rs.getString("importo"));
            o.setProssima(rs.getString("prossima"));
            return o;
        };
	}
	
	

}
