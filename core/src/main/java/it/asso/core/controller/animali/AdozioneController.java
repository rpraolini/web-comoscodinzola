package it.asso.core.controller.animali;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.report.SimpleReportExporter;
import it.asso.core.controller.report.SimpleReportFiller;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.organizzazione.OrganizzazioneDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.organizzazione.Organizzazione;
import it.asso.core.service.AnimaleService;
import jakarta.servlet.http.HttpServletRequest;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.animali.gestione.TipoIter;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.gestione.Pratica;
import it.asso.core.security.UserAuth;
import it.asso.core.service.AdozioneService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/adozione")
public class AdozioneController {

    private final AdozioneService adozioneService;
    private final ObjectMapper objectMapper;
    private final DataSource dataSource;
    private final AnimaleDAO animaleDao;
    private final OrganizzazioneDAO organizzazioneDao;
    private final AnimaleService animaleService;
    private final SimpleReportFiller simpleReportFiller;

    public AdozioneController(AdozioneService adozioneService, ObjectMapper objectMapper,
                               DataSource dataSource, AnimaleDAO animaleDao,
                               OrganizzazioneDAO organizzazioneDao, AnimaleService animaleService,
                               SimpleReportFiller simpleReportFiller) {
        this.adozioneService = adozioneService;
        this.objectMapper = objectMapper;
        this.dataSource = dataSource;
        this.animaleDao = animaleDao;
        this.organizzazioneDao = organizzazioneDao;
        this.animaleService = animaleService;
        this.simpleReportFiller = simpleReportFiller;
    }

    // PRATICHE
    @GetMapping("/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Pratica>> getPratiche(@PathVariable String idAnimale) {
        return ResponseEntity.ok(adozioneService.getPraticheByAnimale(idAnimale));
    }

    @PostMapping("/pratica/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> nuovaPratica(
            @PathVariable String idAnimale,
            @AuthenticationPrincipal UserAuth user) {
        try {
            Pratica p = adozioneService.nuovaPratica(idAnimale, user);
            return ResponseEntity.ok(Map.of("pratica", p, "messaggio", "Pratica creata"));
        } catch (AssoServiceException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }
    }

    @PutMapping("/pratica/{idPratica}/chiudi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> chiudiPratica(@PathVariable String idPratica) {
        adozioneService.chiudiPratica(idPratica);
        return ResponseEntity.ok(Map.of("messaggio", "Pratica chiusa"));
    }

    @DeleteMapping("/pratica/{idPratica}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> eliminaPratica(
            @PathVariable String idPratica,
            @RequestParam String idAnimale,
            @AuthenticationPrincipal UserAuth user) {
        try {
            adozioneService.eliminaPratica(idPratica, idAnimale, user);
            return ResponseEntity.ok(Map.of("messaggio", "Pratica eliminata"));
        } catch (AssoServiceException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }
    }

    // TIPI ITER
    @GetMapping("/tipi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoIter>> getTipi() {
        return ResponseEntity.ok(adozioneService.getTipiIter());
    }

    // ITER
    @PostMapping("/iter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> aggiungiIter(
            @RequestBody Iter iter,
            @AuthenticationPrincipal UserAuth user) {
        try {
            String id = adozioneService.aggiungiIter(iter, user);
            return ResponseEntity.ok(Map.of("id", id, "messaggio", "Iter aggiunto"));
        } catch (AssoServiceException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }
    }

    @PutMapping("/iter/{idIter}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> aggiornaIter(
            @PathVariable String idIter,
            @RequestBody Iter iter,
            @AuthenticationPrincipal UserAuth user) {
        try {
            iter.setId_iter(idIter);
            adozioneService.aggiornaIter(iter, user);
            return ResponseEntity.ok(Map.of("messaggio", "Iter aggiornato"));
        } catch (AssoServiceException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }
    }

    @PostMapping(value = "/iter/{idIter}/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> saveDocumentoIter(
            @PathVariable String idIter,
            @RequestParam String idAnimale,
            @RequestParam("documento") String documentoJson,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserAuth user) {
        try {
            Documento doc = objectMapper.readValue(documentoJson, Documento.class);
            adozioneService.saveDocumentoIter(idIter, idAnimale, doc, file, user);
            return ResponseEntity.ok(Map.of("messaggio", "Documento salvato"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }
    }

    @GetMapping("/iter/{idIter}/affido/pdf")
    @PreAuthorize("isAuthenticated()")
    public void exportAffidoPdf(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String idIter,
            @RequestParam String idAnimale) throws AssoServiceException {
        try (var conn = dataSource.getConnection()) {
            String tenant = animaleService.resolveTenantFromRequest(request);
            Organizzazione org = organizzazioneDao.getByTenant(tenant);
            Animale animale = animaleDao.getById(idAnimale);

            String reportName = Def.NUM_UNO.equals(animale.getId_tipo_animale()) ? "affidoCane.jrxml" : "affidoGatto.jrxml";
            JasperReport jasperReport = JasperCompileManager.compileReport(
                new ClassPathResource("reports/" + reportName).getInputStream()
            );

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", idAnimale);
            parameters.put("idIter", idIter);
            parameters.put("logo_url", new ClassPathResource("images/" + org.getTenant() + "/logo.png").getURL().toString());
            parameters.put("rag_sociale", org.getRag_sociale() + " " + org.getSigla_tipo_organizzazione());
            parameters.put("iscrizione", org.getIscrizione());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            SimpleReportExporter exporter = new SimpleReportExporter();
            exporter.setJasperPrint(jasperPrint);
            exporter.exportPdf(response, "affido");
        } catch (Exception ex) {
            throw new AssoServiceException(Def.STR_ERROR_000 + ex.getMessage());
        }
    }

    @GetMapping("/iter/{idIter}/passaggio/pdf")
    @PreAuthorize("isAuthenticated()")
    public void exportPassaggioPdf(
            HttpServletResponse response,
            @PathVariable String idIter,
            @RequestParam String idAnimale) throws AssoServiceException {
        try (var conn = dataSource.getConnection()) {
            JasperReport jasperReport = JasperCompileManager.compileReport(
                new ClassPathResource("reports/trasferimento.jrxml").getInputStream()
            );

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", idAnimale);
            parameters.put("idIter", idIter);
            parameters.put("url_stemma", new ClassPathResource("images/stemmi/campania.png").getURL().toString());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            SimpleReportExporter exporter = new SimpleReportExporter();
            exporter.setJasperPrint(jasperPrint);
            exporter.exportPdf(response, "trasferimento");
        } catch (Exception ex) {
            throw new AssoServiceException(Def.STR_ERROR_000 + ex.getMessage());
        }
    }

    @GetMapping("/iter/{idIter}/questionario/pdf")
    @PreAuthorize("isAuthenticated()")
    public void exportQuestionarioPdf(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String idIter,
            @RequestParam String questKey) throws AssoServiceException {
        try {
            String tenant = animaleService.resolveTenantFromRequest(request);
            Organizzazione org = organizzazioneDao.getByTenant(tenant);
            String reportDir = new ClassPathResource("reports/").getFile().getAbsolutePath() + File.separator;

            simpleReportFiller.setReportFileName(reportDir + "questionario_dettaglio.jrxml");
            simpleReportFiller.compileReport();
            simpleReportFiller.setReportFileName(reportDir + "questionario.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("key", questKey);
            parameters.put("sotto_report", reportDir);
            parameters.put("logo_url", new ClassPathResource("images/" + tenant + "/logo.png").getURL().toString());
            parameters.put("rag_sociale", org.getRag_sociale() + " " + org.getSigla_tipo_organizzazione());
            parameters.put("iscrizione", org.getIscrizione());

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            JasperPrint jasperPrint = simpleReportFiller.getJasperPrint();
            if (jasperPrint == null) {
                throw new AssoServiceException("Impossibile generare il questionario: report non compilato o nessun dato trovato per la chiave fornita.");
            }

            SimpleReportExporter exporter = new SimpleReportExporter();
            exporter.setJasperPrint(jasperPrint);
            exporter.exportPdf(response, "questionario");
        } catch (AssoServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AssoServiceException(Def.STR_ERROR_000 + ex.getMessage());
        }
    }

    @PutMapping("/istruttoria/{idAnimale}/chiudi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> chiudiIstruttoria(
            @PathVariable String idAnimale,
            @AuthenticationPrincipal UserAuth user) {
        adozioneService.chiudiIstruttoria(idAnimale, user);
        return ResponseEntity.ok(Map.of("messaggio", "Istruttoria chiusa"));
    }

    @DeleteMapping("/iter/{idIter}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> eliminaIter(
            @PathVariable String idIter,
            @RequestParam String idAnimale,
            @AuthenticationPrincipal UserAuth user) {
        adozioneService.eliminaIter(idIter, idAnimale, user);
        return ResponseEntity.ok(Map.of("messaggio", "Iter eliminato"));
    }
}