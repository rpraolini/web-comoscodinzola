package it.asso.core.controller.pubblico;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.report.SimpleReportExporter;
import it.asso.core.controller.report.SimpleReportFiller;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.log.LogAttivitaDAO;
import it.asso.core.dto.ricerca.FiltroRicerca;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.animale.Caratteri;
import it.asso.core.model.documenti.Foto;
import it.asso.core.model.documenti.Video;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;
import it.asso.core.model.organizzazione.Organizzazione;
import it.asso.core.model.statistiche.SimpleResultSet;
import it.asso.core.service.PublicService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping({"/api/public", "/volantino.html"}) // Path base per pulizia
public class PublicController {

    private static Logger logger = LoggerFactory.getLogger(PublicController.class);

    // 1. INIEZIONE DEL SERVICE E DEI DAO NECESSARI PER I/O E MONITORAGGIO
    private final PublicService publicService;
    private final LogAttivitaDAO logAttivitaDAO;
    private final AnimaleDAO animaleDao;
    private final SimpleReportFiller simpleReportFiller;

    // Configurazione (mantenute per i report)
    private final String pathReport;
    private final String pathDoc;
    private final String pathPublicAssets;

    // COSTRUTTORE CON INIEZIONE
    public PublicController(PublicService publicService, LogAttivitaDAO logAttivitaDAO, AnimaleDAO animaleDao, SimpleReportFiller simpleReportFiller,
                            @Value("${path_report}") String pathReport,
                            @Value("${path_doc}") String pathDoc,
                            @Value("${path_public_assets}") String pathPublicAssets) {
        this.publicService = publicService;
        this.logAttivitaDAO = logAttivitaDAO;
        this.animaleDao = animaleDao;
        this.simpleReportFiller = simpleReportFiller;
        this.pathReport = pathReport;
        this.pathDoc = pathDoc;
        this.pathPublicAssets = pathPublicAssets;
    }

    @RequestMapping(value = "/getOrganizzazioneByTenant.json", method = RequestMethod.GET)
    public @ResponseBody Organizzazione getOrganizzazioneByTenant(@RequestParam String id) throws AssoServiceException {
        // CHIAMATA CORRETTA: getOrganizzazioneByTenant(String tenantId)
        return publicService.getOrganizzazioneByTenant(id);
    }

    // L'endpoint /index non dovrebbe restituire void se gestisce la sessione/tenant.
    // In Spring Boot, la sessione HTTP deve essere gestita diversamente.
    // Ho rimosso l'uso della sessione nel controller.
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public void getOrgByTenant(HttpServletRequest request, @RequestParam(required = false) String id) throws AssoServiceException {
        // Questo metodo è per la risoluzione del tenant all'ingresso (logica da Service)
        String tenant = publicService.resolveTenantFromRequest(request);
        // Se necessario, il Service imposterà qui il TenantContext
        // Questo metodo in Spring Boot non è più necessario per impostare un attributo di sessione "org"
        // ma potresti usarlo per reindirizzare a una landing page specifica del tenant.
    }

    @RequestMapping(value = "/getRandom.json", method = RequestMethod.GET)
    public @ResponseBody List<Animale> getRandom(HttpServletRequest request) {
        // La logica di popolamento/URL/Tenant è delegata al Service
        return publicService.getRandomAnimali(request);
    }

    @RequestMapping(value = "/getLietiFine.json", method = RequestMethod.GET)
    public @ResponseBody List<Animale> getLietiFine(HttpServletRequest request, @RequestParam String anno) {
        return publicService.getLietiFine(request, anno);
    }

    @RequestMapping(value = "/getLietiFineCount.json", method = RequestMethod.GET)
    public @ResponseBody String getLietiFineCount() {
        return animaleDao.getLietiFineCount();
    }

    @RequestMapping(value = "/getLietiFineCountByAnno.json", method = RequestMethod.GET)
    public @ResponseBody List<SimpleResultSet> getLietiFineCountByAnno() {
        return animaleDao.getLietiFineCountByAnno();
    }

    @GetMapping("/animale/{id}") // Sintassi più moderna di @RequestMapping
    public ResponseEntity<Animale> getAnimaleById(HttpServletRequest request, @PathVariable String id) {
        try {
            // 1. Recupero dati
            Animale animale = publicService.getAnimaleByIdWithDetails(request, id);

            // 2. Se non esiste, restituisci 404 invece di null o errore
            if (animale == null) {
                return ResponseEntity.notFound().build();
            }

            // 3. Monitoraggio (Avvolto in try-catch per non bloccare l'app se il log fallisce)
            try {
                logAttivitaDAO.save(
                        Def.LOG_VISUALIZZA_DETTAGLIO_PUBBLICO,
                        request.getRemoteAddr(),
                        animale.getNome(), // Ora siamo sicuri che animale non è null
                        request.getSession().getId()
                );
            } catch (Exception e) {
                // Loggiamo l'errore su console ma lasciamo continuare l'utente
                System.err.println("Errore salvataggio log attività: " + e.getMessage());
            }

            // 4. Ritorna 200 OK con l'oggetto
            return ResponseEntity.ok(animale);

        } catch (AssoServiceException e) {
            return ResponseEntity.status(500).build(); // Errore server
        }
    }

    @GetMapping("/getFoto.json")
    public ResponseEntity<List<Foto>> getFotoGallery(
            HttpServletRequest request,
            @RequestParam("id_animale") String id // Associa 'id_animale' dal FE alla variabile 'id' nel BE
    ) {
        try {
            List<Foto> foto = publicService.getFotoPubbliche(request, id);
            return ResponseEntity.ok(foto);
        } catch (AssoServiceException e) {
            // Se c'è un errore nel service, ritorna un 500 in modo pulito
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/getCaratteri.json")
    public ResponseEntity<List<Caratteri>> getCaratteri(
            @RequestParam("id_animale") String id // Associa 'id_animale' dal FE alla variabile 'id' nel BE
    ) {
        try {
            List<Caratteri> caratteri = publicService.getCaratteriByAnimale(id);
            return ResponseEntity.ok(caratteri);
        } catch (AssoServiceException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @RequestMapping(value = "/getVideoById.json", method = RequestMethod.GET)
    public @ResponseBody List<Video> getVideoById(@RequestParam String id) throws AssoServiceException {
        return publicService.getVideoPubblici(id);
    }


    @GetMapping("/ricerca.json")
    public ResponseEntity<List<Animale>> ricerca(HttpServletRequest request,@ModelAttribute FiltroRicerca filtri){
        // La logica di ricerca e popolamento è delegata al Service
        List<Animale> risultati = publicService.ricercaAnimali(request, filtri);
        return ResponseEntity.ok(risultati);
    }

    // Endpoint di localizzazione delegati al Service
    @GetMapping(value = "/province/{idRegione}")
    public @ResponseBody List<Provincia> getProvinceByRegione(@PathVariable("idRegione") String idRegione) throws AssoServiceException {
        return publicService.getProvinceByRegioneUsed(idRegione);
    }

    @RequestMapping(value = "/getRegioni.json", method = RequestMethod.GET)
    public @ResponseBody List<Regione> getRegioni() throws AssoServiceException {
        return publicService.getRegioni();
    }

    @GetMapping(value = "/regioni/{nazione}")
    public @ResponseBody List<Regione> getRegioniByNazione(@PathVariable("nazione") String nazione) throws AssoServiceException {
        return publicService.getRegioniByNazioneUsed(nazione);
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI REPORT (DOWNLOAD)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/volantino.html", method = RequestMethod.GET)
    public void exportVolantino(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) throws AssoServiceException {
        try {
            String tenant = publicService.resolveTenantFromRequest(request);

            /* ************************ MONITORAGGIO ATTIVITA ******************************************************/
            logAttivitaDAO.save(Def.LOG_VISUALIZZA_VOLANTINO_PUBBLICO, request.getRemoteAddr(), animaleDao.getById(id).getNome(), request.getSession().getId());
            /* ************************ FINE MONITORAGGIO **********************************************************/

            simpleReportFiller.setReportFileName(pathReport + "volantino.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", id);
            parameters.put("path_url", pathDoc + tenant + "/");
            parameters.put("logo_url", pathPublicAssets + tenant + "/logo.png");
            parameters.put("qrcode_url", pathPublicAssets + tenant + "/qrcode.png");

            Organizzazione org = publicService.getOrganizzazioneByTenant(tenant); // Uso il service
            parameters.put("sito", org.getUrl() == null ? "" : org.getUrl());
            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportPdf(response, "volantino");

        }catch(Exception e) {
            logger.error("Errore durante l'export del volantino: " + e.getMessage(), e);
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}