package it.asso.core.dao.documenti;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.model.documenti.AssoFile;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.documenti.TipoDocumento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Repository
public class DocumentoDAO {

	private static final Logger logger = LoggerFactory.getLogger(DocumentoDAO.class);
	
	private final TipoDocumentoDAO tipoDocumentoDao;
    private final FileDAO fileDao;

    private final JdbcTemplate jdbcTemplate;

    public DocumentoDAO(JdbcTemplate jdbcTemplate, FileDAO fileDao, TipoDocumentoDAO tipoDocumentoDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.fileDao = fileDao;
        this.tipoDocumentoDao = tipoDocumentoDao;
    }

	/**************************************** NUOVI METODI  ****************************************/
	public List<Documento> getDocumentiByAnimale(String idAnimale) {
		// La tua query corretta con i join sulle tabelle asso
		String sql = "SELECT " +
				"    d.id_documento, d.id_tipo_documento, d.num_documento, d.dt_inserimento, d.note, " +
				"    td.documento as descr_tipo_doc, " +
				"    f.id_file, f.filename, f.extension, f.size, f.full_path " +
				"FROM an_r_animali_documenti a " +
				"LEFT JOIN an_documenti d ON (a.id_documento = d.id_documento) " +
				"JOIN an_x_tipo_documento td ON d.id_tipo_documento = td.id_tipo_documento " +
				"LEFT JOIN an_files f ON d.id_documento = f.id_documento " +
				"WHERE a.id_animale = ? " +
				"ORDER BY d.dt_inserimento DESC";

		return jdbcTemplate.query(sql, (ResultSet rs) -> {
			// Mappa per raggruppare i file (AssoFile) sotto il rispettivo Documento
			Map<String, Documento> map = new LinkedHashMap<>();

			while (rs.next()) {
				String idDoc = rs.getString("id_documento");

				// Se il documento non è ancora nella mappa, lo creiamo
				Documento doc = map.get(idDoc);
				if (doc == null) {
					doc = new Documento();
					doc.setId_documento(idDoc);
					doc.setId_tipo_documento(rs.getString("id_tipo_documento"));
					doc.setNum_documento(rs.getString("num_documento"));
					doc.setDt_inserimento(rs.getString("dt_inserimento"));
					doc.setNote(rs.getString("note"));

					// Mappatura TipoDocumento (tabella an_x_tipo_documento)
					TipoDocumento td = new TipoDocumento();
					td.setId_tipo_documento(rs.getString("id_tipo_documento"));
					td.setDocumento(rs.getString("descr_tipo_doc"));
					doc.setTipoDocumento(td);

					doc.setAssoFiles(new ArrayList<>());
					map.put(idDoc, doc);
				}

				// Aggiungiamo il file (dalla tabella an_files) se presente
				String idFile = rs.getString("id_file");
				if (idFile != null) {
					AssoFile af = new AssoFile();
					af.setId_file(idFile);
					af.setFilename(rs.getString("filename"));
					af.setExtension(rs.getString("extension"));
					af.setSize(rs.getString("size"));
					af.setFull_path(rs.getString("full_path"));
					af.setId_documento(idDoc);
					doc.getAssoFiles().add(af);
				}
			}
			return new ArrayList<>(map.values());
		}, idAnimale);
	}

	public void insertDocumento(String idAnimale, Documento doc) {
		// 1. Inserimento nella tabella principale an_documenti
		String sqlDoc = "INSERT INTO an_documenti (id_tipo_documento, num_documento, dt_inserimento, account, note) " +
				"VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sqlDoc, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, doc.getId_tipo_documento());
			ps.setString(2, doc.getNum_documento());
			ps.setString(3, doc.getAccount());
			ps.setString(4, doc.getNote());
			return ps;
		}, keyHolder);

		// Recuperiamo l'ID generato dal database
		String idDocumento = keyHolder.getKey().toString();
		doc.setId_documento(idDocumento);

		// 2. Inserimento nella tabella di relazione an_r_animali_documenti
		String sqlRel = "INSERT INTO an_r_animali_documenti (id_animale, id_documento) VALUES (?, ?)";
		jdbcTemplate.update(sqlRel, idAnimale, idDocumento);

		// 3. Inserimento dei file associati nella tabella an_files
		if (doc.getAssoFiles() != null && !doc.getAssoFiles().isEmpty()) {
			String sqlFile = "INSERT INTO an_files (id_documento, filename, extension, size, full_path) VALUES (?, ?, ?, ?, ?)";

			for (AssoFile f : doc.getAssoFiles()) {
				jdbcTemplate.update(sqlFile,
						idDocumento,
						f.getFilename(),
						f.getExtension(),
						f.getSize(),
						f.getFull_path()
				);
			}
		}
	}

	public void deleteDocumento(String idDocumento) {
		// 1. Rimuoviamo il legame con l'animale
		String sqlRel = "DELETE FROM an_r_animali_documenti WHERE id_documento = ?";
		jdbcTemplate.update(sqlRel, idDocumento);

		// 2. Rimuoviamo i file associati
		String sqlFiles = "DELETE FROM an_files WHERE id_documento = ?";
		jdbcTemplate.update(sqlFiles, idDocumento);

		// 3. Rimuoviamo il documento principale
		String sqlDoc = "DELETE FROM an_documenti WHERE id_documento = ?";
		jdbcTemplate.update(sqlDoc, idDocumento);
	}

	public List<AssoFile> getFilesByDocumento(String idDocumento) {
		String sql = "SELECT full_path FROM an_files WHERE id_documento = ?";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(AssoFile.class), idDocumento);
	}

	public AssoFile getFileById(String idFile) {
		// Query mirata sulla tabella dei file che abbiamo visto prima
		String sql = "SELECT id_file, id_documento, filename, extension, size, full_path " +
				"FROM an_files WHERE id_file = ?";

		try {
			return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(AssoFile.class), idFile);
		} catch (EmptyResultDataAccessException e) {
			return null; // Gestiamo il caso in cui l'ID non esista
		}
	}


	/**************************************** DOCUMENTI ****************************************/
	
	/**
	 * @param id_documento
	 * @return Documento
	 */
	@Transactional(readOnly = true)
	public Documento getDocumentoByID(String id_documento) {
		
		String queryStr = "SELECT id_documento, id_tipo_documento, num_documento, account, NullIf(Date_Format(dt_inserimento, '%d/%m/%Y %H:%i:%s'),'') dt_inserimento, note "
				+ " FROM  an_documenti "
				+ " WHERE id_documento = ?";

		try{
			Documento doc = jdbcTemplate.queryForObject(queryStr, new DocumentoRowMapper(), new Object[] { id_documento });
			doc.setAssoFiles(fileDao.getFilesByIDDocumento(id_documento));
			doc.setTipoDocumento(tipoDocumentoDao.getTipoDocumentoByID(doc.getId_tipo_documento()));
			
			return doc;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param documento
	 * @return Documento
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	@Transactional()
	public Documento saveOrUpdate(Documento documento) throws SQLIntegrityConstraintViolationException {
		String result = null;
		
		if (documento.getId_documento() == null) {
			result = save(documento);
		}else {
			result = update(documento);
		}
		
		return getDocumentoByID(result);
	}
	
	/**
	 * @param documento
	 * @return String
	 */
	@Transactional()
	public String save(Documento documento) {

		final String query = "INSERT INTO an_documenti  (id_tipo_documento, num_documento, account, note)  VALUES  (:id_tipo_documento, upper(:num_documento), :account, :note)";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(documento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_documento" });

		return String.valueOf(keyHolder.getKey());
	}
	
	/**
	 * @param documento
	 * @return String
	 */

	@Transactional()
	public String update(Documento documento) {

		final String query = "UPDATE an_documenti  SET id_tipo_documento = :id_tipo_documento, num_documento = upper(:num_documento), account = :account, note = :note  WHERE  id_documento = :id_documento ";

		SqlParameterSource parameters = new BeanPropertySqlParameterSource(documento);
        assert jdbcTemplate.getDataSource() != null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
		namedParameterJdbcTemplate.update(query, parameters);

		return documento.getId_documento();
	}

	/**
	 * @param idDocumento
	 * @return String
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */

	@Transactional()
	public String deleteByID(String idDocumento) throws SQLIntegrityConstraintViolationException, IllegalStateException, IOException {
		fileDao.deleteByIDDocumento(idDocumento);
		String query = "DELETE FROM org_protocollo WHERE id_documento = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento });
		query = "DELETE FROM an_documenti WHERE id_documento = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento });
		return Def.STR_OK;
	}
	
	/**************************************** FINE DOCUMENTI *****************************************/
	
	/**************************************** DOCUMENTI EVENTO CLINICO *******************************/
	
	/**
	 * @param id_evento
	 * @return List<Documento>
	 */
	@Transactional(readOnly = true)
	public ArrayList<Documento> getDocumentiByIDEvento(String id_evento) {

        ArrayList<Documento> documenti = new ArrayList<Documento>();
		String queryStr = "SELECT id_documento, id_evento"
				+ " FROM  an_r_ec_documenti "
				+ " WHERE id_evento = ?";

		try{
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr, new Object[] { id_evento });

			for (Map<String,Object> m : results){
			   documenti.add(getDocumentoByID(m.get("id_documento").toString()));
			} 
			
			return documenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idDocumento, idEvento
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String saveDocumentoEventoClinico(String idDocumento, String idEvento) throws SQLIntegrityConstraintViolationException {
		
		final String query = "INSERT INTO an_r_ec_documenti  (id_documento, id_evento)  VALUES  (?, ?)";
		jdbcTemplate.update(query, new Object[] { idDocumento, idEvento });
		
		return Def.STR_OK;
	}
	
	/**
	 * @param idDocumento, idEvento
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String deleteDocumentoEventoClinico(String idDocumento, String idEvento) throws SQLIntegrityConstraintViolationException {
		
		final String query = "DELETE FROM an_r_ec_documenti  WHERE id_documento= ? and  id_evento = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento, idEvento });
		
		return Def.STR_OK;
	}
	
	/**************************************** FINE DOCUMENTI EVENTO CLINICO ****************************/
	
	/**************************************** DOCUMENTI ANIMALE ****************************************/

	/**
	 * @param id_animale
	 * @return List<Documento>
	 */
	@Transactional(readOnly = true)
	public List<Documento> getDocumentiAnimaleByIDAnimale(String id_animale) {
		
		List<Documento> documenti = new ArrayList<Documento>();
		String queryStr = "SELECT id_documento, id_animale"
				+ " FROM  an_r_animali_documenti "
				+ " WHERE id_animale = ?";

		try{
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr, new Object[] { id_animale });

			for (Map<String,Object> m : results){
			   documenti.add(getDocumentoByID(m.get("id_documento").toString()));
			} 
			
			return documenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idDocumento, idAnimale
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String saveDocumentoAnimale(String idDocumento, String idAnimale) throws SQLIntegrityConstraintViolationException {
		
		final String query = "INSERT INTO an_r_animali_documenti  (id_documento, id_animale)  VALUES  (?, ?)";
		jdbcTemplate.update(query, new Object[] { idDocumento, idAnimale });
		
		return Def.STR_OK;
	}
	
	/**
	 * @param idDocumento, idAnimale
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String deleteDocumentiAnimale(String idDocumento, String idAnimale) throws SQLIntegrityConstraintViolationException {
		
		final String query = "DELETE FROM an_r_animali_documenti  WHERE id_documento= ? and  id_animale = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento, idAnimale });
		
		return Def.STR_OK;
	}
	
	
	/**************************************** FINE DOCUMENTI ANIMALE ********************************/
	
	/**************************************** DOCUMENTI ITER ****************************************/
	
	/**
	 * @param id_iter
	 * @return List<Documento>
	 */
	@Transactional(readOnly = true)
	public List<Documento> getDocumentiByIDIter(String id_iter) {
		
		List<Documento> documenti = new ArrayList<Documento>();
		String queryStr = "SELECT id_documento, id_iter"
				+ " FROM  an_r_iter_documenti "
				+ " WHERE id_iter = ?";

		try{
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr, new Object[] { id_iter });

			for (Map<String,Object> m : results){
			   documenti.add(getDocumentoByID(m.get("id_documento").toString()));
			} 
			
			return documenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idDocumento, idIter
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String saveDocumentoIter(String idDocumento, String idIter) throws SQLIntegrityConstraintViolationException {
		
		final String query = "INSERT INTO an_r_iter_documenti  (id_documento, id_iter)  VALUES  (?, ?)";
		jdbcTemplate.update(query, new Object[] { idDocumento, idIter });
		
		return Def.STR_OK;
	}
	
	/**
	 * @param idDocumento, idIter
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String deleteDocumentoIter(String idDocumento, String idIter) throws SQLIntegrityConstraintViolationException {
		
		final String query = "DELETE FROM an_r_iter_documenti  WHERE id_documento= ? and  id_iter = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento, idIter });
		
		return Def.STR_OK;
	}
	
	
	/**************************************** FINE DOCUMENTI ITER ****************************************/
	
	/**************************************** DOCUMENTI CONTATTO *****************************************/
	
	/**
	 * @param id_contatto
	 * @return List<Documento>
	 */
	@Transactional(readOnly = true)
	public List<Documento> getDocumentiByIDContatto(String id_contatto) {
		
		List<Documento> documenti = new ArrayList<Documento>();
		String queryStr = "SELECT id_documento, id_contatto"
				+ " FROM  an_r_contatti_documenti "
				+ " WHERE id_contatto = ?";

		try{
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr, new Object[] { id_contatto });

			for (Map<String,Object> m : results){
			   documenti.add(getDocumentoByID(m.get("id_documento").toString()));
			} 
			
			return documenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idDocumento, idContatto
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String saveDocumentoContatto(String idDocumento, String idContatto) throws SQLIntegrityConstraintViolationException {
		
		final String query = "INSERT INTO an_r_contatti_documenti  (id_documento, id_contatto)  VALUES  (?, ?)";
		jdbcTemplate.update(query, new Object[] { idDocumento, idContatto });
		
		return Def.STR_OK;
	}
	
	/**
	 * @param idDocumento, idContatto
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String deleteDocumentoContatto(String idDocumento, String idContatto) throws SQLIntegrityConstraintViolationException {
		
		final String query = "DELETE FROM an_r_contatti_documenti  WHERE id_documento= ? and  id_contatto = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento, idContatto });
		
		return Def.STR_OK;
	}
	
	/**************************************** FINE DOCUMENTI CONTATTO ***********************************/
	
	/**************************************** DOCUMENTI FATTURA *****************************************/
	
	/**
	 * @param id_fattura
	 * @return ArrayList<Documento>
	 */
	@Transactional(readOnly = true)
	public ArrayList<Documento> getDocumentiByIDFattura(String id_fattura) {

        ArrayList<Documento> documenti = new ArrayList<Documento>();
		String queryStr = "SELECT id_documento, id_fattura"
				+ " FROM  org_r_fatture_documenti "
				+ " WHERE id_fattura = ?";

		try{
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr, new Object[] { id_fattura });

			for (Map<String,Object> m : results){
			   documenti.add(getDocumentoByID(m.get("id_documento").toString()));
			} 
			
			return documenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	
	/**
	 * @param idDocumento, idFattura
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String saveDocumentoFattura(String idDocumento, String idFattura) throws SQLIntegrityConstraintViolationException {
		
		final String query = "INSERT INTO org_r_fatture_documenti  (id_documento, id_fattura)  VALUES  (?, ?)";
		jdbcTemplate.update(query, new Object[] { idDocumento, idFattura });
		
		return Def.STR_OK;
	}
	
	
	/**
	 * @param idDocumento, idFattura
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String deleteDocumentoFattura(String idDocumento, String idFattura) throws SQLIntegrityConstraintViolationException {
		
		final String query = "DELETE FROM org_r_fatture_documenti  WHERE id_documento= ? and  id_fattura = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento, idFattura });
		
		return Def.STR_OK;
	}
	
	
	/**************************************** FINE DOCUMENTI FATTURA **************************************/
	
	
	/**************************************** DOCUMENTI MOVIMENTO *****************************************/
	/**
	 * @param id_movimento
	 * @return List<Documento>
	 */
	@Transactional(readOnly = true)
	public ArrayList<Documento> getDocumentiByIDMovimento(String id_movimento) {

        ArrayList<Documento> documenti = new ArrayList<Documento>();
		String queryStr = "SELECT id_documento, id_movimento"
				+ " FROM  org_r_movimenti_documenti "
				+ " WHERE id_movimento = ?";

		try{
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr, new Object[] { id_movimento });

			for (Map<String,Object> m : results){
			   documenti.add(getDocumentoByID(m.get("id_documento").toString()));
			} 
			
			return documenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idDocumento, idMovimento
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String saveDocumentoMovimento(String idDocumento, String idMovimento) throws SQLIntegrityConstraintViolationException {
		
		final String query = "INSERT INTO org_r_movimenti_documenti  (id_documento, id_movimento)  VALUES  (?, ?)";
		jdbcTemplate.update(query, new Object[] { idDocumento, idMovimento });
		
		return Def.STR_OK;
	}
	
	/**
	 * @param idDocumento, idMovimento
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */

	@Transactional()
	public String deleteDocumentoMovimento(String idDocumento, String idMovimento) throws SQLIntegrityConstraintViolationException {
		
		final String query = "DELETE FROM org_r_movimenti_documenti  WHERE id_documento= ? and  id_movimento = ?";
		jdbcTemplate.update(query, new Object[] { idDocumento, idMovimento });
		
		return Def.STR_OK;
	}
	
	/**************************************** FINE DOCUMENTI MOVIMENTO **************************************/
	
	
	/**************************************** DOCUMENTI ASSOCIAZIONE *****************************************/
	
	/**
	 * @param 
	 * @return List<Documento>
	 */
	@Transactional(readOnly = true)
	public List<Documento> getDocumentiAssociazione(String ambito) {
		
		List<Documento> documenti = new ArrayList<Documento>();
		String queryStr = "Select "
				+ "    od.id_documento, "
				+ "    od.id_organizzazione "
				+ "From "
				+ "    org_r_documenti od Left Join "
				+ "    an_documenti d On od.id_documento = d.id_documento Left Join "
				+ "    an_x_tipo_documento td On td.id_tipo_documento = d.id_tipo_documento "
				+ "Where "
				+ "    td.ambito = '" + ambito + "'";

		try{
			
			List<Map<String,Object>> results = jdbcTemplate.queryForList(queryStr);

			for (Map<String,Object> m : results){
			   documenti.add(getDocumentoByID(m.get("id_documento").toString()));
			} 
			
			return documenti;

		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	/**
	 * @param idDocumento, idAssociazione
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	@Transactional()
	public String saveDocumentoAssociazione(String idDocumento, String idAssociazione) throws SQLIntegrityConstraintViolationException {
		
		final String query = "INSERT INTO org_r_documenti  (id_documento, id_organizzazione)  VALUES  (?, ?)";
		jdbcTemplate.update(query, new Object[] { idDocumento, idAssociazione });
		
		return Def.STR_OK;
	}
	
	/**
	 * @param idDocumento
	 * @return String
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	@Transactional()
	public String deleteDocumentoAssociazione(String idDocumento) throws SQLIntegrityConstraintViolationException {
		
		final String query = "DELETE FROM org_r_documenti  WHERE id_documento= ? ";
		jdbcTemplate.update(query, new Object[] { idDocumento });
		
		return Def.STR_OK;
	}
	
	/**************************************** FINE DOCUMENTI ASSOCIAZIONE **************************/
		
	private static class DocumentoRowMapper extends BaseRowMapper<Documento> {
		public DocumentoRowMapper() {}		
		@Override
		public Documento mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			Documento o = new Documento();
			o.setId_tipo_documento(rs.getString("id_tipo_documento"));
			o.setId_documento(rs.getString("id_documento"));
			o.setNum_documento(rs.getString("num_documento"));
			o.setAccount(rs.getString("account"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setNote(rs.getString("note"));
			return o;
		}
	
	}


}
