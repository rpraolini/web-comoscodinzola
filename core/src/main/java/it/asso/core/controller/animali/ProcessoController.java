package it.asso.core.controller.animali;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.gestione.ProcessoDAO;
import it.asso.core.security.UserAuth;
import it.asso.core.service.ProcessoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/private/processo")
public class ProcessoController { // Non estende BaseController

    // 1. INIEZIONE DEL SERVICE (per logica complessa/push)
    private final ProcessoService processoService;

    // Manteniamo solo i DAO necessari per le operazioni non delegate
    private final ProcessoDAO processoDao;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public ProcessoController(ProcessoService processoService, ProcessoDAO processoDao) {
        this.processoService = processoService;
        this.processoDao = processoDao;
    }

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_PERMISSION = "@authz.checkWritePermission('AREA_DATI_ANIMALE')";

    // ------------------------------------------------------------------------
    // NUOVI METODI
    // ------------------------------------------------------------------------

    @PostMapping("/registraDecesso")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> registraDecesso(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserAuth user) {
        try {
            String idAnimale = body.get("id_animale");
            String dtDecesso = body.get("dt_decesso");

            processoService.registraDecesso(idAnimale, dtDecesso, user);

            return ResponseEntity.ok(Map.of("messaggio", "Decesso registrato con successo"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    // ------------------------------------------------------------------------
    // LOGICA COMPLESSA (DELEGATA AL SERVICE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/validazione.json", method = RequestMethod.GET)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> valida(@RequestParam String id, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        // La logica di verifica Evento Storico + Validazione è nel Service
        String result = processoService.validaAnimale(id, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/adottabile.json", method = RequestMethod.GET)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> adottabile(@RequestParam String id, @AuthenticationPrincipal UserAuth user) throws Exception {
        // La logica di aggiornamento stato + invio Push è nel Service
        String result = processoService.adottabileAnimale(id, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // ------------------------------------------------------------------------
    // LOGICA SEMPLICE (UTILIZZANO @PreAuthorize E ResponseEntity)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/revocaAdottabile.json", method = RequestMethod.GET)
    @PreAuthorize(WRITE_PERMISSION) // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> revocaAdottabile(@RequestParam String id, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String result = processoDao.revocaAdottabile(id, user.getUtente());
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

}