package it.asso.core.dao.utente;

import it.asso.core.common.Def;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.utente.AreaDati;
import it.asso.core.model.utente.AreaPermesso;
import it.asso.core.model.utente.Ruolo;
import it.asso.core.model.utente.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Repository
public class UtenteDAO {

	private static final Logger logger = LoggerFactory.getLogger(UtenteDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public UtenteDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * @param username
     * @return Utente
     */
    @Transactional(readOnly = true)
    public Utente findByUsername(String username)  {

        String queryStr = "select u.id_utente, u.id_contatto, u.account, u.abilitato, u.pwd, nullif(DATE_FORMAT(u.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, nullif(DATE_FORMAT(u.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, nullif(DATE_FORMAT(u.dt_scadenza, '%d/%m/%Y'),'') dt_scadenza, " +
                "	c.nome, c.cognome, c.cod_fiscale, c.email, c.telefono_1, c.telefono_2, c.cellulare " +
                "from ut_utente u, an_contatti c " +
                "where u.id_contatto = c.id_contatto " +
                "and u.account = ?";
        try {

            Utente utente = (Utente) jdbcTemplate.queryForObject(queryStr, new UtenteRowMapper(), new Object[] { username });
			utente.setRuoli(getRuoliByID(utente.getId_utente()));
			utente.setPermessi(getPermessiByID(utente.getId_utente()));
			return utente;

        } catch (EmptyResultDataAccessException e) {
            logger.error(e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

	/**
	 * @param 
	 * @return List<Utente>
	 */
	@Transactional(readOnly = true)
	public List<Utente> getAllUsers()  {

		String queryStr = "select u.id_utente, u.id_contatto, u.account, u.abilitato, u.pwd, nullif(DATE_FORMAT(u.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, nullif(DATE_FORMAT(u.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, nullif(DATE_FORMAT(u.dt_scadenza, '%d/%m/%Y'),'') dt_scadenza, " + 
							"	c.nome, c.cognome, c.cod_fiscale, c.email, c.telefono_1, c.telefono_2, c.cellulare " + 
							"from ut_utente u, an_contatti c " + 
							"where u.id_contatto = c.id_contatto";

		try {
			
			List<Utente> utenti = jdbcTemplate.query(queryStr, new UtenteRowMapper());
			for (Utente utente : utenti) {
				utente.setRuoli(getRuoliByID(utente.getId_utente()));
				utente.setPermessi(getPermessiByID(utente.getId_utente()));
			}
			
			return utenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun utente trovato. - " + e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param search
	 * @return List<Utente>
	 */
	@Transactional(readOnly = true)
	public List<Utente> getUsersBySearch(String search)  {
		search = "%" + search + "%";
		String queryStr = "select u.id_utente, u.id_contatto, u.account, u.abilitato, u.pwd, nullif(DATE_FORMAT(u.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, nullif(DATE_FORMAT(u.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, nullif(DATE_FORMAT(u.dt_scadenza, '%d/%m/%Y'),'') dt_scadenza, " + 
							"	c.nome, c.cognome, c.cod_fiscale, c.email, c.telefono_1, c.telefono_2, c.cellulare " + 
							"from ut_utente u, an_contatti c " + 
							"where u.id_contatto = c.id_contatto and upper(concat(c.nome, c.cognome)) like (?)";

		try {
			
			List<Utente> utenti = jdbcTemplate.query(queryStr, new UtenteRowMapper(), new Object[] { search.toUpperCase() });
			for (Utente utente : utenti) {
				utente.setRuoli(getRuoliByID(utente.getId_utente()));
				utente.setPermessi(getPermessiByID(utente.getId_utente()));
			}
			
			return utenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error("Nessun utente trovato. - " + e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param username, password
	 * @return Utente
	 */
	@Transactional(readOnly = true)
	public Utente findByAccountAndPassword(String username, String password)  {

		String queryStr = "select u.id_utente, u.id_contatto, u.account, u.abilitato, u.pwd, nullif(DATE_FORMAT(u.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, nullif(DATE_FORMAT(u.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, nullif(DATE_FORMAT(u.dt_scadenza, '%d/%m/%Y'),'') dt_scadenza, " + 
							"	c.nome, c.cognome, c.cod_fiscale, c.email, c.telefono_1, c.telefono_2, c.cellulare " + 
							"from ut_utente u, an_contatti c " + 
							"where u.id_contatto = c.id_contatto " + 
							"and u.account = ? and u.pwd = ? ";

		try {
			
			Utente utente = (Utente) jdbcTemplate.queryForObject(queryStr, new UtenteRowMapper(), new Object[] { username, password });
			utente.setRuoli(getRuoliByID(utente.getId_utente()));
			utente.setPermessi(getPermessiByID(utente.getId_utente()));
			return utente;

		} catch (EmptyResultDataAccessException e) {
			logger.error("Utente non trovato : " + username + " - " + e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	

	
	/**
	 * @param username
	 * @return ID
	 */
	@Transactional(readOnly = true)
	public int findIDByAccount(String username)  {
		String queryStr = "select u.id_utente from ut_utente u where u.account = ?";
		try {
			int id = jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { username });
			return id;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	
	/**
	 * @param id
	 * @return Utente
	 */
	@Transactional(readOnly = true)
	public Utente getUtenteByID(String id)  {

		String queryStr = "select u.id_utente, u.id_contatto, u.account, u.abilitato, u.pwd, nullif(DATE_FORMAT(u.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, nullif(DATE_FORMAT(u.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, nullif(DATE_FORMAT(u.dt_scadenza, '%d/%m/%Y'),'') dt_scadenza, " + 
							"	c.nome, c.cognome, c.cod_fiscale, c.email, c.telefono_1, c.telefono_2, c.cellulare " + 
							"from ut_utente u, an_contatti c " + 
							"where u.id_contatto = c.id_contatto " + 
							"and u.id_utente = ?";
		try {
	
			Utente utente = (Utente) jdbcTemplate.queryForObject(queryStr, new UtenteRowMapper(), new Object[] { id });
			utente.setRuoli(getRuoliByID(utente.getId_utente()));
			utente.setPermessi(getPermessiByID(utente.getId_utente()));
			return utente;

		} catch (EmptyResultDataAccessException e) {
			logger.error("Utente non trovato con id : " + id + " - " +  e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error("Errore nella ricerca utente con id : " + id + " - " +  e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return Utente
	 */
	@Transactional(readOnly = true)
	public Utente getUtenteByIDContatto(String id)  {

		String queryStr = "select u.id_utente, u.id_contatto, u.account, u.abilitato, u.pwd, nullif(DATE_FORMAT(u.dt_aggiornamento, '%d/%m/%Y'),'') dt_aggiornamento, nullif(DATE_FORMAT(u.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, nullif(DATE_FORMAT(u.dt_scadenza, '%d/%m/%Y'),'') dt_scadenza, " + 
							"	c.nome, c.cognome, c.cod_fiscale, c.email, c.telefono_1, c.telefono_2, c.cellulare " + 
							"from ut_utente u, an_contatti c " + 
							"where u.id_contatto = c.id_contatto " + 
							"and u.id_contatto = ?";
		try {
	
			Utente utente = (Utente) jdbcTemplate.queryForObject(queryStr, new UtenteRowMapper(), new Object[] { id });
			utente.setRuoli(getRuoliByID(utente.getId_utente()));
			utente.setPermessi(getPermessiByID(utente.getId_utente()));
			return utente;

		} catch (EmptyResultDataAccessException e) {
			logger.error("Utente non trovato con id : " + id + " - " +  e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error("Errore nella ricerca utente con id : " + id + " - " +  e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param idUtente
	 * @return List<Ruolo>
	 */
	@Transactional(readOnly = true)
	public List<Ruolo> getRuoliByID(String idUtente)  {
		List<Ruolo> ruoli = new ArrayList<Ruolo>();
		
		String queryStr = "SELECT id_ruolo, id_utente FROM ut_r_ruolo" + " WHERE id_utente = ?";

		try {
			
			
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(queryStr, new Object[] { idUtente });
			
			for (Map<String, Object> row : rows) {
				ruoli.add(getRuoloByID(row.get("id_ruolo").toString()));
			}

			return ruoli;

		} catch (EmptyResultDataAccessException e) {
			
			logger.error(e.getMessage());
			return null;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Ruolo>
	 */
	@Transactional(readOnly = true)
	public List<Ruolo> getRuoli()  {
			
		String queryStr = "SELECT id_ruolo, ruolo FROM ut_x_ruolo order by ruolo";

		try {
			return jdbcTemplate.query(queryStr,  new RuoloRowMapper());
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param utente
	 * @return utente
	 */
	@Transactional()
	public Utente saveRuolo(Utente utente) {
		
		String query = "UPDATE ut_r_ruolo SET id_ruolo = ? WHERE id_utente = ?";

		jdbcTemplate.update(query,new Object[] { utente.getRuoli().get(0).getId_ruolo(), utente.getId_utente() });
		
		query = "UPDATE ut_permessi SET `read` = 0, `write` = 0, id_ruolo = ? WHERE id_utente = ?";

		jdbcTemplate.update(query,new Object[] {utente.getRuoli().get(0).getId_ruolo(), utente.getId_utente() });

		return getUtenteByID(utente.getId_utente());
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional()
	public String deleteUser(String id) {
		String query = "SELECT count(*) FROM an_r_attivita WHERE id_utente = ?";
		int s = jdbcTemplate.queryForObject(query, Integer.class,new Object[] {id});
		if(s == 0) {
			query = "DELETE FROM ut_utente WHERE id_utente = ?";
			jdbcTemplate.update(query,new Object[] {id});
			return Def.STR_OK;
		}else {
			return Def.STR_ERROR_1001;
		}
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional()
	public String enableUser(String id) {
		String query = "UPDATE ut_utente SET abilitato = 1 WHERE id_utente = ?";
		jdbcTemplate.update(query,new Object[] {id});
		return Def.STR_OK;
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional()
	public String disableUser(String id) {
		String query = "UPDATE ut_utente SET abilitato = 0 WHERE id_utente = ?";
		jdbcTemplate.update(query,new Object[] {id});
		return Def.STR_OK;
	}
	
	/**
	 * @param u
	 * @return String
	 */
	@Transactional()
	public String updateAccesso(Utente u, String sessionID) {
		String query = "UPDATE ut_utente SET sessionID = ?, dt_ultimo_accesso = now(), session_state=1 WHERE id_utente = ?";
		jdbcTemplate.update(query,new Object[] {sessionID, u.getId_utente()});
		query = "INSERT INTO u_accessi(dt_accesso,account,sessionID) VALUES (now(),?,?);";
		jdbcTemplate.update(query,new Object[] {u.getAccount(), sessionID});
		return Def.STR_OK;
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional()
	public String updateSession(String id) {
		String query = "UPDATE ut_utente SET session_state = 0 WHERE id_utente = ?";
		jdbcTemplate.update(query,new Object[] {id});
		query = "select u.account from ut_utente u where u.id_utente = ?";
		return jdbcTemplate.queryForObject(query, String.class, new Object[] { id });
	}

	
	/**
	 * @param idRuolo, idUtente
	 * @return String
	 */
	@Transactional()
	public String insertRuolo(String idRuolo, String idUtente) {
		
		String query = "INSERT INTO ut_r_ruolo (id_ruolo, id_utente) VALUES(?,?)";

		jdbcTemplate.update(query, new Object[] { idRuolo, idUtente });

		return Def.STR_OK;
	}
	
	/**
	 * @param utente
	 * @return utente
	 */
	@Transactional()
	public Utente savePermessi(Utente utente) {
		
		String query = "DELETE FROM ut_permessi WHERE id_utente = ?";
		jdbcTemplate.update(query,new Object[] { utente.getId_utente() });
		
		query = "INSERT INTO ut_permessi (id_area,id_utente,id_ruolo,`read`,`write`) VALUES(?,?,?,?,?)";
		for (AreaPermesso a : utente.getPermessi()) {
			
			jdbcTemplate.update(query,new Object[] {a.getId_area(),a.getId_utente(),a.getId_ruolo(),a.getRead(),a.getWrite() });
		}

		return getUtenteByID(utente.getId_utente());
	}

	
	/**
	 * @param utente
	 * @return utente
	 */
	@Transactional()
	public Utente insertPermessi(Utente utente) {
		
		String query = "DELETE FROM ut_permessi WHERE id_utente = ?";
		jdbcTemplate.update(query,new Object[] { utente.getId_utente() });
		
		query = "INSERT INTO ut_permessi (id_area,id_utente,id_ruolo,`read`,`write`) VALUES(?,?,?,?,?)";
		for (AreaDati a : getAreeDati()) {
			
			jdbcTemplate.update(query,new Object[] {a.getId_area(),utente.getId_utente(),utente.getRuoli().get(0).getId_ruolo(),0,0 });
		}

		return getUtenteByID(utente.getId_utente());
	}

	
	private class UtenteRowMapper implements RowMapper<Utente> {
		public Utente mapRow(ResultSet rs, int rowNum) throws SQLException {
			Utente o = new Utente();
			o.setId_utente(rs.getString("id_utente"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setAccount(rs.getString("account"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setDt_scadenza(rs.getString("dt_scadenza"));
			o.setCognome(rs.getString("cognome"));
			o.setNome(rs.getString("nome"));
			o.setCod_fiscale(rs.getString("cod_fiscale"));
			o.setCellulare(rs.getString("cellulare"));
			o.setEmail(rs.getString("email"));
			o.setTelefono_1(rs.getString("telefono_1"));
			o.setTelefono_2(rs.getString("telefono_2"));
			o.setAbilitato(rs.getString("abilitato"));
			o.setPwd(rs.getString("pwd"));
			return o;
		}
	}
	
	
	
/*********************************** RUOLI **************************************/
	
	/**
	 * @param idRuolo
	 * @return Ruolo
	 */
	@Transactional(readOnly = true)
	public Ruolo getRuoloByID(String idRuolo)  {

		String queryStr = "SELECT id_ruolo, ruolo FROM ut_x_ruolo" + " WHERE id_ruolo = ?";

		try {
	
			Ruolo ruolo = (Ruolo) jdbcTemplate.queryForObject(queryStr, new RuoloRowMapper(), new Object[] { idRuolo });
			return ruolo;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	private class RuoloRowMapper implements RowMapper<Ruolo> {
		public Ruolo mapRow(ResultSet rs, int rowNum) throws SQLException {
			Ruolo o = new Ruolo();
			o.setId_ruolo(rs.getString("id_ruolo"));
			o.setRuolo(rs.getString("ruolo"));
			return o;
		}
	}
	
/*********************************** PERMESSI **************************************/
	
	/**
	 * @param idUtente
	 * @return List<AreaPermesso>
	 */
	@Transactional(readOnly = true)
	public List<AreaPermesso> getPermessiByID(String idUtente)  {

		String queryStr = "SELECT u.id_utente, p.id_area, p.id_ruolo, a.area, r.ruolo, p.read, p.write " + 
				"FROM ut_utente u, ut_permessi p, ut_x_aree a, ut_x_ruolo r " + 
				"WHERE u.id_utente = p.id_utente and p.id_area = a.id_area and p.id_ruolo = r.id_ruolo " + 
				"and u.id_utente = ?";

		try {
	
			List<AreaPermesso> permessi = (List<AreaPermesso>) jdbcTemplate.query(queryStr, new AreaPermessoRowMapper(), new Object[] { idUtente });
			return permessi;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @return List<AreaDati>
	 */
	@Transactional(readOnly = true)
	public List<AreaDati> getAreeDati()  {

		String queryStr = "SELECT id_area, area FROM ut_x_aree";

		try {
	
			List<AreaDati> permessi = (List<AreaDati>) jdbcTemplate.query(queryStr, new AreaDatiRowMapper());
			return permessi;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param area
	 * @return utente
	 */
	@Transactional()
	public String saveArea(AreaDati area) {
		
		String query = "INSERT INTO ut_x_aree (area) VALUES(:area)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(area);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_area" });
		
		return String.valueOf(keyHolder.getKey());
	}
	
	
	/**
	 * @param c
	 * @return String
	 */
	@Transactional()
	public String saveUtente(Contatto c) {
		String account = c.getNome().substring(0, 1).toLowerCase() + c.getCognome().trim().toLowerCase();
		Utente u = new Utente();
		u.setId_contatto(c.getId_contatto());
		u.setAccount(account);
		u.setPwd(Def.PWD_DEFAULT);
		
		String query = "INSERT INTO ut_utente (id_contatto, account, pwd) VALUES(:id_contatto, :account, :pwd);";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(u);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_utente" });
		
		return String.valueOf(keyHolder.getKey());
	}
	
	
	
	private class AreaPermessoRowMapper implements RowMapper<AreaPermesso> {
		public AreaPermesso mapRow(ResultSet rs, int rowNum) throws SQLException {
			AreaPermesso o = new AreaPermesso();
			o.setId_area(rs.getString("id_area"));
			o.setId_ruolo(rs.getString("id_ruolo"));
			o.setId_utente(rs.getString("id_utente"));
			o.setArea(rs.getString("area"));
			o.setRuolo(rs.getString("ruolo"));
			o.setRead(rs.getString("read"));
			o.setWrite(rs.getString("write"));
			return o;
		}
	}
	
	private class AreaDatiRowMapper implements RowMapper<AreaDati> {
		public AreaDati mapRow(ResultSet rs, int rowNum) throws SQLException {
			AreaDati o = new AreaDati();
			o.setId_area(rs.getString("id_area"));
			o.setArea(rs.getString("area"));
			return o;
		}
	}

}
