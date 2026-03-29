package it.asso.core.controller.animali;

import it.asso.core.model.animali.animale.Caratteri;
import it.asso.core.model.animali.animale.TipoCarattere;
import it.asso.core.service.CarattereService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/carattere")
public class CarattereController {

    private final CarattereService carattereService;

    public CarattereController(CarattereService carattereService) {
        this.carattereService = carattereService;
    }

    @GetMapping("/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Caratteri>> getCaratteriByAnimale(@PathVariable String idAnimale) {
        return ResponseEntity.ok(carattereService.getCaratteriByAnimale(idAnimale));
    }

    @GetMapping("/tipi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TipoCarattere>> getTipiCarattere() {
        return ResponseEntity.ok(carattereService.getTipiCarattere());
    }

    @PostMapping("/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> aggiungi(
            @PathVariable String idAnimale,
            @RequestBody Caratteri carattere) {
        String id = carattereService.aggiungi(idAnimale, carattere);
        return ResponseEntity.ok(Map.of("id", id, "messaggio", "Carattere aggiunto"));
    }

    @PutMapping("/{idCaratteri}/note")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> aggiornaNota(
            @PathVariable String idCaratteri,
            @RequestBody Map<String, String> body) {
        carattereService.aggiornaNota(idCaratteri, body.get("id_carattere"), body.get("note"));
        return ResponseEntity.ok(Map.of("messaggio", "Nota aggiornata"));
    }

    @DeleteMapping("/{idCaratteri}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> elimina(@PathVariable String idCaratteri) {
        carattereService.elimina(idCaratteri);
        return ResponseEntity.ok(Map.of("messaggio", "Carattere rimosso"));
    }
}