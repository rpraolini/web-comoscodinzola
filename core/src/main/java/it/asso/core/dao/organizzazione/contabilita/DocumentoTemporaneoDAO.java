package it.asso.core.dao.organizzazione.contabilita;

import it.asso.core.common.Def;
import it.asso.core.dao.common.BaseRowMapper;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.documenti.FileDAO;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.organizzazione.contabilita.DocumentoTemporaneo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DocumentoTemporaneoDAO {
    private static final Logger logger = LoggerFactory.getLogger(DocumentoTemporaneoDAO.class);


    private final FileDAO fileDao;
	private final DocumentoDAO documentoDao;

    private final JdbcTemplate jdbcTemplate;

    public DocumentoTemporaneoDAO(JdbcTemplate jdbcTemplate, FileDAO fileDao,DocumentoDAO documentoDao) {
        this.jdbcTemplate = jdbcTemplate;

        this.fileDao = fileDao;
        this.documentoDao = documentoDao;
    }
	
	/**
	 * @param id
	 * @return DocumentoTemporaneo
	 */
	@Transactional(readOnly = true)
	public DocumentoTemporaneo getByID(String id) {
		DocumentoTemporaneo dt = null;
		String queryStr = "SELECT id_documento, nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento, account FROM an_documenti where id_documento = ?";
		try{
			dt =  jdbcTemplate.queryForObject(queryStr, new DocumentoTemporaneoRowMapper(), new Object[] { id });
			dt.setAssoFiles(fileDao.getFilesByIDDocumento(id));
			return dt;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return dt;
		}
	}
	
	/**
	 * @param year
	 * @return List<DocumentoTemporaneo>
	 */
	@Transactional(readOnly = true)
	public List<DocumentoTemporaneo> getByYear(String year) {
		List<DocumentoTemporaneo> dt = new ArrayList<DocumentoTemporaneo>();
		String queryStr = "Select"
				+ "    d.id_documento,"
				+ "    nullif(DATE_FORMAT(dt_inserimento, '%d/%m/%Y'),'') dt_inserimento,"
				+ "    d.account"
				+ " From"
				+ "    an_documenti d Left Join"
				+ "    org_r_fatture_documenti fd On fd.id_documento = d.id_documento Left Join"
				+ "    org_r_movimenti_documenti md On md.id_documento = d.id_documento"
				+ " Where"
				+ "    d.id_tipo_documento = " + Def.TIPO_DOC_TEMPORANEO + " And"
				+ "    Year(d.dt_inserimento) = ? And"
				+ "    (fd.id_documento Is Null and"
				+ "        md.id_documento Is Null) order by d.id_documento asc";

		try{
			dt =  jdbcTemplate.query(queryStr, new DocumentoTemporaneoRowMapper(), new Object[] { year });
			for (DocumentoTemporaneo docTemp : dt){
				docTemp.setAssoFiles(fileDao.getFilesByIDDocumento(docTemp.getId_documento()));
			}
			
			return dt;
		} catch (EmptyResultDataAccessException e) {
			logger.error(e.getMessage());
			return dt;
		}
	}
	
	/**
	 * @param dt
	 * @return
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	@Transactional()
	public String saveOrUpdate(DocumentoTemporaneo dt) throws SQLIntegrityConstraintViolationException {
		Documento doc = documentoDao.saveOrUpdate(dt);
		return doc.getId_documento();
	}

	
	/**
	 * @param id
	 * @return
	 * @throws IOException 
	 * @throws IllegalStateException 
	 * @throws SQLIntegrityConstraintViolationException 
	 */
	@Transactional()
	public String deleteByID(String id) throws SQLIntegrityConstraintViolationException, IllegalStateException, IOException {
		return documentoDao.deleteByID(id);
	}
	
	
	
	private static class DocumentoTemporaneoRowMapper extends BaseRowMapper<DocumentoTemporaneo> {
		public DocumentoTemporaneoRowMapper() {
		}		
		@Override
		public DocumentoTemporaneo mapRowImpl(ResultSet rs, int i) throws SQLException {
		
			DocumentoTemporaneo o = new DocumentoTemporaneo();
			o.setId_documento(rs.getString("id_documento"));
			o.setDt_inserimento(rs.getString("dt_inserimento"));
			o.setAccount(rs.getString("account"));
			return o;
		}
	}

}
