package it.asso.core.controller.anagrafiche;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.CaratteriDAO;
import it.asso.core.dao.animali.animale.ColoreDAO;
import it.asso.core.dao.animali.animale.RazzaDAO;
import it.asso.core.dao.animali.salute.EventoClinicoDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.raccolta.MerceDAO;
import it.asso.core.dao.tag.TagDAO;
import it.asso.core.model.animali.animale.Carattere;
import it.asso.core.model.animali.animale.Colore;
import it.asso.core.model.animali.animale.Razza;
import it.asso.core.model.animali.animale.TipoCarattere;
import it.asso.core.model.animali.salute.TipoEvento;
import it.asso.core.model.animali.salute.TipoEventoClinico;
import it.asso.core.model.contatto.Qualifica;
import it.asso.core.model.raccolta.Merce;
import it.asso.core.model.tag.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.sql.SQLIntegrityConstraintViolationException;

@Controller
@RequestMapping("/jsp/private/anagrafiche")
public class AnagraficheController { // Non estende più BaseController

    // Dipendenze finali (Iniezione tramite Costruttore)
    private final CaratteriDAO carattereDao;
    private final ColoreDAO coloreDao;
    private final EventoClinicoDAO eventoClinicoDao;
    private final RazzaDAO razzaDao;
    private final TagDAO tagDao;
    private final MerceDAO merceDao;
    private final ContattoDAO contattoDao;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE (Sostituisce @Autowired sui campi)
    public AnagraficheController(CaratteriDAO carattereDao, ColoreDAO coloreDao, EventoClinicoDAO eventoClinicoDao, RazzaDAO razzaDao, TagDAO tagDao, MerceDAO merceDao, ContattoDAO contattoDao) {
        this.carattereDao = carattereDao;
        this.coloreDao = coloreDao;
        this.eventoClinicoDao = eventoClinicoDao;
        this.razzaDao = razzaDao;
        this.tagDao = tagDao;
        this.merceDao = merceDao;
        this.contattoDao = contattoDao;
    }

    // --- UTILITY PER LA SICUREZZA ---
    private static final String WRITE_PERMISSION = "hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_TABELLE')";

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (POST)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdateColore.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateColore(@RequestBody Colore colore) throws AssoServiceException {
        String result = null;
        try {
            result = coloreDao.saveOrUpdate(colore);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500); // 500 è il codice legacy per violazione di integrità
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateCarattere.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateCarattere(@RequestBody Carattere carattere) throws AssoServiceException {
        String result = carattereDao.saveOrUpdateTipoCarattere(carattere);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateEventoClinico.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateEventoClinico(@RequestBody TipoEvento evento) throws AssoServiceException {
        String result = null;
        try {
            result = eventoClinicoDao.saveOrUpdate(evento);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateRazza.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateRazza(@RequestBody Razza razza) throws AssoServiceException {
        String result = null;
        try {
            result = razzaDao.saveOrUpdate(razza);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateMerce.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateMerce(@RequestBody Merce merce) throws AssoServiceException {
        try {
            merceDao.saveOrUpdate(merce);
            return ResponseEntity.ok().body("\"OK\""); // Assumendo che il risultato sia OK
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveOrUpdateTag.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateTag(@RequestBody Tag tag) throws AssoServiceException {
        String result = null;
        try {
            tagDao.saveOrUpdate(tag);
            return ResponseEntity.ok().body("\"OK\""); // Assumendo che il risultato sia OK
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveOrUpdateQualifica.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateQualifica(@RequestBody Qualifica qualifica) throws AssoServiceException {
        try {
            contattoDao.saveOrUpdateQualifica(qualifica);
            return ResponseEntity.ok().body("\"OK\""); // Assumendo che il risultato sia OK
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (GET)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getColori.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Colore> getColori() throws AssoServiceException {
        return coloreDao.getColori();
    }

    @RequestMapping(value = "/getCaratteri.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Carattere> getCaratteri() throws AssoServiceException {
        List<Carattere> c = carattereDao.getTipoCaratteri();
        // Logica di rimozione mantenuta, ma sarebbe meglio filtrarla nel DAO/Service
        c.removeIf(carattere -> Def.NUM_ZERO.equals(carattere.getId_carattere()));
        return c;
    }

    @RequestMapping(value = "/getTipoCarattere.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<TipoCarattere> getTipoCarattere() throws AssoServiceException {
        List<TipoCarattere> tipoCaratteri = carattereDao.getTipoCarattere();
        // Logica di popolamento mantenuta (da spostare in Service)
        for (TipoCarattere tipoCarattere : tipoCaratteri) {
            tipoCarattere.setCaratteri(carattereDao.getCaratteriByTipo(tipoCarattere.getId_tipo_carattere()));
        }
        return tipoCaratteri;
    }

    @RequestMapping(value = "/getEventiCliniciByTipo.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<TipoEvento> getEventiCliniciByTipo(@RequestParam String id) throws AssoServiceException {
        return eventoClinicoDao.getTipiEventi(id);
    }

    @RequestMapping(value = "/getEventiClinici.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<TipoEvento> getEventiClinici() throws AssoServiceException {
        return eventoClinicoDao.getTipiEventi();
    }

    @RequestMapping(value = "/getTipiEventiClinici.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<TipoEventoClinico> getTipiEventiClinici() throws AssoServiceException {
        return eventoClinicoDao.getTipiEventiClinici();
    }

    @RequestMapping(value = "/getRazze.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Razza> getRazze() throws AssoServiceException {
        return razzaDao.getRazze();
    }

    @RequestMapping(value = "/getTags.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Tag> getTags() throws AssoServiceException {
        return tagDao.getTags();
    }

    @RequestMapping(value = "/getMerci.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Merce> getMerci() throws AssoServiceException {
        return merceDao.getMerci();
    }

    @RequestMapping(value = "/getQualifiche.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Qualifica> getQualifiche() throws AssoServiceException {
        return contattoDao.getQualifiche();
    }

    // ------------------------------------------------------------------------
    // METODI DI ELIMINAZIONE (DELETE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/deleteColore.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteColore(@RequestParam String id) throws AssoServiceException {
        try {
            coloreDao.deleteByID(id);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteCarattere.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteCarattere(@RequestParam String id) throws AssoServiceException {
        try {
            carattereDao.deleteTipoCarattereByID(id);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteEventoClinico.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteTipoEventoClinico(@RequestParam String id) throws AssoServiceException {
        try {
            eventoClinicoDao.deleteTipoEvento(id);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteRazza.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteRazza(@RequestParam String id) throws AssoServiceException {
        try {
            razzaDao.deleteByID(id);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteTag.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteTag(@RequestParam String id) throws AssoServiceException {
        try {
            tagDao.deleteByID(id);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteMerce.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteMerce(@RequestParam String id) throws AssoServiceException {
        try {
            merceDao.deleteByID(id);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/deleteQualifica.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> deleteQualifica(@RequestParam String id) throws AssoServiceException {
        try {
            contattoDao.deleteQualificaByID(id);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (DataIntegrityViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_501);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}