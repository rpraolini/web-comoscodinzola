package it.asso.core.controller.organizzazione;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.organizzazione.contabilita.ContoCorrenteDAO;
import it.asso.core.model.organizzazione.contabilita.ContoCorrente;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/jsp/private/organizzazione") // Path base
public class ContoCorrenteController { // Non estende BaseController

    private final ContoCorrenteDAO contoCorrenteDao;

    // COSTRUTTORE CON INIEZIONE DEL DAO
    public ContoCorrenteController(ContoCorrenteDAO contoCorrenteDao) {
        this.contoCorrenteDao = contoCorrenteDao;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (GET)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getContiCorrenti.json", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody List<ContoCorrente> getContiCorrenti(@RequestParam String id) throws AssoServiceException {
        return contoCorrenteDao.getContiByIDOrganizzazione(id);
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (POST)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/saveOrUpdateCC.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody ResponseEntity<String> saveOrUpdateCC(@RequestBody ContoCorrente cc) throws AssoServiceException{
        String result = contoCorrenteDao.saveOrUpdate(cc);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/eliminaCC.json", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()") // Sostituisce checkAuthentication()
    public @ResponseBody ResponseEntity<String> eliminaCC(@RequestParam String id) throws AssoServiceException{
        String result = contoCorrenteDao.deleteByID(id);
        return ResponseEntity.ok().body("\"" + result + "\"");
    }
}