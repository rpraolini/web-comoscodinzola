package it.asso.core.dao.vaccinazioni;

import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.vaccinazioni.Vaccinazioni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class VaccinazioniScheduledDAO{
	
private static final Logger logger = LoggerFactory.getLogger(VaccinazioniScheduledDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public VaccinazioniScheduledDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @return List<Vaccinazioni>
	 */
	@Transactional("txManagerAssoDS")
	public List<Vaccinazioni> getVaccinazioni() {
		
		
		String queryStr = "Select a.da_inviare_7, a.da_inviare_15, a.id_animale, b.nome, a.dt_evento, a.dt_richiamo, concat(a.tipo_evento_clinico,' (',a.evento,')') evento " +
				"From (Select  rec.id_animale,  rec.dt_evento,  rec.dt_richiamo,  tec.tipo_evento_clinico,  rec.id_tipo_evento,  ec.evento, " + 
				"         Case " + 
				"             When Date_Add(CurDate(), Interval 7 Day) = rec.dt_richiamo " + 
				"             Then 1 " + 
				"             Else 0 " + 
				"         End da_inviare_7, " + 
				"         Case " + 
				"             When Date_Add(CurDate(), Interval 15 Day) = rec.dt_richiamo " + 
				"             Then 1 " + 
				"             Else 0 " + 
				"         End da_inviare_15 " + 
				"     From " + 
				"         an_r_evento_clinico rec Inner Join " + 
				"         an_x_evento_clinico ec On rec.id_tipo_evento = ec.id_tipo_evento Inner Join " + 
				"         an_x_tipo_evento_clinico tec On ec.id_tipo_evento_clinico = tec.id_tipo_evento_clinico " + 
				"     Where " + 
				"         rec.dt_richiamo Is Not Null) a, an_animale b " + 
				"Where a.id_animale=b.id_animale and  (a.da_inviare_7 = 1) Or (a.da_inviare_15 = 1)";

		try{
			return jdbcTemplate.query(queryStr, new VaccinazioniRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		} 
	}
	
	private static class VaccinazioniRowMapper extends BaseRowMapper<Vaccinazioni> {
		public VaccinazioniRowMapper() {
		}		
		@Override
		public Vaccinazioni mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Vaccinazioni o = new Vaccinazioni();
			o.setId_animale(rs.getString("id_animale"));
			o.setDt_evento(rs.getString("dt_evento"));
			o.setDt_richiamo(rs.getString("dt_richiamo"));
			o.setEvento(rs.getString("evento"));
			o.setDa_inviare_15(rs.getString("da_inviare_15"));
			o.setDa_inviare_7(rs.getString("da_inviare_7"));
			o.setNome(rs.getString("nome"));
			return o;
		}
	}
	
}
