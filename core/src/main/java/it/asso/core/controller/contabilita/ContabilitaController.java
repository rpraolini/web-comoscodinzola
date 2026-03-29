package it.asso.core.controller.contabilita;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.contabilita.ContabilitaDAO;
import it.asso.core.dao.contabilita.ScadenziarioDAO;
import it.asso.core.model.contabilita.Pagamento;
import it.asso.core.model.contabilita.PrevisioneSpesa;
import it.asso.core.model.contabilita.Scadenziario;
import it.asso.core.security.UserAuth;
import it.asso.core.service.ContabilitaService; // Nuovo Service Layer
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jsp/private/contabilita") // Path base per pulizia
public class ContabilitaController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE E DEI DAO
    private final ContabilitaService contabilitaService; // Per logica complessa
    private final ContabilitaDAO contabilitaDAO; // Per operazioni semplici/lettura
    private final ScadenziarioDAO scadenziarioDao; // Per operazioni semplici/lettura

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public ContabilitaController(ContabilitaService contabilitaService, ContabilitaDAO contabilitaDAO, ScadenziarioDAO scadenziarioDao) {
        this.contabilitaService = contabilitaService;
        this.contabilitaDAO = contabilitaDAO;
        this.scadenziarioDao = scadenziarioDao;
    }

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_CONTABILITA')";

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (GET)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getPrevisioni.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<PrevisioneSpesa> getPrevisioni() {
        return contabilitaDAO.getPrevisioneSpesa();
    }

    @RequestMapping(value = "/getPagamentiByIDEvento.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Pagamento> getPagamentiByIDEvento(@RequestParam String id) {
        return contabilitaDAO.getPagamentiByEvento(id);
    }

    @RequestMapping(value = "/getScadenziario.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Scadenziario> getScadenziario() {
        return scadenziarioDao.getScadenziario();
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (DELEGATI AL SERVICE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdatePagamento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission()
    public @ResponseBody ResponseEntity<String> saveOrUpdatePagamento(@RequestBody Pagamento pagamento, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {

        // Logica complessa di ripartizione e salvataggio è nel Service
        String result = contabilitaService.handlePagamento(pagamento, user);

        if (Def.STR_ERROR.equals(result)) {
            // Assumiamo che il service lanci AssoServiceException o restituisca l'errore
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("\"Errore durante il salvataggio o la ripartizione.\"");
        }
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/deletePagamento.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission()
    public @ResponseBody ResponseEntity<String> deletePagamento(@RequestBody Pagamento pagamento) throws AssoServiceException {
        try {
            String result = contabilitaDAO.deletePagamento(pagamento);
            return ResponseEntity.ok().body("\"" + result + "\"");
        } catch(Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}