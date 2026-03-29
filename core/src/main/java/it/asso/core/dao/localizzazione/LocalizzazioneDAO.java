package it.asso.core.dao.localizzazione;

import it.asso.core.model.localizzazione.Comune;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Repository
public class LocalizzazioneDAO {

	private static Logger logger = LoggerFactory.getLogger(LocalizzazioneDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public LocalizzazioneDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	
	/**
	 * @param 
	 * @return List<Regione>
	 */
	@Transactional(readOnly = true)
	public List<Regione> getRegioni() {
		
		String queryStr = "SELECT id, nome, stato FROM an_x_regioni order by stato, nome";

		try{
			return  jdbcTemplate.query(queryStr, new RegioneRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param 
	 * @return List<Provincia>
	 */
	@Transactional(readOnly = true)
	public List<Provincia> getProvincie(String id) {
		
		String queryStr = "SELECT id, nome, sigla_automobilistica FROM an_x_province WHERE id_regione = ? order by nome";

		try{
			return  jdbcTemplate.query(queryStr, new ProvinciaRowMapper(),  new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Comune>
	 */
	@Transactional(readOnly = true)
	public List<Comune> getComuni(String id) {
		
		String queryStr = "SELECT id, nome, longitudine, latitudine FROM an_x_comuni WHERE id_provincia = ? order by nome";

		try{
			return  jdbcTemplate.query(queryStr, new ComuneRowMapper(),  new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return Regione
	 */
	@Transactional(readOnly = true)
	public Regione getRegioneByID(String id) {
		
		if(id==null) {return null;}
		
		String queryStr = "SELECT id, nome, stato FROM an_x_regioni where id = ?";

		try{
			return  jdbcTemplate.queryForObject(queryStr, new RegioneRowMapper(), new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun risultato per ID Regione : " + id + " - " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return Regione
	 */
	@Transactional(readOnly = true)
	public List<Regione> getRegioneByNazione(String id) {
		
		if(id==null) {return null;}
		
		String queryStr = "SELECT id, nome, stato FROM an_x_regioni where stato = ?";

		try{
			return  jdbcTemplate.query(queryStr, new RegioneRowMapper(), new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun risultato per ID Regione : " + id + " - " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Regione>
	 */
	@Transactional(readOnly = true)
	public List<Regione> getRegioneByNazioneUsed(String id) {
		
		if(id==null) {return null;}
		
		String queryStr = "SELECT DISTINCT " + 
				"  r.id, " + 
				"  r.nome, " + 
				"  r.stato " + 
				"FROM " + 
				"  v_last_eventi_storici ve " + 
				"  INNER JOIN an_contatti c ON c.id_contatto = ve.id_contatto " + 
				"  INNER JOIN an_animale a ON ve.id_animale = a.id_animale " + 
				"  INNER JOIN an_x_regioni r ON r.id = c.id_regione " + 
				"WHERE " + 
				"  a.id_stato IN (3, 4) and r.stato = ?";

		try{
			return  jdbcTemplate.query(queryStr, new RegioneRowMapper(), new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun risultato per ID Regione : " + id + " - " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Provincia>
	 */
	@Transactional(readOnly = true)
	public List<Provincia> getProvincieByRegioneUsed(String id) {
		
		String queryStr = "SELECT DISTINCT   " + 
				"  p.id,   " + 
				"  p.nome,   " + 
				"  p.sigla_automobilistica   " + 
				"FROM   " + 
				"  v_last_eventi_storici ve   " + 
				"  INNER JOIN an_contatti c ON c.id_contatto = ve.id_contatto   " + 
				"  INNER JOIN an_x_province p ON p.id = c.id_provincia   " + 
				"  INNER JOIN an_animale a ON ve.id_animale = a.id_animale   " + 
				"WHERE   " + 
				"  p.id_regione = ? and a.id_stato in (3, 4)   " + 
				"ORDER BY   " + 
				"  p.nome";

		try{
			return  jdbcTemplate.query(queryStr, new ProvinciaRowMapper(),  new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param 
	 * @return Provincia
	 */
	@Transactional(readOnly = true)
	public Provincia getProvinciaByID(String id) {
		
		if(id==null) {return null;}
		
		String queryStr = "SELECT id, nome, sigla_automobilistica FROM an_x_province WHERE id = ? ";

		try{
			return  jdbcTemplate.queryForObject(queryStr, new ProvinciaRowMapper(),  new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun risultato per ID Provincia : " + id + " - " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Comune>
	 */
	@Transactional(readOnly = true)
	public Comune getComuneByID(String id) {
		
		if(id==null) {return null;}
		
		String queryStr = "SELECT id, nome, longitudine, latitudine FROM an_x_comuni WHERE id = ?";

		try{
			return  jdbcTemplate.queryForObject(queryStr, new ComuneRowMapper(),  new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun risultato per ID Comune : " + id + " - " + e.getMessage());
			return null;
		}
	}
	
	
	private static final class RegioneRowMapper implements RowMapper<Regione> {
		public Regione mapRow(ResultSet rs, int rowNum) throws SQLException {
			Regione o = new Regione();
			o.setId(rs.getString("id"));
			o.setNome(rs.getString("nome"));
			o.setStato(rs.getString("stato"));
			return o;
		}
	}
	
	
	private static final class ProvinciaRowMapper implements RowMapper<Provincia> {
		public Provincia mapRow(ResultSet rs, int rowNum) throws SQLException {
			Provincia o = new Provincia();
			o.setId(rs.getString("id"));
			o.setNome(rs.getString("nome"));
			o.setSigla(rs.getString("sigla_automobilistica"));
			return o;
		}
	}
	
	private static final class ComuneRowMapper implements RowMapper<Comune> {
		public Comune mapRow(ResultSet rs, int rowNum) throws SQLException {
			Comune o = new Comune();
			o.setId(rs.getString("id"));
			o.setNome(rs.getString("nome"));
			o.setLon(rs.getString("longitudine"));
			o.setLat(rs.getString("latitudine"));
			return o;
		}
	}
	
	

}
