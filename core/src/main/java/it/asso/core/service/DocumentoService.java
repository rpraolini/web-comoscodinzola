package it.asso.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.documenti.FileDAO;
import it.asso.core.dao.documenti.TipoDocumentoDAO;
import it.asso.core.dao.organizzazione.OrganizzazioneDAO;
import it.asso.core.dao.organizzazione.contabilita.DocumentoTemporaneoDAO;
import it.asso.core.dao.organizzazione.contabilita.FattureDAO;
import it.asso.core.dao.organizzazione.contabilita.MovimentiDAO;
import it.asso.core.dao.utente.UtenteDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.salute.EventoClinico;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.documenti.AssoFile;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.documenti.TipoDocumento;
import it.asso.core.model.organizzazione.Protocollo;
import it.asso.core.model.organizzazione.contabilita.DocumentoTemporaneo;
import it.asso.core.model.utente.Utente;
import it.asso.core.security.UserAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DocumentoService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoService.class);

    // DAO
    private final DocumentoTemporaneoDAO documentoTemporaneoDao;
    private final DocumentoDAO documentoDao;
    private final TipoDocumentoDAO tipoDocumentoDao;
    private final FileDAO fileDao;
    private final ContattoDAO contattoDao;
    private final FattureDAO fatturaDao;
    private final MovimentiDAO movimentiDao;
    private final UtenteDAO utenteDao;

    private final OrganizzazioneDAO organizzazioneDao;
    private final ObjectMapper objectMapper;
    @Autowired
    private HttpServletRequest request;

    @Lazy
    @Autowired
    private AnimaleService animaleService;

    @Lazy
    @Autowired
    private AnimaleDAO animaleDao;

    @Lazy
    @Autowired
    private FileService fileService;

    // Iniettiamo il path dal file properties
    @Value("${file.upload.base-path}")
    private String basePath;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public DocumentoService(DocumentoTemporaneoDAO documentoTemporaneoDao, DocumentoDAO documentoDao, TipoDocumentoDAO tipoDocumentoDao, FileDAO fileDao, ContattoDAO contattoDao, FattureDAO fatturaDao, MovimentiDAO movimentiDao, UtenteDAO utenteDao, OrganizzazioneDAO organizzazioneDao, ObjectMapper objectMapper, @Value("${path_doc}") String pathDoc) {
        this.documentoTemporaneoDao = documentoTemporaneoDao;
        this.documentoDao = documentoDao;
        this.tipoDocumentoDao = tipoDocumentoDao;
        this.fileDao = fileDao;
        this.contattoDao = contattoDao;
        this.fatturaDao = fatturaDao;
        this.movimentiDao = movimentiDao;
        this.utenteDao = utenteDao;
        this.organizzazioneDao = organizzazioneDao;
        this.objectMapper = objectMapper;
    }




    @Transactional
    public void saveDocumentoEventoClinicoSemplice(String idEvento, String idAnimale,
                                                   String documentoJson, MultipartFile file, UserAuth user)
            throws IOException, SQLIntegrityConstraintViolationException {

        ObjectMapper mapper = new ObjectMapper();
        Documento documento = mapper.readValue(documentoJson, Documento.class);
        documento.setAccount(user.getUsername());

        documento = documentoDao.saveOrUpdate(documento);

        if (documento.getId_documento() != null) {
            documentoDao.saveDocumentoEventoClinico(documento.getId_documento(), idEvento);
        }

        // Ricaviamo il tenant dalla request invece che dall'organizzazione
        String tenant = animaleService.resolveTenantFromRequest(request);

        MultipartFile[] files = { file };
        handleFileUploadAndDbEntry(documento, files, tenant, idAnimale, null);
    }


    /**
     * Recupera la lista completa dei documenti associati a un animale
     */
    public List<Documento> getDocumentiPerAnimale(String idAnimale) {
        // Chiamata al DAO che usa il ResultSetExtractor per mappare Documento + List<AssoFile>
        return documentoDao.getDocumentiByAnimale(idAnimale);
    }

    // ------------------------------------------------------------------------
    // UTILITY PRIVATE
    // ------------------------------------------------------------------------

    private void createDirectoryIfNotExist(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                logger.error("Impossibile creare la directory: " + directory, e);
            }
        }
    }

    private void generateProtocollo(Documento documento, Utente utente, String mittente, String oggetto) throws AssoServiceException {
        if (documento == null || documento.getId_documento() == null) return;

        Protocollo protocollo = new Protocollo();
        protocollo.setId_documento(documento.getId_documento());

        if (documento.getTipoDocumento() == null) {
            documento.setTipoDocumento(tipoDocumentoDao.getTipoDocumentoByID(documento.getId_tipo_documento()));
        }

        protocollo.setDocumento(documento.getTipoDocumento().getDocumento());
        protocollo.setDestinatario(utente.getCognome() + " " + utente.getNome());
        protocollo.setMittente(mittente);
        protocollo.setOggetto(oggetto);

        organizzazioneDao.save(protocollo);
    }

    // Logica di base per salvare il file e i metadati AssoFile
    private void saveFileAndMetadata(Documento documento, MultipartFile fileAsso, String fullPath, String filenamePrefix, int index) throws IllegalStateException, IOException, SQLIntegrityConstraintViolationException {

        String suffix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        AssoFile asso = new AssoFile();
        asso.setSize(String.valueOf(fileAsso.getBytes().length));

        String originalFilename = fileAsso.getOriginalFilename();
        String[] fileExt = originalFilename != null ? originalFilename.split("\\.") : new String[0];
        String estensione = fileExt.length > 0 ? fileExt[fileExt.length - 1] : "";

        asso.setExtension(estensione);

        TipoDocumento td = tipoDocumentoDao.getTipoDocumentoByID(documento.getId_tipo_documento());

        asso.setFilename((td.getPrefix_filename() + "_" + index + "_" + suffix + "." + estensione).toLowerCase());
        asso.setFull_path(fullPath);
        asso.setId_documento(documento.getId_documento());

        // 1. Salva metadati nel DB
        fileDao.saveOrUpdate(asso);

        // 2. Scrivi il file su disco
        fileDao.storeFile(fileAsso, asso.getFull_path(), asso.getFilename());
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (DELEGATI DAL CONTROLLER)
    // ------------------------------------------------------------------------

    public List<TipoDocumento> getTipoDocumenti(String ambito) {
        return tipoDocumentoDao.getTipoDocumento(ambito);
    }

    public List<Documento> getDocumentiByEvento(String idEvento) {
        return documentoDao.getDocumentiByIDEvento(idEvento);
    }

    public Documento getDocumento(String idDocumento) {
        return documentoDao.getDocumentoByID(idDocumento);
    }

    public List<Documento> getDocumentiAnimale(String idAnimale) {
        return documentoDao.getDocumentiAnimaleByIDAnimale(idAnimale);
    }

    public List<Documento> getDocumentiMovimento(String idMovimento) {
        return documentoDao.getDocumentiAnimaleByIDAnimale(idMovimento);
    }

    public List<Documento> getDocumentiIter(String idIter) {
        return documentoDao.getDocumentiByIDIter(idIter);
    }

    public List<Documento> getDocumentiFattura(String idFattura) {
        return documentoDao.getDocumentiAnimaleByIDAnimale(idFattura);
    }

    public List<DocumentoTemporaneo> getDocumentiFatturaTemporaneo(String anno) {
        return documentoTemporaneoDao.getByYear(anno);
    }

    public List<Documento> getDocumentiAssociazione(String ambito) {
        return documentoDao.getDocumentiAssociazione(ambito);
    }

    public File getPhysicalFile(String idAssoFile) throws AssoServiceException {
        AssoFile f = fileDao.getFileByID(idAssoFile);

        if(f == null) {
            throw new AssoServiceException("File non trovato nel database.");
        }

        Path filePath = Paths.get(basePath, f.getFull_path(), f.getFilename());
        return filePath.toFile();
    }

    // ------------------------------------------------------------------------
    // METODI DI UPLOAD E SALVATAGGIO COMPLESSO (TRANSAZIONALI)
    // ------------------------------------------------------------------------

   // --- SAVE DOCUMENTO CONTATTO ---
    @Transactional(rollbackFor = {AssoServiceException.class, IOException.class, SQLIntegrityConstraintViolationException.class})
    public void saveDocumentoContatto(String jsonContatto, String jsonDocumento, MultipartFile[] files, UserAuth user) throws AssoServiceException, IllegalStateException, IOException, SQLIntegrityConstraintViolationException {

        Contatto contatto = objectMapper.readValue(jsonContatto, Contatto.class);
        Documento documento = objectMapper.readValue(jsonDocumento, Documento.class);
        documento.setAccount(user.getUsername());

        documento = documentoDao.saveOrUpdate(documento);

        // Protocollo
        Utente utente = utenteDao.findByUsername(user.getUsername());
        String destinatario = utente.getCognome() + " " + utente.getNome();
        String mittente = contatto.getRag_sociale() == null? contatto.getCognome() + " " + contatto.getCognome() : contatto.getRag_sociale();
        String oggetto = documento.getTipoDocumento().getDocumento() + " - " + mittente;
        generateProtocollo(documento, utente, mittente, oggetto);

        // Collegamento
        if (documento.getId_documento() != null) {
            documentoDao.saveDocumentoContatto(documento.getId_documento(), contatto.getId_contatto());
        }

        // Gestione File
        handleFileUploadAndDbEntry(documento, files, user.getUtente().getOrganizzazione().getTenant(), null, contatto);
    }

    // --- SAVE DOCUMENTO ASSOCIAZIONE ---
    @Transactional(rollbackFor = {AssoServiceException.class, IOException.class, SQLIntegrityConstraintViolationException.class})
    public void saveDocumentoAssociazione(String jsonDocumento, MultipartFile[] files, UserAuth user, String ambito) throws AssoServiceException, IllegalStateException, IOException, SQLIntegrityConstraintViolationException {

        Documento documento = objectMapper.readValue(jsonDocumento, Documento.class);
        documento.setAccount(user.getUsername());

        boolean exist = documento.getId_documento() != null;

        documento = documentoDao.saveOrUpdate(documento);

        // Protocollo
        if(documento != null && documento.getTipoDocumento() != null) {
            Protocollo protocollo = new Protocollo();
            protocollo.setId_documento(documento.getId_documento());
            protocollo.setDocumento(documento.getTipoDocumento().getDocumento());
            protocollo.setDestinatario(user.getUtente().getCognome() + " " + user.getUtente().getNome());
            protocollo.setMittente("Associazione");
            protocollo.setOggetto(documento.getTipoDocumento().getDocumento()  + " - " + protocollo.getMittente());
            organizzazioneDao.save(protocollo);
        }

        // Collegamento
        if (!exist) {
            if (ambito.equals(Def.DOC_ASSOCIAZIONE) || ambito.equals(Def.DOC_VERBALI)) {
                documentoDao.saveDocumentoAssociazione(documento.getId_documento(), Def.NUM_UNO);
            }
        } else if (files != null && files.length > 0) {
            fileDao.deleteByIDDocumento(documento.getId_documento());
        }

        // Gestione File
        handleFileUploadAssociazione(documento, files, user.getUtente().getOrganizzazione().getTenant());
    }

    // ------------------------------------------------------------------------
    // METODI DI ELIMINAZIONE (TRANSAZIONALI)
    // ------------------------------------------------------------------------

    @Transactional(rollbackFor = {DataIntegrityViolationException.class, AssoServiceException.class})
    public void deleteDocumentoContatto(String idDocumento, String idContatto) throws DataIntegrityViolationException, AssoServiceException, SQLIntegrityConstraintViolationException, IOException {
        documentoDao.deleteDocumentoContatto(idDocumento, idContatto);
        documentoDao.deleteByID(idDocumento);
        // Aggiungere logica di eliminazione file fisico se necessaria
    }

    @Transactional(rollbackFor = {DataIntegrityViolationException.class, AssoServiceException.class})
    public void deleteDocumentoAssociazione(String idDocumento) throws DataIntegrityViolationException, AssoServiceException, SQLIntegrityConstraintViolationException, IOException {
        documentoDao.deleteDocumentoAssociazione(idDocumento);
        documentoDao.deleteByID(idDocumento);
        // Aggiungere logica di eliminazione file fisico se necessaria
    }



    // ------------------------------------------------------------------------
    // LOGICA DI GESTIONE FILESYSTEM (HANDLE FILE UPLOAD)
    // ------------------------------------------------------------------------

    private void handleFileUploadAndDbEntry(Documento documento, MultipartFile[] files, String tenant, String idAnimale, Contatto contatto) throws IllegalStateException, IOException, SQLIntegrityConstraintViolationException {

        if (files == null || files.length == 0) return;

        // Determina la sottocartella (animale o contatto)
        String baseDirName = idAnimale != null ? idAnimale : (contatto != null ? (Def.TC_PERSONA_FISICA.equals(contatto.getId_tipo_contatto()) ? contatto.getNome().toLowerCase() + "_" + contatto.getCognome().toLowerCase() : contatto.getRag_sociale().toLowerCase()) : "generico");

        String fullPath = tenant + "/" + baseDirName;
        Path directory = Paths.get(basePath, fullPath);
        createDirectoryIfNotExist(directory);

        int i = 1;

        for (MultipartFile fileAsso : files) {
            String originalFilename = fileAsso.getOriginalFilename();
            String[] fileExt = originalFilename != null ? originalFilename.split("\\.") : new String[0];
            String estensione = fileExt.length > 0 ? fileExt[fileExt.length - 1] : "";

            AssoFile assoFile = new AssoFile();
            assoFile.setSize(String.valueOf(fileAsso.getBytes().length));
            assoFile.setExtension(estensione);

            // Creazione filename con prefisso e indice
            TipoDocumento td = tipoDocumentoDao.getTipoDocumentoByID(documento.getId_tipo_documento());
            String filenamePrefix = td.getPrefix_filename();
            String suffix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            assoFile.setFilename((filenamePrefix + "_" + i + "_" + suffix + "." + estensione).toLowerCase());
            assoFile.setFull_path(fullPath);
            assoFile.setId_documento(documento.getId_documento());

            // 1. Salva metadati nel DB
            fileDao.saveOrUpdate(assoFile);

            // 2. Scrivi il file su disco
            fileDao.storeFile(fileAsso, assoFile.getFull_path(), assoFile.getFilename());
            i++;
        }
    }

    // Logica specifica per i documenti dell'Associazione
    private void handleFileUploadAssociazione(Documento documento, MultipartFile[] files, String tenant) throws IllegalStateException, IOException, SQLIntegrityConstraintViolationException {
        if (files == null || files.length == 0) return;

        String baseDir = tenant + "/associazione";
        Path directory = Paths.get(basePath, baseDir);
        createDirectoryIfNotExist(directory);

        for (MultipartFile fileAsso : files) {
            String originalFilename = fileAsso.getOriginalFilename();
            String[] fileExt = originalFilename != null ? originalFilename.split("\\.") : new String[0];
            String estensione = fileExt.length > 0 ? fileExt[fileExt.length - 1] : "";

            AssoFile asso = new AssoFile();
            asso.setExtension(estensione);
            asso.setId_documento(documento.getId_documento());
            asso.setSize(String.valueOf(fileAsso.getBytes().length));

            String s = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
            asso.setFull_path(baseDir);
            asso.setFilename(s + "_" + originalFilename.toLowerCase());

            fileDao.saveOrUpdate(asso);
            fileDao.storeFile(fileAsso, asso.getFull_path(), asso.getFilename());
        }
    }


    // ------------------------------------------------------------------------
    // NUOVI METODI
    // ------------------------------------------------------------------------

    @Transactional
    public void saveDocumentoEventoClinico(String documentoJson, String idEvento,
                                           String idAnimale, MultipartFile file, UserAuth user) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Documento documento = mapper.readValue(documentoJson, Documento.class);
        documento.setAccount(user.getUsername());
        if (documento.getNum_documento() == null) documento.setNum_documento("");

        documento = documentoDao.saveOrUpdate(documento);
        documentoDao.saveDocumentoEventoClinico(documento.getId_documento(), idEvento);
        fileService.uploadFile(file, documento.getId_documento(),
                FileService.TipoSoggetto.ANIMALE, idAnimale);
    }

    @Transactional
    public void saveDocumento(String idAnimale, Documento doc, MultipartFile file, UserAuth user) throws IOException, SQLIntegrityConstraintViolationException {
        doc.setAccount(user.getUsername());
        if (doc.getNum_documento() == null) doc.setNum_documento("");
        doc = documentoDao.saveOrUpdate(doc);
        documentoDao.saveDocumentoAnimale(doc.getId_documento(), idAnimale);
        fileService.uploadFile(file, doc.getId_documento(),
                FileService.TipoSoggetto.ANIMALE, idAnimale);
    }

    public void downloadDocumento(String idFile, HttpServletResponse response) throws IOException {
        fileService.downloadFile(idFile, response);
    }

    @Transactional
    public void deleteDocumento(String idDocumento) throws IOException, SQLIntegrityConstraintViolationException {
        List<AssoFile> files = documentoDao.getFilesByDocumento(idDocumento);
        for (AssoFile f : files) {
            fileService.deleteFile(f.getId_file());
        }
        documentoDao.deleteDocumento(idDocumento);
    }
}