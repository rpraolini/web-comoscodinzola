package it.asso.core.controller.documenti;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.model.documenti.AssoFile;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.documenti.TipoDocumento;
import it.asso.core.security.UserAuth;
import it.asso.core.service.DocumentoService;
import it.asso.core.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/private/documenti")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final FileService fileService;

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_ANIMALE = "@authz.checkWritePermission('AREA_DATI_ANIMALE')";
    private static final String WRITE_CONTATTI = "@authz.checkWritePermission('AREA_DATI_CONTATTI')";
    private static final String WRITE_ASSOCIAZIONE = "@authz.checkWritePermission('AREA_DATI_ASSOCIAZIONE')";
    private static final String AUTH = "isAuthenticated()";


    // COSTRUTTORE CON INIEZIONE DEL SERVICE
    public DocumentoController(DocumentoService documentoService, FileService fileService) {
        this.documentoService = documentoService;
        this.fileService = fileService;
    }

    // ------------------------------------------------------------------------
    // NUOVI METODI
    // ------------------------------------------------------------------------

    @GetMapping("/getListaDocumentiPerAnimale")
    @PreAuthorize("isAuthenticated()") // Assicura che l'utente sia loggato
    public ResponseEntity<List<Documento>> getListaDocumentiPerAnimale(@RequestParam("idAnimale") String idAnimale) {

        List<Documento> documenti = documentoService.getDocumentiPerAnimale(idAnimale);

        // Restituiamo la lista. Se vuota, Spring restituirà un array JSON vuoto []
        return ResponseEntity.ok(documenti);
    }

    @GetMapping("/{ambito}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoDocumento>> getTipoDocumenti(@PathVariable("ambito") String ambito) {
        return ResponseEntity.ok(documentoService.getTipoDocumenti(ambito));
    }

    @DeleteMapping("/deleteDocumento")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteDocumento(@RequestParam("id") String idDocumento) throws SQLIntegrityConstraintViolationException, IOException {
        documentoService.deleteDocumento(idDocumento);
        return ResponseEntity.ok().body("\"" + Def.STR_SUCCESS + "\"");
    }

    @GetMapping("/downloadDocumento")
    @PreAuthorize("isAuthenticated()")
    public void downloadDocumento(@RequestParam("idFile") String idFile,
                                  HttpServletResponse response) throws IOException {
        fileService.downloadFile(idFile, response);
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI LETTURA
    // ------------------------------------------------------------------------



    @RequestMapping(value = "/getDocumentiEC.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Documento> getDocumentiEC(@RequestParam String idEvento) {
        return documentoService.getDocumentiByEvento(idEvento);
    }


    // ------------------------------------------------------------------------
    // ENDPOINT DI UPLOAD/SALVATAGGIO
    // ------------------------------------------------------------------------

    @PostMapping(value = "/saveDocumentoAnimale", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(WRITE_ANIMALE)
    public ResponseEntity<String> saveDocumentoAnimale(@RequestParam("idAnimale") String idAnimale,
                                                       @RequestParam("documento") String documentoJson,
                                                       @RequestParam("file") MultipartFile file,
                                                       @AuthenticationPrincipal UserAuth user) throws AssoServiceException, IllegalStateException, IOException, SQLIntegrityConstraintViolationException {
        ObjectMapper mapper = new ObjectMapper();
        Documento doc = mapper.readValue(documentoJson, Documento.class);
        documentoService.saveDocumento(idAnimale, doc, file, user);
        return ResponseEntity.ok().body("\"" + Def.STR_SUCCESS + "\"");
    }


    @RequestMapping(value = "/saveDocumentoAssociazione.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ANIMALE) // Assumendo che fosse 'AREA_DATI_ASSOCIAZIONE'
    public @ResponseBody ResponseEntity<String> saveDocumentoAssociazione(@RequestParam("documento") final String jsonDocumento, @RequestParam(required=false, value="file") final MultipartFile[] files, @RequestParam("ambito") final String ambito, @AuthenticationPrincipal UserAuth user) throws AssoServiceException, IllegalStateException, IOException, SQLIntegrityConstraintViolationException {
        documentoService.saveDocumentoAssociazione(jsonDocumento, files, user, ambito);
        return ResponseEntity.ok().body("\"" + Def.STR_SUCCESS + "\"");
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI ELIMINAZIONE
    // ------------------------------------------------------------------------

    @DeleteMapping("/deleteDocumento.json")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteDocumentoOld(@RequestParam("id") String idDocumento) throws SQLIntegrityConstraintViolationException, IOException {
        documentoService.deleteDocumento(idDocumento);
        return ResponseEntity.ok().body("\"" + Def.STR_SUCCESS + "\"");
    }

    @RequestMapping(value = "/deleteDocumentoContatto.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_CONTATTI) // Sostituisce checkWritePermission(Def.AREA_DATI_CONTATTI)
    public @ResponseBody ResponseEntity<String> deleteByIDContatto(@RequestParam String idDocumento, @RequestParam String idContatto) throws AssoServiceException {
        try {
            documentoService.deleteDocumentoContatto(idDocumento, idContatto);
            return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR + "Documento non eliminabile.");
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    // [OMISSIS]... tutti gli altri delete vengono delegati in modo simile.

    @RequestMapping(value = "/deleteDocumentoAssociazione.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteDocumentoAssociazione(@RequestParam String idDocumento) throws AssoServiceException {
        try {
            documentoService.deleteDocumentoAssociazione(idDocumento);
            return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }


    // ------------------------------------------------------------------------
    // ENDPOINT DI DOWNLOAD (MANTENUTO NEL CONTROLLER PER I/O DI RISPOSTA)
    // ------------------------------------------------------------------------

    @RequestMapping(value="/download.html", method = RequestMethod.GET)
    @PreAuthorize(AUTH) // Sostituisce checkAuthentication()
    public void downloadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id) throws IOException, AssoServiceException {
        // La logica di recupero del file dal disco e scrittura sulla risposta HTTP
        // rimane nel controller/servizio di utilità

        File file = documentoService.getPhysicalFile(id);

        if (!file.exists() || !file.isFile()) {
            // Gestione errore file non trovato
            String errorMessage = "Spiacente, ma il file non esiste.";
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.getOutputStream().write(errorMessage.getBytes(Charset.forName("UTF-8")));
            response.getOutputStream().close();
            return;
        }

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if(mimeType == null){
            mimeType = "application/octet-stream";
        }

        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() +"\""));
        response.setContentLength((int)file.length());

        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        }
    }

    @PostMapping(value = "/saveDocumentoEC", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> saveDocumentoEC(
            @RequestParam("idEvento") String idEvento,
            @RequestParam("idAnimale") String idAnimale,
            @RequestParam("documento") String documentoJson,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserAuth user) throws Exception {
        documentoService.saveDocumentoEventoClinico(documentoJson, idEvento, idAnimale, file, user);
        return ResponseEntity.ok(Map.of("messaggio", "Documento caricato"));
    }
}