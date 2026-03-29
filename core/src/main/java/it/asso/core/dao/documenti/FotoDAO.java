package it.asso.core.dao.documenti;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.documenti.Foto;
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
public class FotoDAO {

	private static Logger logger = LoggerFactory.getLogger(FotoDAO.class);

    private final JdbcTemplate jdbcTemplate;

    public FotoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	
	
	/**
	 * @param idAnimale
	 * @return String
	 */
	@Transactional(readOnly = true)
	public int getCountByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT count(id_animale) " + 
				" FROM an_foto " + 
				" WHERE id_animale = ? ";

		try{
			return jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return 0;
		}
	}
	
	/**
	 * @param 
	 * @return List<Foto>
	 */
	@Transactional(readOnly = true)
	public List<Foto> getFotoByIdAnimale(String idAnimale) {
		
		
		String queryStr = "SELECT id_animale, " + 
				"    id_foto, " + 
				"    nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
				"    account, " + 
				"    estensione, " + 
				"    dimensione, " + 
				"    nome_file, nome_file_t, " + 
				"    percorso, " + 
				"    didascalia, " +
				"    pubblica, " +
				"    id_tipo_foto " +
				" FROM an_foto " + 
				" WHERE id_animale = ? ";

		try{
			return jdbcTemplate.query(queryStr, new FotoRowMapper(), new Object[] { idAnimale });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idFoto
	 * @return Foto
	 */
	@Transactional(readOnly = true)
	public Foto getFotoById(String idFoto) {
		
		
		String queryStr = "SELECT id_animale, " + 
				"    id_foto, " + 
				"    nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
				"    account, " + 
				"    estensione, " + 
				"    dimensione, " + 
				"    nome_file, nome_file_t, " + 
				"    percorso, " + 
				"    didascalia, " +
				"    pubblica, " +
				"    id_tipo_foto " +
				" FROM an_foto " + 
				" WHERE id_foto = ? ";

		try{
			return jdbcTemplate.queryForObject(queryStr, new FotoRowMapper(), new Object[] { idFoto });

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idAnimale
	 * @return Foto
	 */
	@Transactional(readOnly = true)
	public Foto getFotoProfiloById(String idAnimale) {
		try{

			String queryStr = "SELECT id_animale, " + 
					"    id_foto, " + 
					"    NullIf(Date_Format(an_foto.dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
					"    account, " + 
					"    estensione, " + 
					"    dimensione, " + 
					"    nome_file, nome_file_t, " + 
					"    percorso, " + 
					"    didascalia, " +
					"    pubblica, " +
					"    id_tipo_foto " +
					" FROM an_foto " + 
					" WHERE id_animale = ? and id_tipo_foto = " + Def.FOTO_PROFILO ;
			List<Foto> foto = jdbcTemplate.query(queryStr, new FotoRowMapper(), new Object[] { idAnimale });
			
			if(!foto.isEmpty()) {
				return foto.get(0);
			}else {
				return null;
			}

		} catch (EmptyResultDataAccessException e) {
			logger.error("Foto profilo inesistente per idAnimale : " + idAnimale + " - " +e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idAnimale
	 * @return Foto
	 */
	@Transactional(readOnly = true)
	public Foto getFotoPostAdozione(String idAnimale) {
		try{
			
			String queryStr = "SELECT count(id_foto) FROM an_foto WHERE id_animale = ? and id_tipo_foto = " + Def.FOTO_POST_ADOZIONE;
			int check = jdbcTemplate.queryForObject(queryStr, Integer.class, new Object[] { idAnimale });
			
			if(check > 0) {
				queryStr = "SELECT id_animale, " + 
						"    id_foto, " + 
						"    nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, " + 
						"    account, " + 
						"    estensione, " + 
						"    dimensione, " + 
						"    nome_file, nome_file_t, " + 
						"    percorso, " + 
						"    didascalia, " +
						"    pubblica, " +
						"    id_tipo_foto " +
						" FROM an_foto " + 
						" WHERE id_animale = ? and id_tipo_foto = " + Def.FOTO_POST_ADOZIONE ;
				
				
				
				return jdbcTemplate.queryForObject(queryStr, new FotoRowMapper(), new Object[] { idAnimale });
			}else {
				return null;
			}
			

		} catch (EmptyResultDataAccessException e) {
			logger.error("Foto profilo inesistente per idAnimale : " + idAnimale + " - " +e.getMessage());
			return null;
		}
	}

	@Transactional(readOnly = true)
	public int getNextProgressivo(String idAnimale) {
		String query = "SELECT nome_file FROM an_foto WHERE id_animale = ? ORDER BY id_foto DESC LIMIT 1";
		try {
			String nomeFile = jdbcTemplate.queryForObject(query, String.class, idAnimale);
			if (nomeFile == null) return 1;

			// nomeFile es: "1406_3.jpg" oppure "1406_3_T.jpg"
			// Rimuoviamo estensione
			String senzaExt = nomeFile.contains(".")
					? nomeFile.substring(0, nomeFile.lastIndexOf("."))
					: nomeFile;

			// Rimuoviamo eventuale suffisso _T
			if (senzaExt.endsWith("_T")) {
				senzaExt = senzaExt.substring(0, senzaExt.length() - 2);
			}

			// Estraiamo la parte dopo l'ultimo underscore -> progressivo
			String[] parti = senzaExt.split("_");
			int progressivo = Integer.parseInt(parti[parti.length - 1]);
			return progressivo + 1;

		} catch (EmptyResultDataAccessException e) {
			return 1; // Nessuna foto ancora
		} catch (NumberFormatException e) {
			logger.warn("Impossibile determinare progressivo da nome file per animale {}, uso timestamp", idAnimale);
			return (int)(System.currentTimeMillis() % 100000); // fallback sicuro
		}
	}
	
	/**
	 * @param foto
	 * @return
	 */
	
	@Transactional()
	public String saveOrUpdate(Foto foto) {
		String idFoto = "";
		
		if(Def.FOTO_PROFILO.equals(foto.getId_tipo_foto())) {
			foto.setPubblica(Def.FOTO_PUBBLICA);
		}
		
		if (foto.getId_foto() == null) {
			idFoto = save(foto);
			if (Def.FOTO_PROFILO.equals(foto.getId_tipo_foto())) {
				foto.setId_foto(idFoto);
				updateImmagineProfilo(foto);
			} else if (Def.FOTO_POST_ADOZIONE.equals(foto.getId_tipo_foto())) {
				foto.setId_foto(idFoto);
				updateImmaginePostAdozione(foto);
			}
		} else {
			idFoto = update(foto);
		}
		return idFoto;
	}

	/**
	 * @param foto
	 * @return
	 */
	
	@Transactional()
	public String save(Foto foto) {

		final String query = "INSERT INTO an_foto " + 
										"(nome_file, nome_file_t," + 
										"dimensione," + 
										"estensione," + 
										"percorso," + 
										"id_animale, " +
										"account," + 
										"didascalia," +
										"pubblica," +
										"id_tipo_foto)" +
										" VALUES " + 
										"(:nome_file, :nome_file_t," + 
										":dimensione," + 
										":estensione," + 
										":percorso," + 
										":id_animale," +
										":account, " +
										":didascalia, " +
										":pubblica, " +
										":id_tipo_foto " +
										")";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(foto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_foto" });
		
		return String.valueOf(keyHolder.getKey());
	}

	/**
	 * @param foto
	 * @return
	 */
	
	@Transactional()
	public String update(Foto foto) {

		final String query = "UPDATE an_foto " + 
				" SET " + 
				"nome_file = :nome_file," + 
				"nome_file_t = :nome_file_t," + 
				"dimensione = :dimensione," + 
				"estensione = :estensione," + 
				"percorso = :percorso," + 
				"id_animale = :id_animale," +
				"account = :account, " +
				"didascalia = :didascalia, " +
				"pubblica = :pubblica, " +
				"id_tipo_foto = :id_tipo_foto " +
				" WHERE id_foto = :id_foto";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(foto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);
		
		if(Def.FOTO_PROFILO.equals(foto.getId_tipo_foto())) {
			updateImmagineProfilo(foto);
		}else if(Def.FOTO_POST_ADOZIONE.equals(foto.getId_tipo_foto())) {
			updateImmaginePostAdozione(foto);
		}
		
		return foto.getId_foto();
	}
	
	/**
	 * @param foto
	 * @return
	 */
	
	@Transactional()
	private void updateImmagineProfilo(Foto foto) {

		final String query = "UPDATE an_foto " +
				" SET " +
				"id_tipo_foto = 0 " +
				" WHERE id_animale = :id_animale and id_foto not in (:id_foto) and id_tipo_foto = 1";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(foto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

	}
	
	/**
	 * @param foto
	 * @return
	 */
	
	@Transactional()
	private void updateImmaginePostAdozione(Foto foto) {

		final String query = "UPDATE an_foto " + 
				" SET " + 
				"id_tipo_foto = 0 " +
				" WHERE id_animale = :id_animale and id_foto not in (:id_foto) and id_tipo_foto != 1";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(foto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

	}
	
	
	
	/**
	 * @param foto
	 * @return
	 */
	
	@Transactional()
	public String delete(Foto foto) {

		final String query = "DELETE FROM an_foto WHERE id_foto = :id_foto";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(foto);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return foto.getId_foto();
	}
	
	/**
	 * @param id
	 * @return
	 */
	
	@Transactional()
	public String delete(String id) {

		Foto foto = new Foto();
		foto.setId_foto(id);
		
		delete(foto);
		
		return  Def.STR_OK;
	}
	
	private static class FotoRowMapper extends BaseRowMapper<Foto> {
		public FotoRowMapper() {
		}		
		/*
		 * (non-Javadoc)
		 *
		 * @see
		 * it.asso.webapps.repository.dao.BaseRowMapper#mapRowImpl(java.sql.
		 * ResultSet, int)
		 */
		@Override
		public Foto mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Foto o = new Foto();
			o.setId_animale(rs.getString("id_animale"));
			o.setAccount(rs.getString("account"));
			o.setDimensione(rs.getString("dimensione"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setEstensione(rs.getString("estensione"));
			o.setId_foto(rs.getString("id_foto"));
			o.setNome_file(rs.getString("nome_file"));
			o.setNome_file_t(rs.getString("nome_file_t"));
			o.setPercorso(rs.getString("percorso"));
			o.setDidascalia(rs.getString("didascalia"));
			o.setPubblica(rs.getString("pubblica"));
			o.setId_tipo_foto(rs.getString("id_tipo_foto"));
			return o;
		}
	}
	
	
}
