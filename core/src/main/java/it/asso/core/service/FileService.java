package it.asso.core.service;

import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.documenti.FileDAO;
import it.asso.core.model.documenti.AssoFile;
import it.asso.core.model.documenti.Documento;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLIntegrityConstraintViolationException;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${file.upload.base-path}")
    private String basePath;

    private final FileDAO fileDao;
    private final DocumentoDAO documentoDao;

    @Autowired
    private HttpServletRequest request;

    @Lazy
    @Autowired
    private AnimaleService animaleService;

    public FileService(FileDAO fileDao, DocumentoDAO documentoDao) {
        this.fileDao = fileDao;
        this.documentoDao = documentoDao;
    }

    // ------------------------------------------------------------------------
    // ENUM per il tipo di soggetto — determina la sottocartella
    // ------------------------------------------------------------------------
    public enum TipoSoggetto {
        ANIMALE, CONTATTO, ASSOCIAZIONE
    }

    // ------------------------------------------------------------------------
    // UPLOAD — metodo unico per tutte le sezioni
    // ------------------------------------------------------------------------

    /**
     * Salva il file su filesystem e registra i metadati in an_files.
     * Ritorna l'AssoFile popolato con id_file, full_path e filename.
     */
    @Transactional
    public AssoFile uploadFile(MultipartFile file, String idDocumento,
                               TipoSoggetto tipoSoggetto, String idSoggetto) throws IOException, SQLIntegrityConstraintViolationException {
        String tenant = animaleService.resolveTenantFromRequest(request);

        // 1. Determina la sottocartella in base al soggetto
        String sottoCartella = switch (tipoSoggetto) {
            case ANIMALE      -> idSoggetto;
            case CONTATTO     -> idSoggetto; // es. "rossi_mario"
            case ASSOCIAZIONE -> "associazione";
        };

        // 2. Costruisce il path: basePath/tenant/sottoCartella/
        Path targetDir = Paths.get(basePath, tenant, sottoCartella);
        Files.createDirectories(targetDir);

        // 3. Nome file con timestamp per unicità
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        String storedFilename = System.currentTimeMillis() + "_" + originalFilename;

        // 4. Salvataggio fisico
        Path targetPath = targetDir.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 5. Popolamento AssoFile
        AssoFile assoFile = new AssoFile();
        assoFile.setId_documento(idDocumento);
        assoFile.setFilename(storedFilename);
        assoFile.setExtension(extension);
        assoFile.setSize(String.valueOf(file.getSize() / 1024));
        assoFile.setFull_path(targetDir.toAbsolutePath().toString()); // solo directory

        // 6. Persistenza su DB
        fileDao.saveOrUpdate(assoFile);

        return assoFile;
    }

    // ------------------------------------------------------------------------
    // DOWNLOAD — metodo unico per tutte le sezioni
    // ------------------------------------------------------------------------

    public void downloadFile(String idFile, HttpServletResponse response) throws IOException {
        AssoFile assoFile = fileDao.getFileByID(idFile);

        if (assoFile == null) {
            logger.error("downloadFile: nessun record in DB per idFile={}", idFile);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File non trovato nel database");
            return;
        }

        logger.debug("downloadFile: idFile={}, full_path='{}', filename='{}'",
                idFile, assoFile.getFull_path(), assoFile.getFilename());

        // Costruisce il path completo: se full_path è già assoluto lo usa direttamente,
        // altrimenti lo combina con basePath
        Path fullPathDir = Paths.get(assoFile.getFull_path());
        Path filePath;
        if (fullPathDir.isAbsolute()) {
            filePath = fullPathDir.resolve(assoFile.getFilename());
            logger.debug("downloadFile: path assoluto da DB = {}", filePath);
        } else {
            filePath = Paths.get(basePath, assoFile.getFull_path(), assoFile.getFilename());
            logger.debug("downloadFile: tentativo path 1 = {}", filePath.toAbsolutePath());

            // Fallback per vecchi record con path assoluto già incluso nel full_path
            if (!filePath.toFile().exists()) {
                filePath = fullPathDir.resolve(assoFile.getFilename());
                logger.debug("downloadFile: path 1 non trovato, tentativo path 2 = {}", filePath.toAbsolutePath());
            }
        }

        File file = filePath.toFile();

        if (!file.exists()) {
            logger.error("downloadFile: file fisico non trovato: {}", filePath.toAbsolutePath());
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "File fisico non trovato: " + filePath);
            return;
        }

        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) mimeType = "application/octet-stream";

        response.setContentType(mimeType);
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + assoFile.getFilename() + "\"");
        response.setContentLength((int) file.length());
        Files.copy(filePath, response.getOutputStream());
        response.getOutputStream().flush();
    }

    // ------------------------------------------------------------------------
    // DELETE — elimina file fisico e record DB
    // ------------------------------------------------------------------------

    @Transactional
    public void deleteFile(String idFile) throws IOException, SQLIntegrityConstraintViolationException {
        AssoFile assoFile = fileDao.getFileByID(idFile);
        if (assoFile != null) {
            Path filePath = Paths.get(assoFile.getFull_path(), assoFile.getFilename());
            if (!filePath.toFile().exists()) {
                filePath = Paths.get(assoFile.getFull_path());
            }
            Files.deleteIfExists(filePath);
            fileDao.deleteByID(idFile);
        }
    }

    // ------------------------------------------------------------------------
    // HELPER — salva documento + file in un'unica operazione
    // ------------------------------------------------------------------------

    @Transactional
    public Documento saveDocumentoConFile(Documento documento, MultipartFile file,
                                          TipoSoggetto tipoSoggetto, String idSoggetto,
                                          String account) throws IOException, SQLIntegrityConstraintViolationException {
        documento.setAccount(account);
        if (documento.getNum_documento() == null) documento.setNum_documento("");

        // 1. Salva il documento
        documento = documentoDao.saveOrUpdate(documento);

        // 2. Salva il file
        uploadFile(file, documento.getId_documento(), tipoSoggetto, idSoggetto);

        return documento;
    }
}
