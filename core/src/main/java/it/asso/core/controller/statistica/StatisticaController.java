package it.asso.core.controller.statistica;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.report.SimpleReportExporter;
import it.asso.core.controller.report.SimpleReportFiller;
import it.asso.core.dao.log.LogAttivitaDAO;
import it.asso.core.dao.statistiche.StatisticaDAO;
import it.asso.core.model.log.AttivitaSintetico;
import it.asso.core.model.statistiche.Marker;
import it.asso.core.model.statistiche.SimpleResultSet;
import it.asso.core.service.StatisticaService; // Nuovo Service Layer

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jsp/private")
public class StatisticaController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE E DEI COMPONENTI
    private final StatisticaService statisticaService;
    private final StatisticaDAO statisticaDAO;
    private final LogAttivitaDAO logAttivitaDAO;
    private final SimpleReportFiller simpleReportFiller;

    // Configurazione (mantenute per i report)
    private final String pathReport;

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String AUTH = "isAuthenticated()";

    // COSTRUTTORE CON INIEZIONE
    public StatisticaController(StatisticaService statisticaService, StatisticaDAO statisticaDAO, LogAttivitaDAO logAttivitaDAO, SimpleReportFiller simpleReportFiller, @Value("${path_report}") String pathReport) {
        this.statisticaService = statisticaService;
        this.statisticaDAO = statisticaDAO;
        this.logAttivitaDAO = logAttivitaDAO;
        this.simpleReportFiller = simpleReportFiller;
        this.pathReport = pathReport;
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI LETTURA (DELEGATI AL SERVICE PER LA LOGICA URL/TENANT)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/animale/getAnimaliAdottati.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH) // Sostituisce checkAuthentication()
    public @ResponseBody List<Marker> getAnimaliAdottati(HttpServletRequest request) throws AssoServiceException {
        // Logica di risoluzione URL/Tenant e caricamento Marker nel Service
        return statisticaService.getAnimaliAdottatiMarker(request);
    }

    @RequestMapping(value = "/animale/getPreaffidantiMarker.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Marker> getPreaffidantiMarker() throws AssoServiceException {
        return statisticaDAO.getPreaffidanti();
    }

    @RequestMapping(value = "/contatto/getCountAnimaliByStato.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody List<SimpleResultSet> getCountAnimaliByStato() throws AssoServiceException {
        return statisticaDAO.getCountAnimaliByStato();
    }

    @RequestMapping(value = "/contatto/getCountContattiByTipo.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody List<SimpleResultSet> getCountContattiByTipo() throws AssoServiceException {
        return statisticaDAO.getCountContattiByTipo();
    }

    @RequestMapping(value = "/contatto/getCountContatti.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResponseEntity<String> getCountContatti() throws AssoServiceException {
        String count = statisticaDAO.getCountContatti();
        return ResponseEntity.ok().body("\"" + count + "\"");
    }

    @RequestMapping(value = "/contatto/getCountAnimali.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResponseEntity<String> getCountAnimali() throws AssoServiceException {
        String count = statisticaDAO.getCountAnimali();
        return ResponseEntity.ok().body("\"" + count + "\"");
    }

    @RequestMapping(value = "/contatto/getCountAttivita.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<AttivitaSintetico> getCountAttivita(@RequestParam String arg) throws AssoServiceException {
        // Logica di business e decisione della query delegata al Service
        return statisticaService.getCountAttivita(arg);
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI REPORT (DOWNLOAD EXCEL)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/statistiche/exportAnimaliXls.html", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public void exportAnimaliXls(HttpServletResponse response) throws AssoServiceException {
        try {
            simpleReportFiller.setReportFileName(pathReport + "animali.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportXlsx(response, "animali");
        }catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/statistiche/exportContattiXls.html", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public void exportContattiXls(HttpServletResponse response) throws AssoServiceException {
        try {
            simpleReportFiller.setReportFileName(pathReport + "contatti.jrxml");
            simpleReportFiller.compileReport();

            Map<String, Object> parameters = new HashMap<>();
            //parameters.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);

            simpleReportFiller.setParameters(parameters);
            simpleReportFiller.fillReport();

            SimpleReportExporter simpleExporter = new SimpleReportExporter();
            simpleExporter.setJasperPrint(simpleReportFiller.getJasperPrint());
            simpleExporter.exportXlsx(response, "contatti");
        }catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}