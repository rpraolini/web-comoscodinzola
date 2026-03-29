package it.asso.core.dao.documenti;

import it.asso.core.common.Def;
import it.asso.core.model.documenti.AssoFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Repository
public class FileDAO {

    private static final Logger logger = LoggerFactory.getLogger(FileDAO.class);

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final String pathDoc;

    // COSTRUTTORE CORRETTO PER L'INIEZIONE DI TEMPLATE E @Value
    public FileDAO (JdbcTemplate jdbcTemplate, @Value("${path_doc}") String pathDoc){
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.pathDoc = pathDoc;
    }

    // Metodo di utilità per il JdbcTemplate (se necessario, usa direttamente il campo)
    // private JdbcTemplate getJdbcTemplate() { return this.jdbcTemplate; }
    // private NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() { return this.namedParameterJdbcTemplate; }


    @Transactional(rollbackFor = IOException.class)
    public void storeFile(MultipartFile mf, String nameDirectory, String fileName) throws IllegalStateException, IOException {

        String saveDirectory = pathDoc;

        // Sostituisci la concatenazione di stringhe con Paths.get()
        Path directory = Paths.get(saveDirectory, nameDirectory);

        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory); // Corretto per creare directory annidate
            } catch (IOException e) {
                logger.error("Errore nella creazione della directory : {} - {}", nameDirectory, e.getMessage());
                throw e; // Rilanciare l'eccezione I/O
            }
        }

        File f = new File(directory.toFile(), fileName); // Usa File(directory, filename)

        // mf.transferTo(f); è una chiamata non transazionale, gestita da Spring MVC
        // Deve essere nel service o nel controller
        mf.transferTo(f);
    }

    public void deleteFile(String nameDirectory, String fileName) throws IllegalStateException, IOException {
        String saveDirectory = pathDoc;
        Path path = Paths.get(saveDirectory, nameDirectory, fileName);
        Files.deleteIfExists(path);
    }

    /**
     * @param id_file
     * @return AssoFile
     */
    @Transactional(readOnly = true)
    public AssoFile getFileByID(String id_file) {


        final String queryStr = "SELECT id_file, filename, extension, size, id_documento, full_path FROM an_files WHERE id_file = ?";

        try{
            return jdbcTemplate.queryForObject(queryStr, new FileRowMapper(), id_file);

        } catch (EmptyResultDataAccessException e) {
            logger.error("Nessun file trovato con id : {} - {}", id_file, e.getMessage());
            return null;
        }
    }

    /**
     * @param id_documento
     * @return AssoFile
     */
    @Transactional(readOnly = true)
    public List<AssoFile> getFilesByIDDocumento(String id_documento) {


        final String queryStr = "SELECT id_file, filename, extension, size, id_documento, full_path FROM an_files WHERE id_documento = ?";

        try{
            return jdbcTemplate.query(queryStr, new FileRowMapper(), id_documento);

        } catch (EmptyResultDataAccessException e) {
            logger.error("Nessun file trovato per il documento con id : {} - {}", id_documento, e.getMessage());
            return null;
        }
    }


    /**
     * @param file
     * @return AssoFile
     */

    @Transactional(rollbackFor = SQLIntegrityConstraintViolationException.class)
    public AssoFile saveOrUpdate(AssoFile file) throws SQLIntegrityConstraintViolationException {
        String idFile;

        if (file.getId_file() == null) {
            idFile = save(file);
        }else {
            idFile = update(file);
        }

        return getFileByID(idFile);
    }

    /**
     * @param file
     * @return idFile
     */

    @Transactional()
    public String save(AssoFile file) {

        final String query = "INSERT INTO an_files (id_documento, filename, extension, size, full_path) VALUES (:id_documento, :filename, :extension, :size, :full_path)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        SqlParameterSource parameters = new BeanPropertySqlParameterSource(file);
        namedParameterJdbcTemplate.update(query, parameters, keyHolder, new String[] { "id_file" });

        return String.valueOf(keyHolder.getKey());
    }

    /**
     * @param file
     * @return idFile
     */

    @Transactional()
    public String update(AssoFile file) {

        final String query = "UPDATE an_files SET id_documento = :id_documento, "
                + "    filename = :filename, "
                + " extension = :extension, "
                + " size = :size, full_path = :full_path  "
                + " WHERE  id_file = :id_file ";


        SqlParameterSource parameters = new BeanPropertySqlParameterSource(file);
        namedParameterJdbcTemplate.update(query, parameters);

        return file.getId_file();
    }


    /**
     * @param idFile
     * @return
     */

    @Transactional(rollbackFor = {SQLIntegrityConstraintViolationException.class, IOException.class})
    public String deleteByID(String idFile) throws SQLIntegrityConstraintViolationException, IllegalStateException, IOException {
        AssoFile assoFile = getFileByID(idFile);
        final String query = "DELETE FROM an_files WHERE id_file = ?";
        jdbcTemplate.update(query, idFile);

        if (assoFile != null) {
            deleteFile(assoFile.getFull_path(), assoFile.getFilename());
        }
        return Def.STR_OK;
    }

    /**
     * @param idDocumento
     * @return
     */

    @Transactional(rollbackFor = {SQLIntegrityConstraintViolationException.class, IOException.class})
    public String deleteByIDDocumento(String idDocumento) throws SQLIntegrityConstraintViolationException, IllegalStateException, IOException {

        List<AssoFile> files = getFilesByIDDocumento(idDocumento);
        deleteFiles(files);

        return Def.STR_OK;
    }

    /**
     * @param files
     * @return
     */

    @Transactional(rollbackFor = {SQLIntegrityConstraintViolationException.class, IOException.class})
    public String deleteFiles(List<AssoFile> files) throws SQLIntegrityConstraintViolationException, IllegalStateException, IOException {

        if(files != null && !files.isEmpty()) {
            for (AssoFile assoFile : files) {
                final String query = "DELETE FROM an_files WHERE id_file = ?";
                jdbcTemplate.update(query, assoFile.getId_file());
                deleteFile(assoFile.getFull_path(), assoFile.getFilename());
            }
        }

        return Def.STR_OK;
    }

    /**
     * @param assoFile
     * @return
     */

    @Transactional(rollbackFor = {SQLIntegrityConstraintViolationException.class, IOException.class})
    public String deleteFile(AssoFile assoFile) throws SQLIntegrityConstraintViolationException, IllegalStateException, IOException {

        final String query = "DELETE FROM an_files WHERE id_file = ?";
        jdbcTemplate.update(query, assoFile.getId_file());
        deleteFile(assoFile.getFull_path(), assoFile.getFilename());

        return Def.STR_OK;
    }

    /**
     * @param idDocumento
     * @param files
     * @return
     */

    @Transactional(readOnly = true)
    public String checkFilesByIDDocumento(String idDocumento, List<AssoFile> files) throws IllegalStateException, IOException {

        List<AssoFile> mfiles = getFilesByIDDocumento(idDocumento);

        for (AssoFile af : mfiles) {
            boolean idExists = files.stream()
                    .map(AssoFile::getId_file)
                    .anyMatch(af.getId_file()::equals);

            if(!idExists) {
                try {
                    // Non eseguire la DELETE del DB qui perché la transazione è READONLY!
                    // In un vero scenario, questa logica andrebbe spostata nel Service Layer.
                    deleteFile(af.getFull_path(), af.getFilename());
                } catch (Exception e) {
                    logger.warn("Errore durante l'eliminazione del file fisico non mappato: " + af.getFilename(), e);
                }
            }
        }

        return Def.STR_OK;
    }

    private static class FileRowMapper implements RowMapper<AssoFile> {
        @Override
        public AssoFile mapRow(ResultSet rs, int i) throws SQLException {

            AssoFile o = new AssoFile();
            o.setId_file(rs.getString("id_file"));
            o.setFilename(rs.getString("filename"));
            o.setExtension(rs.getString("extension"));
            o.setSize(rs.getString("size"));
            o.setId_documento(rs.getString("id_documento"));
            o.setFull_path(rs.getString("full_path"));
            return o;
        }
    }


}