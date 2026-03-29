package it.asso.core.controller.raccolta;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.report.SimpleReportExporter;
import it.asso.core.controller.report.SimpleReportFiller;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.raccolta.EventoDAO;
import it.asso.core.dao.raccolta.MerceDAO;
import it.asso.core.dao.raccolta.TurnoDAO;
import it.asso.core.model.contatto.ContattiWrapper;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.raccolta.Evento;
import it.asso.core.model.raccolta.Merce;
import it.asso.core.model.raccolta.Turno;
import it.asso.core.security.UserAuth;
import it.asso.core.service.RaccoltaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/jsp/private/raccolta") // Path base
public class RaccoltaController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE E DEI COMPONENTI REPORT
    private final RaccoltaService raccoltaService;
    private final SimpleReportFiller simpleReportFiller;
    private final EventoDAO eventoDao; // Mantenuto per le interazioni semplici
    private final TurnoDAO turnoDao; // Mantenuto per le interazioni semplici
    private final MerceDAO merceDao;
    private final ContattoDAO contattoDao;

    // Configurazione (mantenute per i report)
    private final String pathReport;

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String AUTH = "isAuthenticated()";
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_RACCOLTA')";


    // COSTRUTTORE CON INIEZIONE
    public RaccoltaController(RaccoltaService raccoltaService, SimpleReportFiller simpleReportFiller, EventoDAO eventoDao, TurnoDAO turnoDao, MerceDAO merceDao,ContattoDAO contattoDao, @Value("${path_report}") String pathReport) {
        this.raccoltaService = raccoltaService;
        this.simpleReportFiller = simpleReportFiller;
        this.eventoDao = eventoDao;
        this.turnoDao = turnoDao;
        this.merceDao = merceDao;
        this.pathReport = pathReport;
        this.contattoDao = contattoDao;
    }

    /*---------------------------------------- GESTIONE EVENTO -----------------------------------------------------------*/

    @RequestMapping(value = "/getEventoByID.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody Evento getByID(@RequestParam String idEvento) throws AssoServiceException {
        // Delega al Service la logica di popolamento
        return raccoltaService.getEventoByID(idEvento);
    }

    @RequestMapping(value = "/getAllEventi.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Evento> getAll() throws AssoServiceException {
        // Delega al Service la logica di popolamento
        return raccoltaService.getAllEventiWithContatti();
    }

    @RequestMapping(value = "/getEventiBySearch.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Evento> getEventiBySearch(@RequestParam String strToSearch) throws AssoServiceException {
        // Delega al Service la logica di ricerca e popolamento
        return raccoltaService.getEventiBySearch(strToSearch);
    }

    @SuppressWarnings("unused")
    @RequestMapping(value = "/saveEvento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody Evento save(@RequestBody Evento evento, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        // Delega al Service la logica complessa di creazione turni
        return raccoltaService.saveEvento(evento, user);
    }


    @RequestMapping(value = "/deleteEvento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteByID(@RequestParam String idEvento) throws AssoServiceException {
        try {
            eventoDao.deleteByID(idEvento);
            return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
        } catch(DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501); // Se ci sono chiavi esterne
        } catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    /*---------------------------------------- GESTIONE TURNI -----------------------------------------------------------*/


    @RequestMapping(value = "/deleteTurnoByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteTurnoByID(@RequestParam String idTurno) throws AssoServiceException {
        try {
            turnoDao.deleteByID(idTurno);
            return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
        }catch(DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        }catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveTurno.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody Turno saveTurno(@RequestBody Turno turno) throws AssoServiceException {
        // La logica di base è delegata al Service
        return raccoltaService.saveTurno(turno);
    }

    /*---------------------------------------- GESTIONE CONTATTI -----------------------------------------------------------*/

    @RequestMapping(value = "/deleteContattoByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteContattoByID(@RequestParam String idTurno, @RequestParam String idContatto, @RequestParam String idEvento) throws AssoServiceException {
        try {
            turnoDao.deleteContattoByID(idTurno, idContatto, idEvento);
            return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
        }catch(DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        }catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveContatto.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody Turno saveContatto(@RequestParam String idTurno, @RequestParam String idContatto, @RequestParam String idEvento) throws AssoServiceException {
        // Logica di base è delegata al Service
        return raccoltaService.saveContatto(idTurno, idContatto, idEvento);
    }

    @RequestMapping(value = "/getRaccoglitoriByCentro.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Contatto> getRaccoglitoriByCentro(@RequestParam String idContatto) throws AssoServiceException {
        return contattoDao.getRaccoglitoriByCentro(idContatto);
    }

    @RequestMapping(value = "/inviaMail.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> inviaMail(@RequestBody ContattiWrapper contattiWrapper, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws AssoServiceException {
        // Logica di invio mail è nel Service
        String result = raccoltaService.inviaMailRaccoltaCibo(contattiWrapper, user, request);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/inviaMailRingraziamento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> inviaMailRingraziamento(@RequestBody ContattiWrapper contattiWrapper, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws AssoServiceException {
        // Logica di invio mail è nel Service
        String result = raccoltaService.inviaMailRingraziamentoRaccoltaCibo(contattiWrapper, user, request);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }


    @RequestMapping(value = "/getProdotti.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Merce> getProdotti(@RequestParam String strToSearch) throws AssoServiceException {
        return merceDao.getByStrToSearch(strToSearch);
    }

    @RequestMapping(value = "/saveProdotto.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody Evento saveProdotto(@RequestParam String idEvento, @RequestParam String idMerce, @RequestParam String quantita, @RequestParam String pesoTot) throws AssoServiceException {
        // Logica di salvataggio prodotto (EventoMerce) è nel Service
        return raccoltaService.saveProdottoRaccolta(idEvento, idMerce, quantita, pesoTot);
    }

    @RequestMapping(value = "/getProdotto.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Merce getProdotto(@RequestParam String idMerce, @RequestParam String idEvento) throws AssoServiceException {
        return merceDao.getByIDEvento(idMerce,idEvento);
    }

    @RequestMapping(value = "/deleteProdottoByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody Evento deleteProdottoByID(@RequestParam String idEvento, @RequestParam String idMerce) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        // Logica di eliminazione prodotto (EventoMerce) è nel Service
        return raccoltaService.deleteProdottoRaccolta(idEvento, idMerce);
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI REPORT (DOWNLOAD)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/exportXls.html", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public void exportXlsx(HttpServletRequest request, HttpServletResponse response, @RequestParam String idEvento) throws AssoServiceException {
        // Logica di preparazione del report è nel Service
        try {
            simpleReportFiller.setReportFileName(pathReport + "raccolto.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", idEvento);
            //parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportXlsx(response, "raccolto");
        } catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}