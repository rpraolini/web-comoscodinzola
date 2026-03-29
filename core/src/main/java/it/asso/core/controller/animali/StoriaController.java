package it.asso.core.controller.animali;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.storia.EventoStoricoDAO;
import it.asso.core.model.animali.storia.EventoStorico;
import it.asso.core.model.animali.storia.TipoEventoStorico;
import it.asso.core.security.UserAuth;
import it.asso.core.service.EventiStoriciService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/storia")
public class StoriaController {

    private final EventiStoriciService eventiStoriciService;
    private final EventoStoricoDAO eventoStoricoDao;

    public StoriaController(EventiStoriciService eventiStoriciService, EventoStoricoDAO eventoStoricoDao) {
        this.eventiStoriciService = eventiStoriciService;
        this.eventoStoricoDao = eventoStoricoDao;
    }

    @GetMapping("/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventoStorico>> getEventi(@PathVariable String idAnimale) throws AssoServiceException {
        return ResponseEntity.ok(eventiStoriciService.getEventiStoriciByIdAnimale(idAnimale));
    }

    @GetMapping("/tipi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoEventoStorico>> getTipi() {
        List<TipoEventoStorico> tipi = eventoStoricoDao.getTipiEventiStorici();
        return ResponseEntity.ok(tipi != null ? tipi : Collections.emptyList());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> save(
            @RequestBody EventoStorico evento,
            @AuthenticationPrincipal UserAuth user) {
        try {
            // Normalizzazione date ISO 8601 → dd/MM/yyyy
            evento.setDt_da(normalizzaData(evento.getDt_da()));
            evento.setDt_a(normalizzaData(evento.getDt_a()));
            String id = eventiStoriciService.saveOrUpdateEventoStorico(evento, user);
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
            @RequestBody EventoStorico evento,
            @AuthenticationPrincipal UserAuth user) {
        try {
            evento.setId_evento(idEvento);
            evento.setDt_da(normalizzaData(evento.getDt_da()));
            evento.setDt_a(normalizzaData(evento.getDt_a()));
            String id = eventiStoriciService.saveOrUpdateEventoStorico(evento, user);
            return ResponseEntity.ok(Map.of("id", id, "messaggio", "Evento aggiornato"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @DeleteMapping("/{idEvento}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String idEvento) {
        eventoStoricoDao.delete(idEvento);
        return ResponseEntity.ok(Map.of("messaggio", "Evento eliminato"));
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
}
