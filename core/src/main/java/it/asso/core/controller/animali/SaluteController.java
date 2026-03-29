package it.asso.core.controller.animali;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.salute.EventoClinicoDAO;
import it.asso.core.model.animali.salute.EventoClinico;
import it.asso.core.model.animali.salute.TipoEvento;
import it.asso.core.model.animali.salute.TipoEventoClinico;
import it.asso.core.model.documenti.Documento;
import it.asso.core.security.UserAuth;
import it.asso.core.service.DocumentoService;
import it.asso.core.service.EventiCliniciService;
import it.asso.core.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/salute")
public class SaluteController {

    private final EventiCliniciService eventiCliniciService;
    private final EventoClinicoDAO eventoClinicoDao;
    private final DocumentoService documentoService;


    public SaluteController(EventiCliniciService eventiCliniciService, EventoClinicoDAO eventoClinicoDao, DocumentoService documentoService) {
        this.eventiCliniciService = eventiCliniciService;
        this.eventoClinicoDao = eventoClinicoDao;
        this.documentoService = documentoService;
    }

    @GetMapping("/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventoClinico>> getEventi(@PathVariable String idAnimale) throws AssoServiceException {
        return ResponseEntity.ok(eventiCliniciService.getEventiCliniciByIdAnimale(idAnimale));
    }

    @GetMapping("/tipi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoEvento>> getTipi() {
        List<TipoEvento> tipi = eventoClinicoDao.getTipiEventi();
        return ResponseEntity.ok(tipi != null ? tipi : Collections.emptyList());
    }

    @GetMapping("/categorie")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoEventoClinico>> getCategorie() {
        List<TipoEventoClinico> categorie = eventoClinicoDao.getTipiEventiClinici();
        return ResponseEntity.ok(categorie != null ? categorie : Collections.emptyList());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> save(@RequestBody EventoClinico evento) {
        try {
            evento.setId_evento(null); // forza insert
            evento.setDt_evento(normalizzaData(evento.getDt_evento()));
            evento.setDt_richiamo(normalizzaData(evento.getDt_richiamo()));
            String id = eventoClinicoDao.save(evento);
            return ResponseEntity.ok(Map.of("id", id, "messaggio", "Evento salvato"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PutMapping("/{idEvento}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable String idEvento,
            @RequestBody EventoClinico evento) {
        try {
            evento.setId_evento(idEvento);
            evento.setDt_evento(normalizzaData(evento.getDt_evento()));
            evento.setDt_richiamo(normalizzaData(evento.getDt_richiamo()));
            eventoClinicoDao.update(evento);
            return ResponseEntity.ok(Map.of("messaggio", "Evento aggiornato"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @DeleteMapping("/{idEvento}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String idEvento) {
        try {
            eventiCliniciService.deleteEventoClinicoAndDocuments(idEvento);
            return ResponseEntity.ok(Map.of("messaggio", "Evento eliminato"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    private String normalizzaData(String data) {
        if (data == null || data.isEmpty()) return data;
        try {
            if (data.contains("T")) {
                LocalDate date = Instant.parse(data)
                        .atZone(ZoneId.of("Europe/Rome"))
                        .toLocalDate();
                return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return data;
        } catch (Exception e) {
            return data;
        }
    }

    @PostMapping(value = "/{idEvento}/documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> uploadDocumento(
            @PathVariable String idEvento,
            @RequestParam("idAnimale") String idAnimale,
            @RequestParam("documento") String documentoJson,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserAuth user) throws Exception {
        documentoService.saveDocumentoEventoClinico(documentoJson, idEvento, idAnimale, file, user);
        return ResponseEntity.ok(Map.of("messaggio", "Documento caricato"));
    }
}
