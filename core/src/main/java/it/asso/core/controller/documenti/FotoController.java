package it.asso.core.controller.documenti;

import it.asso.core.model.documenti.Foto;
import it.asso.core.security.UserAuth;
import it.asso.core.service.AnimaleService;
import it.asso.core.service.FotoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/private/foto")
public class FotoController {

    private final FotoService fotoService;
    private final AnimaleService animaleService;

    public FotoController(FotoService fotoService, AnimaleService animaleService) {
        this.fotoService = fotoService;
        this.animaleService = animaleService;
    }

    @GetMapping("/{idAnimale}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Foto>> getFoto(
            @PathVariable String idAnimale,
            @AuthenticationPrincipal UserAuth user) {
        String tenant = animaleService.resolveTenantFromRequest(
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest());
        return ResponseEntity.ok(fotoService.getFotoByAnimale(idAnimale, tenant));
    }

    @PostMapping(value = "/{idAnimale}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> upload(
            @PathVariable String idAnimale,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String didascalia,
            @RequestParam(required = false) String idTipoFoto,
            @RequestParam(required = false, defaultValue = "0") String pubblica, // ← nuovo
            @AuthenticationPrincipal UserAuth user) {
        try {
            String tenant = animaleService.resolveTenantFromRequest(
                    ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest());
            fotoService.uploadFoto(idAnimale, file, didascalia, idTipoFoto, pubblica, tenant, user.getUsername());
            return ResponseEntity.ok(Map.of("messaggio", "Foto caricata con successo"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PutMapping("/{idFoto}/pubblica")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> togglePubblica(
            @PathVariable String idFoto,
            @RequestBody Map<String, String> body) {
        try {
            fotoService.togglePubblica(idFoto, body.get("pubblica"));
            return ResponseEntity.ok(Map.of("messaggio", "Stato aggiornato"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @DeleteMapping("/{idFoto}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String idFoto) {
        try {
            fotoService.deleteFoto(idFoto);
            return ResponseEntity.ok(Map.of("messaggio", "Foto eliminata"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PutMapping("/{idFoto}/profilo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> impostaProfilo(@PathVariable String idFoto) {
        fotoService.impostaComeProfilo(idFoto);
        return ResponseEntity.ok(Map.of("messaggio", "Foto profilo aggiornata"));
    }

    @GetMapping("/serve/{idFoto}")
    public void serveImmagine(@PathVariable String idFoto, HttpServletResponse response) throws IOException {
        fotoService.serveImmagine(idFoto, response);
    }
}
