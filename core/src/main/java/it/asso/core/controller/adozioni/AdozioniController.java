package it.asso.core.controller.adozioni;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.adozioni.AdozioniDAO;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.model.adozioni.*;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.security.UserAuth;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.sql.SQLIntegrityConstraintViolationException; // Necessario se i DAO lanciano questa eccezione

// import org.json.JSONObject; // Rimosso l'uso diretto

@Controller
@RequestMapping("/jsp/private/adozioni") // Path base per pulizia
public class AdozioniController { // Non estende più BaseController

    // Dipendenze finali (Iniezione tramite Costruttore)
    private final AdozioniDAO adozioniDao;
    private final AnimaleDAO animaleDao;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public AdozioniController(AdozioniDAO adozioniDao, AnimaleDAO animaleDao) {
        this.adozioniDao = adozioniDao;
        this.animaleDao = animaleDao;
    }


    /*-------------------------------------- ADOTTANTI (LETTURA) --------------------------------------------------------*/

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAdottanti.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Adottante> getAdottanti(@RequestParam String strToSearch) throws AssoServiceException {
        // La logica di popolamento deve essere spostata in un Service Layer
        List<Adottante> a = adozioniDao.getAdottanti(strToSearch);
        for(Adottante ad : a) {
            ad.setAdottati(adozioniDao.getAdottabiliByIdAdottante(ad.getId_adottante()));
        }
        return a;
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAdottantiDisponibili.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Adottante> getAdottantiDisponibili(@RequestParam String strToSearch) throws AssoServiceException {
        return adozioniDao.getAdottantiDisponibili(strToSearch);
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getTotaliAdottanti.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody TotaleAdottanti getTotaliAdottanti() throws AssoServiceException {
        return adozioniDao.getTotaliAdottanti();
    }

    // CHIAMATA DI SCRITTURA (Usa @PreAuthorize e ResponseEntity)
    @RequestMapping(value = "/saveOrUpdateAdottante.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ADOZIONI')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateAdottante(@RequestBody Adottante adottante, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        adottante.setAccount(user.getUsername());
        String idAdottante = adozioniDao.saveOrUpdateAdottante(adottante);
        // Restituisce l'ID del nuovo adottante come stringa JSON valida
        return ResponseEntity.ok().body("\"" + idAdottante + "\"");
    }


    /*-------------------------------------- ADOTTABILI (LETTURA) --------------------------------------------------------*/

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAdottabili.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Adottabile> getAdottabili(@RequestParam String strToSearch) throws AssoServiceException {
        // Logica di popolamento da spostare in Service
        List<Adottabile> adottabili = adozioniDao.getAdottabili(strToSearch);
        for (Adottabile a : adottabili) {
            a.setAdottanti(adozioniDao.getAdottantiByIdAdottabile(a.getId_adottabile()));
        }
        return adottabili;
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAdottabiliDisponibili.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Adottabile> getAdottabiliDisponibili(@RequestParam String strToSearch) throws AssoServiceException {
        return adozioniDao.getAdottabiliDisponibili(strToSearch);
    }

    // CHIAMATA DI SCRITTURA (Usa @PreAuthorize e ResponseEntity)
    @RequestMapping(value = "/saveOrUpdateAdottabile.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ADOZIONI')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateAdottabile(@RequestBody Adottabile adottabile, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        adottabile.setAccount(user.getUsername());
        String idAdottabile = adozioniDao.saveOrUpdateAdottabile(adottabile);
        return ResponseEntity.ok().body("\"" + idAdottabile + "\"");
    }

    // CHIAMATA DI SCRITTURA (Usa @PreAuthorize e ResponseEntity)
    @RequestMapping(value = "/updateAdozioneAttivazione.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ADOZIONI')")
    public @ResponseBody ResponseEntity<String> updateAdozioneAttivazione(@RequestBody Adozione adozione) throws AssoServiceException {
        String esito = adozioniDao.updateAdozioneAttivazione(adozione.getAttivo(), adozione.getId_adozione());
        return ResponseEntity.ok().body("\"" + esito + "\"");
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAnimaliAdottabili.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Animale> getAnimaliAdottabili(@RequestParam String strToSearch) throws AssoServiceException {
        List<Animale> animali = animaleDao.getAdottabiliBySearch(strToSearch);
        return animali;
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAdottantiWithAdozioni.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Adottante> getAdottantiWithAdozioni(@RequestParam String search) throws AssoServiceException {
        // Logica di popolamento da spostare in Service
        List<Adottante> l = adozioniDao.getAdottantiWithAdozioni(search);
        for (Adottante a : l) {
            a.setAdottati(adozioniDao.getAdottabiliByID(a.getId_adottante()));
            for(Adottabile adl : a.getAdottati()) {
                adl.setAdozione(adozioniDao.getAdozioneByAdottanteAndAdottabile(a.getId_adottante(), adl.getId_adottabile()));
            }
        }
        return l;
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAdozioneByAdottanteAndAdottabile.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody ResponseEntity<String> getAdozioneByAdottanteAndAdottabile(@RequestParam String idAdottante, @RequestParam String idAdottabile) throws AssoServiceException {
        Adozione a = adozioniDao.getAdozioneByAdottanteAndAdottabile(idAdottante, idAdottabile);
        if(a == null) {
            return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
        }else {
            return ResponseEntity.ok().body("\"" + Def.STR_KO + "\"");
        }
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getAdottanteWithAdozione.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Adottante getAdottanteWithAdozione(@RequestParam String id) throws AssoServiceException {
        // Logica di popolamento da spostare in Service
        Adottante l = adozioniDao.getAdottanteWithAdozione(id);
        l.setAdottati(adozioniDao.getAdottabiliByID(l.getId_adottante()));
        for(Adottabile adl : l.getAdottati()) {
            adl.setAdozione(adozioniDao.getAdozioneByAdottanteAndAdottabile(l.getId_adottante(), adl.getId_adottabile()));
        }

        return l;
    }

    // CHIAMATA DI SCRITTURA (Usa @PreAuthorize e ResponseEntity)
    @RequestMapping(value = "/saveOrUpdate.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ADOZIONI')")
    public @ResponseBody ResponseEntity<String> save(@RequestBody Adozione adozione, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        adozione.setAccount(user.getUsername());
        String idAdozione = adozioniDao.saveOrUpdate(adozione);
        return ResponseEntity.ok().body("\"" + idAdozione + "\"");
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/animale/getVersamentiByAdozione.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Versamento> getVersamentiByAdozione(@RequestParam String idAdozione) throws AssoServiceException {
        List<Versamento> a =  adozioniDao.getVersamentiByAdozione(idAdozione);
        return a;
    }

    /*---------------------------------------------------------------------------------------------------------*/

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/getCalendarioByAnno.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Calendario> getCalendarioByAnno(@RequestParam String anno) throws AssoServiceException {
        List<Calendario> result = adozioniDao.getCalendario(anno);
        return result;
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/animale/getAdozioneById.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Adozione getAnimaleById(@RequestParam String id) throws AssoServiceException {
        Adozione a = adozioniDao.getAdozioneByID(id);
        a.setAdottanti(adozioniDao.getAdottantiByID(a.getId_adozione()));
        return a;
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/animale/getVersamentiByAdottante.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody List<Versamento> getVersamentiByAdottante(@RequestParam String id) throws AssoServiceException {
        List<Versamento> a =  adozioniDao.getVersamentiByAdottante(id);
        return a;
    }

    // SOLA AUTENTICAZIONE
    @RequestMapping(value = "/animale/getVersamentiById.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody Versamento getVersamentiById(@RequestParam String id) throws AssoServiceException {
        Versamento a =  adozioniDao.getVersamentiById(id);
        return a;
    }

    // CHIAMATA DI SCRITTURA (Usa @PreAuthorize e ResponseEntity)
    @RequestMapping(value = "/deleteAdottante.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ADOZIONI')")
    public @ResponseBody ResponseEntity<String> deleteAdottante(@RequestParam String id) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        String result = adozioniDao.deleteByID(id);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // CHIAMATA DI SCRITTURA (Usa @PreAuthorize e ResponseEntity)
    @RequestMapping(value = "/saveOrUpdateVersamento.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ADOZIONI')")
    public @ResponseBody ResponseEntity<String> saveOrUpdateVersamento(@RequestBody Versamento versamento, @AuthenticationPrincipal UserAuth user) throws AssoServiceException {
        versamento.setAccount(user.getUsername());
        String result = adozioniDao.saveOrUpdateVersamento(versamento);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // CHIAMATA DI SCRITTURA (Usa @PreAuthorize e ResponseEntity)
    @RequestMapping(value = "/deleteVersamento.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ADOZIONI')")
    public @ResponseBody ResponseEntity<String> deleteVersamento(@RequestParam String id) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        String result = adozioniDao.deleteVersamentoByID(id);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

}