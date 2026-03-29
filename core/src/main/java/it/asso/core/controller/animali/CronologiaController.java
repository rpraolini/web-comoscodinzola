package it.asso.core.controller.animali;

import it.asso.core.dto.proprietario.ProprietarioDTO;
import it.asso.core.model.animali.attivita.Attivita;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.service.CronologiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/private/cronologia")
public class CronologiaController {

    private final CronologiaService cronologiaService;

    public CronologiaController(CronologiaService cronologiaService) {
        this.cronologiaService = cronologiaService;
    }

    @GetMapping("/{idAnimale}/attivita")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Attivita>> getAttivita(@PathVariable String idAnimale) {
        return ResponseEntity.ok(cronologiaService.getAttivita(idAnimale));
    }

    @GetMapping("/{idAnimale}/proprietario")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProprietarioDTO> getProprietario(@PathVariable String idAnimale) {
        return ResponseEntity.ok(cronologiaService.getProprietario(idAnimale));
    }
}
