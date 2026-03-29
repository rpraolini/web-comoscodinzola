package it.asso.core.controller.animali;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.CaratteriDAO;
import it.asso.core.model.animali.animale.Carattere;
import it.asso.core.model.animali.animale.Caratteri;
import it.asso.core.model.animali.animale.TipoCarattere;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jsp/private/carattere") // Definiamo un path base
public class CaratteriController { // Non estende BaseController

    // Iniezione tramite costruttore (sostituisce @Autowired)
    private final CaratteriDAO caratteriDao;

    public CaratteriController(CaratteriDAO caratteriDao) {
        this.caratteriDao = caratteriDao;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getCaratteri.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<Caratteri> getCaratteriByAnimale(@RequestParam String id) {
        return caratteriDao.getCaratteriByIdAnimale(id);
    }

    @RequestMapping(value = "/getTipoCaratteri.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<TipoCarattere> getTipoCaratteri() {
        List<TipoCarattere> tipoCaratteri =  caratteriDao.getTipoCarattere();
        // La logica di popolamento è mantenuta qui, ma andrebbe spostata in un Service
        for (TipoCarattere tipoCarattere : tipoCaratteri) {
            tipoCarattere.setCaratteri(caratteriDao.getCaratteriByTipo(tipoCarattere.getId_tipo_carattere()));
        }
        return tipoCaratteri;
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (UTILIZZANO @PreAuthorize E ResponseEntity)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdateCarattere.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')") // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> saveOrUpdate(@RequestBody List<Caratteri> caratteri) throws  AssoServiceException {

        for (Caratteri c : caratteri) {
            // Logica di business mantenuta nel Controller:
            if(c.getId_carattere() != null && !"".equals(c.getId_carattere())) {
                if( Def.NUM_ZERO.equals(c.getId_carattere()) && (c.getNote() == null || "".equals(c.getNote()))) {
                    caratteriDao.deleteByID(c.getId_caratteri());
                } else {
                    caratteriDao.saveOrUpdate(c);
                }
            } else {
                // Attenzione: questo 'if' è logicamente problematico nel codice originale:
                // L'ID del carattere è nullo, ma si usa c.getId_caratteri() (probabilmente ID riga)
                // Lo manteniamo come era, ma in una vera migrazione andrebbe rivisto:
                // if (c.getId_carattere() == null) {
                caratteriDao.deleteByID(c.getId_carattere());
            }
        }
        // Restituisce la risposta HTTP OK
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/delCarattereById.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')") // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> delCarattereById(@RequestParam String id) throws  AssoServiceException {
        String result = caratteriDao.deleteByID(id);
        // Restituisce la risposta HTTP con il risultato
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/saveOrUpdateCaratteri.json", method = RequestMethod.POST)
    @PreAuthorize("@authz.checkWritePermission('AREA_DATI_ANIMALE')") // Sostituisce checkWritePermission
    public @ResponseBody ResponseEntity<String> saveOrUpdateTipoCaratteri(@RequestBody Carattere carattere) throws  AssoServiceException {
        String result = caratteriDao.saveOrUpdateTipoCarattere(carattere);
        // Restituisce la risposta HTTP con il risultato
        return ResponseEntity.ok().body("\"" + result + "\"");
    }
}