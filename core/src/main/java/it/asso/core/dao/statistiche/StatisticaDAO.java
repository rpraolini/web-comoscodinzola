package it.asso.core.dao.statistiche;

import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.statistiche.Marker;
import it.asso.core.model.statistiche.SimpleResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;



@Repository
public class StatisticaDAO {

	private static final Logger logger = LoggerFactory.getLogger(StatisticaDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public StatisticaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
	 * @return String
	 */
	@Transactional()
	public String getCountAnimali() {
		
		
		String queryStr = "SELECT count(id_animale) count FROM an_animale";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class).toString();

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @return List<SimpleResultSet>
	 */
	@Transactional()
	public List<SimpleResultSet> getCountAnimaliByStato() {
		
		
		String queryStr = "SELECT count(a.id_animale) count, b.descr_stato value FROM an_animale a, an_x_stati b where a.id_stato = b.id_stato group by b.descr_stato order by 1 desc";

		try{
			return jdbcTemplate.query(queryStr, new SimpleResultSetRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @return String
	 */
	@Transactional()
	public String getCountContatti() {
		
		
		String queryStr = "SELECT count(id_contatto) count FROM an_contatti";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class).toString();

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @return List<SimpleResultSet>
	 */
	@Transactional()
	public List<SimpleResultSet> getCountContattiByTipo() {
		
		
		String queryStr = "SELECT count(a.id_contatto) count, b.tipo_contatto value FROM an_contatti a, an_x_tipo_contatto b where a.id_tipo_contatto = b.id_tipo_contatto group by b.tipo_contatto order by 1 desc";

		try{
			return jdbcTemplate.query(queryStr, new SimpleResultSetRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	private static class SimpleResultSetRowMapper extends BaseRowMapper<SimpleResultSet> {
		public SimpleResultSetRowMapper() {}		
		@Override
		public SimpleResultSet mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			SimpleResultSet o = new SimpleResultSet();
			o.setValue_1(rs.getString("count"));
			o.setValue_2(rs.getString("value"));
			return o;
		}
	
	}
	
	/**
	 * @return List<Marker>
	 */
	@Transactional(readOnly = true)
	public List<Marker> getAnimaliAdottati(String params) {
		
		String queryStr = "Select " + 
				"    a.id_animale, " + 
				"    a.id_stato, " + 
				"    a.nome nome_animale, " + 
				"    ap.id_contatto_proprietario, " + 
				"    ap.corrente, " + 
				"    p.latitudine, " + 
				"    p.longitudine, " + 
				"    p.cognome, " + 
				"    p.nome, " + 
				"    p.comune, " + 
				"    g.sigla_automobilistica, " + 
				"    concat('"+ params + "', a.id_animale, '/', f.nome_file_t) url_t " + 
				"From " + 
				"    an_animale a Inner Join " + 
				"    an_animale_proprietari ap On ap.id_animale = a.id_animale Inner Join " + 
				"    an_contatti p On ap.id_contatto_proprietario = p.id_contatto Inner Join " + 
				"    an_x_comuni c On c.id = p.id_comune Inner Join " + 
				"    an_x_province g On g.id = c.id_provincia Inner Join " + 
				"    an_foto f On f.id_animale = a.id_animale " + 
				"Where " + 
				"    a.id_stato In (6, 7, 98) And " + 
				"    ap.corrente = 1 And " + 
				"    ap.id_contatto_proprietario != 2 And " + // tolta rosetta
				"    f.id_tipo_foto = 1 and p.latitudine is not null";

		try{
			return jdbcTemplate.query(queryStr, new MarkerRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @return List<Marker>
	 */
	@Transactional(readOnly = true)
	public List<Marker> getPreaffidanti() {
		
		String queryStr = "Select " + 
				"    p.latitudine, " + 
				"    p.longitudine, " + 
				"    p.cognome, " + 
				"    p.nome, " + 
				"    p.comune, " + 
				"    g.sigla_automobilistica," +
				"    p.email, " + 
				"    p.telefono_1, " + 
				"    p.cellulare " + 
				"From " + 
				"    an_contatti p Inner Join " + 
				"    an_x_comuni c On c.id = p.id_comune Inner Join " + 
				"    an_x_province g On g.id = c.id_provincia Inner Join " + 
				"    an_r_qualifiche_contatto a On a.id_contatto = p.id_contatto " + 
				"Where " + 
				"    a.id_qualifica = 4 and p.latitudine is not null";

		try{
			return jdbcTemplate.query(queryStr, new MarkerPreaffidantiRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private static class MarkerPreaffidantiRowMapper extends BaseRowMapper<Marker> {
		public MarkerPreaffidantiRowMapper() {}		
		@Override
		public Marker mapRowImpl(ResultSet rs, int i) throws SQLException {
			Marker o = new Marker();
			o.setNome(rs.getString("nome"));
			o.setLatitudine(rs.getString("latitudine"));
			o.setLongitudine(rs.getString("longitudine"));
			o.setCognome(rs.getString("cognome"));
			o.setComune(rs.getString("comune"));
			o.setSigla_automobilistica(rs.getString("sigla_automobilistica"));
			o.setTelefono_1(rs.getString("telefono_1"));
			o.setEmail(rs.getString("email"));
			o.setCellulare(rs.getString("cellulare"));
			return o;
		}
	}
	
	private static class MarkerRowMapper extends BaseRowMapper<Marker> {
		public MarkerRowMapper() {}		
		@Override
		public Marker mapRowImpl(ResultSet rs, int i) throws SQLException {
			Marker o = new Marker();
			o.setId_animale(rs.getString("id_animale"));
			o.setNome(rs.getString("nome"));
			o.setId_stato(rs.getString("id_stato"));
			o.setId_contatto_proprietario(rs.getString("id_contatto_proprietario"));
			o.setCorrente(rs.getString("corrente"));
			o.setLatitudine(rs.getString("latitudine"));
			o.setLongitudine(rs.getString("longitudine"));
			o.setCognome(rs.getString("cognome"));
			o.setComune(rs.getString("comune"));
			o.setSigla_automobilistica(rs.getString("sigla_automobilistica"));
			o.setNome_animale(rs.getString("nome_animale"));
			o.setUrl_t(rs.getString("url_t"));
			return o;
		}
	}
	
	
}
