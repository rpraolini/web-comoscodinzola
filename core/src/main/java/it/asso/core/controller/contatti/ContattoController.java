package it.asso.core.controller.contatti;

import it.asso.core.model.contatto.ContattoAutocompleteDTO;
import it.asso.core.service.ContattoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/private/contatti")
public class ContattoController {

    private final ContattoService contattoService;

    public ContattoController(ContattoService contattoService) {
        this.contattoService = contattoService;
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContattoAutocompleteDTO>> autocomplete(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "") String idTipoEvento) {
        return ResponseEntity.ok(contattoService.cercaPerAutocomplete(query, idTipoEvento));
    }
}
