package it.asso.core.controller.animali;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.tag.TagDAO;
import it.asso.core.model.tag.Tag;

import it.asso.core.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Controller
@RequestMapping("/api/private/tags") // Path base
public class TagController { // Non estende BaseController

    private final TagDAO tagDao;
    public final TagService tagService;

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String WRITE_ANIMALE = "@authz.checkWritePermission('AREA_DATI_ANIMALE')";
    private static final String WRITE_TABELLE = "@authz.checkWritePermission('AREA_DATI_TABELLE')";

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public TagController(TagDAO tagDao, TagService tagService) {
        this.tagDao = tagDao;
        this.tagService = tagService;
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (TAG PER ANIMALE)
    // ------------------------------------------------------------------------

    @PostMapping("/{idAnimale}/tags/{idTag}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> assignTag(@PathVariable String idAnimale, @PathVariable String idTag) throws SQLIntegrityConstraintViolationException {
        tagService.assignTag(idAnimale, idTag);
        return ResponseEntity.ok().body("\"" + Def.STR_SUCCESS + "\"");
    }

    @DeleteMapping("/{idAnimale}/tags/{idTag}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> removeTag(@PathVariable String idAnimale, @PathVariable String idTag) {
        tagService.removeTag(idAnimale, idTag);
        return ResponseEntity.ok().body("\"" + Def.STR_SUCCESS + "\"");
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA
    // ------------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Tag>> getTagByIDAnimale(@PathVariable("id") String id) throws AssoServiceException {
        return ResponseEntity.ok(tagService.getTagsByAnimale(id));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Tag>> getTags() throws AssoServiceException {
        return ResponseEntity.ok(tagService.getTags());
    }


    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (TAG DI CONFIGURAZIONE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/deleteTagById.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_TABELLE) // Sostituisce checkWritePermission(Def.AREA_DATI_TABELLE)
    public @ResponseBody ResponseEntity<String> deleteTagById(@RequestParam String idTag) throws AssoServiceException {
        try {
            String result = tagDao.deleteByID(idTag);
            return ResponseEntity.ok().body("\"" + result + "\"");
        } catch (SQLIntegrityConstraintViolationException | DataIntegrityViolationException e) {
            // Utilizziamo DataIntegrityViolationException per catturare l'errore generico di Spring
            throw new AssoServiceException(Def.STR_ERROR_501);
        }
    }

    @RequestMapping(value = "/saveOrUpdate.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_TABELLE) // Sostituisce checkWritePermission(Def.AREA_DATI_TABELLE)
    public @ResponseBody ResponseEntity<Tag> saveOrUpdate(@RequestBody Tag tag) throws AssoServiceException {
        try {
            Tag result = tagDao.saveOrUpdate(tag);
            return ResponseEntity.ok(result); // Restituisce l'oggetto Tag aggiornato (JSON)
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        }
    }
}