package it.asso.core.dao.animali.gestione;

import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.animali.gestione.TipoIter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Repository
public class IterDAO {

	private static Logger logger = LoggerFactory.getLogger(IterDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public IterDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	/**
	 * @param idPratica
	 * @return List<Richiesta>
	 */
	@Transactional()
	public List<Iter> getIterByIdPratica(String idPratica) {
		
		String queryStr = "SELECT a.id_iter, a.id_animale, a.id_tipo_iter, a.nome, a.localita, a.note, b.tipo_iter, b.colore, a.account, " + 
				"nullif(DATE_FORMAT(a.dt_aggiornamento, '%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " +
				"nullif(DATE_FORMAT(a.quest_invio, '%d/%m/%Y'),'') quest_invio, " +
				"nullif(DATE_FORMAT(a.quest_ritorno, '%d/%m/%Y'),'') quest_ritorno, " +
				"a.quest_f, a.quest_key, a.email, a.telefono, a.id_contatto, a.esito, a.id_contatto_vol, a.id_contatto_adottante, a.id_contatto_proprietario, " +
				"nullif(DATE_FORMAT(a.dt_colloquio, '%d/%m/%Y'),'') dt_colloquio, nullif(DATE_FORMAT(a.dt_consegna, '%d/%m/%Y'),'') dt_consegna, a.contributo, a.id_pratica " +
				"FROM an_r_iter a, an_x_tipo_iter b " +
				"WHERE a.id_tipo_iter = b.id_tipo_iter " +
				"	AND a.id_pratica = ? ORDER BY b.ordine asc, a.dt_aggiornamento desc";

		try{
			return jdbcTemplate.query(queryStr, new IterRowMapper(), new Object[] { idPratica });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	

	/**
	 * @param 
	 * @return List<Richiesta>
	 */
	@Transactional()
	public List<Iter> getIterByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT a.id_iter, a.id_animale, a.id_tipo_iter, a.nome, a.localita, a.note, b.tipo_iter, b.colore, a.account, " + 
				"nullif(DATE_FORMAT(a.dt_aggiornamento, '%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " + 
				"nullif(DATE_FORMAT(a.quest_invio, '%d/%m/%Y'),'') quest_invio, " + 
				"nullif(DATE_FORMAT(a.quest_ritorno, '%d/%m/%Y'),'') quest_ritorno, " + 
				"a.quest_f, a.quest_key, a.email, a.telefono, a.id_contatto, a.esito, a.id_contatto_vol, a.id_contatto_adottante, a.id_contatto_proprietario, " +
				"nullif(DATE_FORMAT(a.dt_colloquio, '%d/%m/%Y'),'') dt_colloquio, nullif(DATE_FORMAT(a.dt_consegna, '%d/%m/%Y'),'') dt_consegna, a.contributo, a.id_pratica " + 
				"FROM an_r_iter a, an_x_tipo_iter b " + 
				"WHERE a.id_tipo_iter = b.id_tipo_iter " + 
				"	AND a.id_animale = ? ORDER BY a.dt_aggiornamento desc";

		try{
			return jdbcTemplate.query(queryStr, new IterRowMapper(), new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return List<Richiesta>
	 */
	@Transactional()
	public List<Iter> getIterByIdAnimale(String idAnimale, String idTipoIter) {
		
		
		String queryStr = "SELECT a.id_iter, a.id_animale, a.id_tipo_iter, a.nome, a.localita, a.note, b.tipo_iter, b.colore, a.account, " + 
				"nullif(DATE_FORMAT(a.dt_aggiornamento, '%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " + 
				"nullif(DATE_FORMAT(a.quest_invio, '%d/%m/%Y'),'') quest_invio, " + 
				"nullif(DATE_FORMAT(a.quest_ritorno, '%d/%m/%Y'),'') quest_ritorno, " + 
				"a.quest_f, a.quest_key, a.email, a.telefono, a.id_contatto, a.esito, a.id_contatto_vol, a.id_contatto_adottante, a.id_contatto_proprietario, " +
				"nullif(DATE_FORMAT(a.dt_colloquio, '%d/%m/%Y'),'') dt_colloquio, nullif(DATE_FORMAT(a.dt_consegna, '%d/%m/%Y'),'') dt_consegna, a.contributo, a.id_pratica " + 
				"FROM an_r_iter a, an_x_tipo_iter b " + 
				"WHERE a.id_tipo_iter = b.id_tipo_iter " + 
				"	AND a.id_animale = ? and a.id_tipo_iter = ? ORDER BY a.dt_aggiornamento desc";

		try{
			List<Iter> result = jdbcTemplate.query(queryStr, new IterRowMapper(), new Object[] { idAnimale, idTipoIter });
			return result;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	
	/**
	 * @param 
	 * @return Richiesta
	 */
	@Transactional()
	public Iter getIterByKey(String key) {
		
		
		String queryStr = "SELECT a.id_iter, a.id_animale, a.id_tipo_iter, a.nome, a.localita, a.note, b.tipo_iter, b.colore, a.account, " + 
				"nullif(DATE_FORMAT(a.dt_aggiornamento, '%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " + 
				"nullif(DATE_FORMAT(a.quest_invio, '%d/%m/%Y'),'') quest_invio, " + 
				"nullif(DATE_FORMAT(a.quest_ritorno, '%d/%m/%Y'),'') quest_ritorno, " + 
				"a.quest_f, a.quest_key, a.email, a.telefono, a.id_contatto, a.esito, a.id_contatto_vol, a.id_contatto_adottante, a.id_contatto_proprietario, " +
				"nullif(DATE_FORMAT(a.dt_colloquio, '%d/%m/%Y'),'') dt_colloquio, nullif(DATE_FORMAT(a.dt_consegna, '%d/%m/%Y'),'') dt_consegna, a.contributo, a.id_pratica " + 
				"FROM an_r_iter a, an_x_tipo_iter b " + 
				"WHERE a.id_tipo_iter = b.id_tipo_iter " + 
				"	AND a.quest_key = ? and a.quest_f = 'I'";

		try{
			return jdbcTemplate.queryForObject(queryStr, new IterRowMapper(), new Object[] { key });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param 
	 * @return Richiesta
	 */
	@Transactional()
	public Iter getIterByID(String idIter) {
		
		
		String queryStr = "SELECT a.id_iter, a.id_animale, a.id_tipo_iter, a.nome, a.localita, a.note, b.tipo_iter, b.colore, a.account, " + 
				"nullif(DATE_FORMAT(a.dt_aggiornamento, '%d/%m/%Y %H:%i:%s'),'') dt_aggiornamento, " + 
				"nullif(DATE_FORMAT(a.quest_invio, '%d/%m/%Y'),'') quest_invio, " + 
				"nullif(DATE_FORMAT(a.quest_ritorno, '%d/%m/%Y'),'') quest_ritorno, " + 
				"a.quest_f, a.quest_key, a.email, a.telefono, a.id_contatto, a.esito, a.id_contatto_vol, a.id_contatto_adottante, a.id_contatto_proprietario, " +
				"nullif(DATE_FORMAT(a.dt_colloquio, '%d/%m/%Y'),'') dt_colloquio, nullif(DATE_FORMAT(a.dt_consegna, '%d/%m/%Y'),'') dt_consegna, a.contributo, a.id_pratica " + 
				"FROM an_r_iter a, an_x_tipo_iter b " + 
				"WHERE a.id_tipo_iter = b.id_tipo_iter " + 
				"	AND a.id_iter = ? ";

		try{
			return jdbcTemplate.queryForObject(queryStr, new IterRowMapper(), new Object[] { idIter });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idPratica
	 * @return String
	 */
	@Transactional()
	public String getTipoIterByPratica(String idPratica) {
		
		String queryStr = "SELECT max( a.id_tipo_iter) FROM an_r_iter a " + 
				"WHERE a.id_pratica = ?";
		try{
			return jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { idPratica });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param id
	 * @return String
	 */
	@Transactional()
	public String getTipoIterByID(String id) {
		
		String queryStr = "SELECT a.id_tipo_iter FROM an_r_iter a " + 
				"WHERE a.id_iter = ?";
		try{
			return jdbcTemplate.queryForObject(queryStr, String.class, new Object[] { id });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
		
	/**
	 * @param iter
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(Iter iter) {
		String id = "";
		
		if (iter.getId_iter() == null) {
			id = save(iter);
		}else {
			id = update(iter);
		}
		
		return id;
	}
	
	/**
	 * @param iter
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdateAdozione(Iter iter) {
		String id = "";
		
		if (iter.getId_iter() == null) {
			id = save(iter);
		}else {
			id = updateAdozione(iter);
		}
		
		return id;
	}

		
	/**
	 * @param iter
	 * @return
	 */
	
	@Transactional()
	public String updateAdozione(Iter iter) {

		final String query = "UPDATE an_r_iter" + 
				"	SET" + 
				"	id_contatto_adottante = :id_contatto_adottante, id_contatto_proprietario = :id_contatto_proprietario, id_contatto_vol = :id_contatto_vol, contributo = :contributo, note = :note, " +
				"	account = :account " +
				"	WHERE id_iter = :id_iter";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(iter);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return iter.getId_iter();
	}
	
	
	/**
	 * @param iter
	 * @return
	 */
	
	@Transactional()
	public String save(Iter iter) {

		final String query = "INSERT INTO an_r_iter " +
										"(id_animale, " +
										"id_tipo_iter," +
										"nome, note, account, quest_f, quest_key, email, localita, telefono, id_contatto, esito, id_contatto_vol, dt_colloquio, id_contatto_adottante, id_contatto_proprietario, contributo, id_pratica, dt_consegna)" +
										" VALUES " +
										"(:id_animale," +
										":id_tipo_iter, " +
										":nome, :note, :account, :quest_f, :quest_key, :email, :localita, :telefono, :id_contatto, :esito, :id_contatto_vol, str_to_date(nullif(:dt_colloquio,''), '%d/%m/%Y'), :id_contatto_adottante, :id_contatto_proprietario, :contributo, :id_pratica, str_to_date(nullif(:dt_consegna,''), '%d/%m/%Y') " +
										")";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(iter);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_iter" });

		return String.valueOf(keyHolder.getKey());
	}
	
	
	/**
	 * @param iter
	 * @return
	 */
	
	@Transactional()
	public String update(Iter iter) {

		final String query = "UPDATE an_r_iter" + 
				"	SET" + 
				"	nome = :nome," + 
				"	localita = :localita," + 
				"	note = :note," + 
				"	quest_f = :quest_f," + 
				"	account = :account, email = :email," + 
				"	telefono = :telefono, id_contatto = :id_contatto, id_contatto_vol = :id_contatto_vol, id_contatto_adottante = :id_contatto_adottante, id_contatto_proprietario = :id_contatto_proprietario," + 
				"	esito = :esito," + 
				"	contributo = :contributo," + 
				"   dt_colloquio = str_to_date(nullif(:dt_colloquio,''), '%d/%m/%Y'), " +
				"   dt_consegna = str_to_date(nullif(:dt_consegna,''), '%d/%m/%Y') " +
				"	WHERE id_iter = :id_iter";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(iter);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return iter.getId_iter();
	}
	
	
	/**
	 * @param iter
	 * @return
	 */
	
	@Transactional()
	public String delete(Iter iter) {

		final String query = "DELETE FROM an_r_iter WHERE id_iter = :id_iter";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(iter);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return iter.getId_iter();
	}
	
	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String delete(String id) {

		Iter iter = new Iter();
		iter.setId_iter(id);

		return delete(iter);
	}
		
	
	private static class IterRowMapper extends BaseRowMapper<Iter> {
		public IterRowMapper() {
		}		
		@Override
		public Iter mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Iter o = new Iter();
			o.setId_iter(rs.getString("id_iter"));
			o.setId_tipo_iter(rs.getString("id_tipo_iter"));
			o.setNome(rs.getString("nome"));
			o.setNote(rs.getString("note"));
			o.setId_animale(rs.getString("id_animale"));
			o.setAccount(rs.getString("account"));
			o.setDt_aggiornamento(rs.getString("dt_aggiornamento"));
			o.setTipo_iter(rs.getString("tipo_iter"));
			o.setColore(rs.getString("colore"));
			o.setLocalita(rs.getString("localita"));
			o.setQuest_f(rs.getString("quest_f"));
			o.setQuest_invio(rs.getString("quest_invio"));
			o.setQuest_ritorno(rs.getString("quest_ritorno"));
			o.setQuest_key(rs.getString("quest_key"));
			o.setEmail(rs.getString("email"));
			o.setTelefono(rs.getString("telefono"));
			o.setId_contatto(rs.getString("id_contatto"));
			o.setEsito(rs.getString("esito"));
			o.setId_contatto_vol(rs.getString("id_contatto_vol"));
			o.setDt_colloquio(rs.getString("dt_colloquio"));
			o.setId_contatto_adottante(rs.getString("id_contatto_adottante"));
			o.setId_contatto_proprietario(rs.getString("id_contatto_proprietario"));
			o.setContributo(rs.getString("contributo"));
			o.setId_pratica(rs.getString("id_pratica"));
			o.setDt_consegna(rs.getString("dt_consegna"));
			return o;
		}
	}
		
	
	/**
	 * @param 
	 * @return List<TipoRichiesta>
	 */
	@Transactional(readOnly = true)
	public List<TipoIter> getTipiIter() {
		
		String queryStr = "SELECT id_tipo_iter,  tipo_iter, colore " +
				" FROM an_x_tipo_iter " + 
				" ORDER BY tipo_iter";

		try{
			return jdbcTemplate.query(queryStr, new TipoIterRowMapper());

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	private static class TipoIterRowMapper extends BaseRowMapper<TipoIter> {
		public TipoIterRowMapper() {
		}		
		@Override
		public TipoIter mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			TipoIter o = new TipoIter();
			o.setId_tipo_iter(rs.getString("id_tipo_iter"));
			o.setTipo_iter(rs.getString("tipo_iter"));
			o.setColore(rs.getString("colore"));
			return o;
		}
	}

}
