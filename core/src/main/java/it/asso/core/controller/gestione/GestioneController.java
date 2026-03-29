package it.asso.core.controller.gestione;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.gestione.ProcessoDAO;
import it.asso.core.dao.gestione.AttivitaDAO;
import it.asso.core.dao.gestione.PraticaDAO;
import it.asso.core.dao.utente.UtenteDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.gestione.Attivita;
import it.asso.core.model.gestione.Pratica;
import it.asso.core.security.UserAuth;
import it.asso.core.service.GestioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jsp/private/gestione")
public class GestioneController {

    // 1. INIEZIONE DEL SERVICE E DEI DAO DI SCRITTURA
    private final GestioneService gestioneService;
    private final PraticaDAO praticaDao;
    private final AttivitaDAO attivitaDao;
    private final UtenteDAO utenteDAO;
    private final AnimaleDAO animaleDao;
    private final ProcessoDAO processoDao;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public GestioneController(GestioneService gestioneService, PraticaDAO praticaDao, AttivitaDAO attivitaDao, UtenteDAO utenteDAO, AnimaleDAO animaleDao, ProcessoDAO processoDao) {
        this.gestioneService = gestioneService;
        this.praticaDao = praticaDao;
        this.attivitaDao = attivitaDao;
        this.utenteDAO = utenteDAO;
        this.animaleDao = animaleDao;
        this.processoDao = processoDao;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (DELEGATI AL SERVICE PER LA LOGICA DI POPOLAMENTO)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getPraticheByAnimale.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Pratica> getPraticheByAnimale(@RequestParam String id) throws AssoServiceException {
        return gestioneService.getPraticheByAnimale(id);
    }

    @RequestMapping(value = "/getIterByID.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Iter getIterByID(@RequestParam String id) throws AssoServiceException {
        // La logica di popolamento è nel Service
        return gestioneService.getIterById(id);
    }

    // ------------------------------------------------------------------------
    // CHIAMATE DI SCRITTURA (UTILIZZANO @PreAuthorize E ResponseEntity)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/closePratica.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.hasWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> closePratica(@RequestParam String id, @RequestParam String idAnimale, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        praticaDao.close(id);
        Animale a = animaleDao.getById(idAnimale);

        if (Def.ST_ISTRUTTORIA_CHIUSA.equals(a.getId_stato())) {
            animaleDao.updateProprietario(idAnimale, id);
        }

        Attivita attivita = new Attivita();
        attivita.setAccount(user.getUsername());
        attivita.setId_pratica(id);
        attivita.setId_attivita_p(Def.ATT_P_CHIUSURA_PRATICA);
        attivita.setId_stato_padre(Def.ST_P_PRATICA_CHIUSA);
        attivita.setId_utente(String.valueOf(utenteDAO.findIDByAccount(user.getUsername())));
        attivitaDao.saveOrUpdate(attivita);

        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/deletePratica.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.hasWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> deletePratica(@RequestParam String id) throws AssoServiceException {
        praticaDao.deleteByID(id);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/saveOrUpdate.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.hasWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> savePratica(@RequestBody Pratica pratica, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String idPratica = praticaDao.getExistPraticaAttivaByIDAnimale(pratica.getId_animale());
        if(idPratica != null) {
            throw new AssoServiceException(Def.STR_ERROR_701 + " " + idPratica);
        }

        pratica.setAccount(user.getUsername());
        pratica.setId_stato(Def.ST_P_PRATICA_APERTA);
        idPratica = praticaDao.saveOrUpdate(pratica);

        Attivita attivita = new Attivita();
        attivita.setAccount(pratica.getAccount());
        attivita.setId_pratica(idPratica);
        attivita.setId_attivita_p(Def.ATT_P_APERTURA_PRATICA);
        attivita.setId_stato_padre(Def.ST_P_PRATICA_APERTA);
        attivita.setId_utente(String.valueOf(utenteDAO.findIDByAccount(pratica.getAccount())));
        attivitaDao.saveOrUpdate(attivita);

        Animale animale = animaleDao.getById(pratica.getId_animale());
        if(Integer.valueOf(animale.getId_stato()) > Integer.valueOf(Def.ST_ADOTTABILE)) {
            processoDao.setStato(animale.getId_animale(), Def.ST_VALIDA);
        }

        return ResponseEntity.ok().body("\"" + idPratica + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateRichiestaPreaffido.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateRichiestaPreaffido(@RequestBody Iter iter, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String result = gestioneService.saveOrUpdateRichiestaPreaffido(iter, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateAdozione.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateAdozione(@RequestBody Iter iter, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String result = gestioneService.saveOrUpdateAdozione(iter, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateConsegna.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateConsegna(@RequestBody Iter iter, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String result = gestioneService.saveOrUpdateConsegna(iter, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdatePassaggio.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> saveOrUpdatePassaggio(@RequestBody Iter iter, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String result = gestioneService.saveOrUpdatePassaggio(iter, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateChiusura.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateChiusura(@RequestBody Iter iter, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String result = gestioneService.saveOrUpdateChiusura(iter, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }


    @RequestMapping(value = "/delIterById.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')")
    public @ResponseBody ResponseEntity<String> delIterById(@RequestParam String id, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        String result = gestioneService.deleteIterById(id, user);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }
}