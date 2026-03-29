package it.asso.core.controller.animali;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.CaratteriDAO;
import it.asso.core.dao.animali.animale.ColoreDAO;
import it.asso.core.dao.animali.animale.RazzaDAO;
import it.asso.core.dao.animali.attivita.AttivitaDAO;
import it.asso.core.dao.animali.salute.EventoClinicoDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.raccolta.MerceDAO;
import it.asso.core.dao.tag.TagDAO;
import it.asso.core.model.animali.animale.*;
import it.asso.core.model.animali.attivita.Attivita;
import it.asso.core.model.animali.attivita.Stato;
import it.asso.core.model.animali.salute.TipoEvento;
import it.asso.core.model.animali.salute.TipoEventoClinico;
import it.asso.core.model.contatto.Qualifica;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;
import it.asso.core.model.raccolta.Merce;
import it.asso.core.model.tag.Tag;

import it.asso.core.security.UserAuth;
import it.asso.core.service.AnimaleService;
import it.asso.core.service.AttivitaService;
import it.asso.core.service.ProcessoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

@RestController
@RequestMapping("/api/private/animali")
public class AnimaliController {

    private final CaratteriDAO carattereDao;
    private final AttivitaDAO attivitaDao;
    private final ColoreDAO coloreDao;
    private final EventoClinicoDAO eventoClinicoDao;
    private final RazzaDAO razzaDao;
    private final TagDAO tagDao;
    private final MerceDAO merceDao;
    private final ContattoDAO contattoDao;
    private final AnimaleService animaleService;
    private final AttivitaService attivitaService;
    private final ProcessoService  processoService;
    // VARIABILE PER L'AUTORIZZAZIONE (AREA_DATI_TABELLE)
    private static final String WRITE_PERMISSION = "hasRole('AMMINISTRATORE') and @authz.hasWritePermission('AREA_DATI_TABELLE')";

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public AnimaliController(CaratteriDAO carattereDao, ColoreDAO coloreDao, EventoClinicoDAO eventoClinicoDao, RazzaDAO razzaDao, TagDAO tagDao, MerceDAO merceDao, ContattoDAO contattoDao, AnimaleService animaleService, AttivitaDAO attivitaDao, AttivitaService attivitaService,  ProcessoService processoService) {
        this.carattereDao = carattereDao;
        this.coloreDao = coloreDao;
        this.eventoClinicoDao = eventoClinicoDao;
        this.razzaDao = razzaDao;
        this.tagDao = tagDao;
        this.merceDao = merceDao;
        this.contattoDao = contattoDao;
        this.animaleService = animaleService;
        this.attivitaDao = attivitaDao;
        this.attivitaService = attivitaService;
        this.processoService = processoService;
    }

    // ------------------------------------------------------------------------
    // NUOVI METODI
    // ------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<Animale> getById(@PathVariable("id") String id) {
        // Cerchiamo l'animale tramite il service
        Animale animale = animaleService.findById(id);

        if (animale != null) {
            return ResponseEntity.ok(animale);
        } else {
            // Se l'ID non esiste, restituiamo un 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Proteggiamo la rotta
    public ResponseEntity<List<Animale>> getTuttiGliAnimali() {
        // Chiamiamo il DAO per ottenere gli ultimi 50 inseriti (passa il filtro adeguato, es. "1")
        List<Animale> animaliVeri = animaleService.getUltimiAnimaliInseriti(50, "1");
        return ResponseEntity.ok(animaliVeri);
    }

    @PostMapping("/ricerca")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Animale>> ricercaAnimali(@RequestBody RicercaDTO criteri) {
        // Il tuo DAO ha già tutta la logica SQL complessa dentro getRicerca!
        List<Animale> risultati = animaleService.getRicerca(criteri);
        return ResponseEntity.ok(risultati);
    }

    @PostMapping("/ricerca-completa")
    public ResponseEntity<Map<String, Object>> ricercaCompleta(@RequestBody RicercaDTO criteri) {
        List<Animale> lista = animaleService.getRicerca(criteri);
        int totale = animaleService.getCountBySearch(criteri);

        return ResponseEntity.ok(Map.of(
                "animali", lista,
                "totale", totale
        ));
    }

    @GetMapping("/stati")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Stato>> geStati() throws AssoServiceException {
        return ResponseEntity.ok(attivitaDao.getStati());
    }

    @GetMapping("/regioni/{nazione}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Regione>> getRegioni(@PathVariable("nazione") String nazione) throws AssoServiceException {
        return ResponseEntity.ok(animaleService.getRegioni(nazione));
    }
    @GetMapping(value = "/province/{idRegione}")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Provincia> getProvinceByRegione(@PathVariable("idRegione") String idRegione) throws AssoServiceException {
        return animaleService.getProvinceByRegioneUsed(idRegione);
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (SAVE OR UPDATE)
    // ------------------------------------------------------------------------

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> save(@RequestBody Animale nuovo, @AuthenticationPrincipal UserAuth user) {
        try {
            System.out.println("Principal Type: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass().getName());
            // Chiamata al metodo save del tuo service che già possiedi
            String nuovoId = animaleService.inserisciNuovoAnimale(nuovo);

            if(nuovoId != null && !nuovoId.isEmpty()) {
                Attivita attivita = new Attivita();
                attivita.setId_animale(nuovoId);
                attivita.setId_attivita(Def.ATT_PRIMO_INSERIMENTO);
                attivita.setNote_attivita(Def.ATT_PRIMO_INSERIMENTO_DESCR);
                attivita.setId_utente(user.getUtente().getId_utente());
                attivita.setAccount(user.getUsername());
                attivitaService.save(attivita);
            }

            return ResponseEntity.ok(Map.of(
                    "id", nuovoId,
                    "messaggio", "Salvataggio completato con successo"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable("id") String id,
            @RequestBody Animale animale,
            @AuthenticationPrincipal UserAuth user) {
        try {
            String idAggiornato = animaleService.aggiorna(id, animale);

            return ResponseEntity.ok(Map.of(
                    "id", idAggiornato,
                    "messaggio", "Aggiornamento completato con successo"
            ));
        } catch (AssoServiceException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("errore", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> elimina(@PathVariable("id") String id) {
        try {
            animaleService.elimina(id);
            return ResponseEntity.ok(Map.of("messaggio", "Animale eliminato con successo"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PostMapping("/{id}/valida")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> valida(
            @PathVariable String id,
            @AuthenticationPrincipal UserAuth user) {
        try {
            String result = processoService.validaAnimale(id, user);
            if (Def.STR_OK.equals(result)) {
                return ResponseEntity.ok(Map.of("messaggio", "Animale validato con successo"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("errore", "Inserire almeno un evento storico di origine prima di validare"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PostMapping("/{id}/adottabile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> adottabile(
            @PathVariable String id,
            @AuthenticationPrincipal UserAuth user) {
        try {
            String result = processoService.adottabileAnimale(id, user);
            return ResponseEntity.ok(Map.of("messaggio", "Animale reso adottabile"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }

    @PostMapping("/{id}/revoca-adottabile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> revocaAdottabile(
            @PathVariable String id,
            @AuthenticationPrincipal UserAuth user) {
        try {
            processoService.revocaAdottabile(id, user);
            return ResponseEntity.ok(Map.of("messaggio", "Adottabilità revocata"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errore", e.getMessage()));
        }
    }



    @RequestMapping(value = "/saveOrUpdateColore.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateColore(@RequestBody Colore colore) throws AssoServiceException {
        try {
            String result = coloreDao.saveOrUpdate(colore);
            return ResponseEntity.ok().body("\"" + result + "\"");
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveOrUpdateCarattere.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateCarattere(@RequestBody Carattere carattere) {
        String result = carattereDao.saveOrUpdateTipoCarattere(carattere);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateEventoClinico.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateEventoClinico(@RequestBody TipoEvento evento) throws AssoServiceException {
        try {
            String result = eventoClinicoDao.saveOrUpdate(evento);
            return ResponseEntity.ok().body("\"" + result + "\"");
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveOrUpdateRazza.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateRazza(@RequestBody Razza razza) throws AssoServiceException {
        try {
            String result = razzaDao.saveOrUpdate(razza);
            return ResponseEntity.ok().body("\"" + result + "\"");
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveOrUpdateMerce.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateMerce(@RequestBody Merce merce) throws AssoServiceException {
        try {
            merceDao.saveOrUpdate(merce);
            return ResponseEntity.ok().body("\"OK\"");
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssoServiceException(Def.STR_ERROR_500);
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @RequestMapping(value = "/saveOrUpdateTag.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_PERMISSION)
    public @ResponseBody ResponseEntity<String> saveOrUpdateTag(@RequestBody Tag tag) throws AssoServiceException {
        try {
            tagDao.saveOrUpdate(tag);
            return ResponseEntity.ok().body("\"OK\"");
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
            return ResponseEntity.ok().body("\"OK\"");
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
        // Logica di rimozione (migrata alla lambda)
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