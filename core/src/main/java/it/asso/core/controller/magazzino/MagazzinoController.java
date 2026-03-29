package it.asso.core.controller.magazzino;

import it.asso.core.common.Def;
import it.asso.core.common.ResultGrid;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.model.magazzino.*;
import it.asso.core.security.UserAuth;
import it.asso.core.service.MagazzinoService; // Nuovo Service
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Controller
@RequestMapping("/jsp/private/magazzino")
public class MagazzinoController {

    private final MagazzinoService magazzinoService;
    private final ObjectMapper objectMapper; // Per deserializzare il JSON di Prodotto/Tipologia

    // UTILITY PER L'AUTORIZZAZIONE
    private static final String AUTH = "isAuthenticated()";
    private static final String WRITE_ASSOCIAZIONE = "@authz.checkWritePermission('AREA_DATI_ASSOCIAZIONE')";
    private static final String WRITE_ANIMALE = "@authz.checkWritePermission('AREA_DATI_ANIMALE')"; // Usato per saveTipologia

    // COSTRUTTORE CON INIEZIONE DEL SERVICE E DI JACKSON
    public MagazzinoController(MagazzinoService magazzinoService, ObjectMapper objectMapper) {
        this.magazzinoService = magazzinoService;
        this.objectMapper = objectMapper;
    }

    // ------------------------------------------------------------------------
    // PRODOTTI (CRUD)
    // ------------------------------------------------------------------------

    // SALVATAGGIO PRODOTTO (Logica complessa di File I/O e Popolamento)
    @RequestMapping(value = "/saveProdotto.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> saveProdotto(@RequestParam("prodotto") final String jsondata, @RequestParam(required=false, value="foto") final MultipartFile[] files, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws Exception {

        Prodotto prodotto = objectMapper.readValue(jsondata, Prodotto.class);

        // Delega la logica di salvataggio, immagine, e popolamento al Service
        String result = magazzinoService.saveProdotto(prodotto, files, user, request);

        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    // LETTURA PRODOTTI (Logica complessa di Popolamento/URL)
    @RequestMapping(value = "/getProdottiByID.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getProdottiByID(@RequestBody RicercaDTO ricerca, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws AssoServiceException {
        // Delega la logica di caricamento, popolamento e URL al Service
        return magazzinoService.getProdottiByID(ricerca, user, request);
    }

    @RequestMapping(value = "/deleteProdottoByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteProdottoByID(@RequestBody Prodotto prodotto) throws AssoServiceException{
        String ret = magazzinoService.deleteProdotto(prodotto);
        return ResponseEntity.ok().body("\"" + ret + "\"");
    }

    // ------------------------------------------------------------------------
    // MAGAZZINO (CRUD)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getMagazzini.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Magazzino> getMagazzini() throws AssoServiceException {
        return magazzinoService.getMagazzini();
    }

    @RequestMapping(value = "/getRicercaMagazzini.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getRicercaMagazzini(@RequestBody RicercaDTO ricerca) throws AssoServiceException {
        return magazzinoService.getRicercaMagazzini(ricerca);
    }

    @RequestMapping(value = "/saveMagazzino.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> saveMagazzino(@RequestBody Magazzino magazzino) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        magazzinoService.saveMagazzino(magazzino);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/deleteMagazzinoByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteMagazzinoByID(@RequestBody Magazzino magazzino) throws AssoServiceException{
        String ret = magazzinoService.deleteMagazzino(magazzino);
        return ResponseEntity.ok().body("\"" + ret + "\"");
    }

    // ------------------------------------------------------------------------
    // MARCA (CRUD)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getMarche.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Marca> getMarche() throws AssoServiceException {
        return magazzinoService.getMarche();
    }

    @RequestMapping(value = "/getRicercaMarche.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getRicercaMarche(@RequestBody RicercaDTO ricerca) throws AssoServiceException {
        return magazzinoService.getRicercaMarche(ricerca);
    }

    @RequestMapping(value = "/saveMarca.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> saveMarca(@RequestBody Marca marca) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        magazzinoService.saveMarca(marca);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/deleteMarcaByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteMarcaByID(@RequestBody Marca marca) throws AssoServiceException{
        String ret = magazzinoService.deleteMarca(marca);
        return ResponseEntity.ok().body("\"" + ret + "\"");
    }

    // ------------------------------------------------------------------------
    // COLORI (CRUD)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getColori.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Colore> getColori() throws AssoServiceException {
        return magazzinoService.getColori();
    }

    @RequestMapping(value = "/getRicercaColori.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getRicercaColori(@RequestBody RicercaDTO ricerca) throws AssoServiceException {
        return magazzinoService.getRicercaColori(ricerca);
    }

    @RequestMapping(value = "/saveColore.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> saveColore(@RequestBody Colore colore) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        magazzinoService.saveColore(colore);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/deleteColoreByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteColoreByID(@RequestBody Colore colore) throws AssoServiceException{
        String ret = magazzinoService.deleteColore(colore);
        return ResponseEntity.ok().body("\"" + ret + "\"");
    }

    // ------------------------------------------------------------------------
    // TAGLIE (CRUD)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getTaglie.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody List<Taglia> getTaglie() throws AssoServiceException {
        return magazzinoService.getTaglie();
    }

    @RequestMapping(value = "/getRicercaTaglie.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getRicercaTaglie(@RequestBody RicercaDTO ricerca) throws AssoServiceException {
        return magazzinoService.getRicercaTaglie(ricerca);
    }

    @RequestMapping(value = "/saveTaglia.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> saveTaglia(@RequestBody Taglia taglia) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        magazzinoService.saveTaglia(taglia);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }

    @RequestMapping(value = "/deleteTagliaByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteTagliaByID(@RequestBody Taglia taglia) throws AssoServiceException{
        String ret = magazzinoService.deleteTaglia(taglia);
        return ResponseEntity.ok().body("\"" + ret + "\"");
    }

    // ------------------------------------------------------------------------
    // TIPOLOGIA (CRUD)
    // ------------------------------------------------------------------------

    // SALVATAGGIO TIPOLOGIA (Logica complessa di File I/O e Popolamento)
    @RequestMapping(value = "/saveTipologia.json", method = RequestMethod.POST)
    // NOTA: Usava Def.AREA_DATI_ANIMALE nel vecchio codice per la scrittura con foto. Manteniamo quella logica.
    @PreAuthorize(WRITE_ANIMALE)
    public @ResponseBody ResponseEntity<String> saveTipologia(@RequestParam("tipologia") final String jsondata, @RequestParam(required=false, value="foto") final MultipartFile[] files, @AuthenticationPrincipal UserAuth user) throws Exception {

        Tipologia tipologia = objectMapper.readValue(jsondata, Tipologia.class);

        // Delega la logica di salvataggio, immagine, e popolamento al Service
        String result = magazzinoService.saveTipologia(tipologia, files, user);

        return ResponseEntity.ok().body("\"" + result + "\"");
    }

    @RequestMapping(value = "/getTipologie.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getTipologie(@RequestBody RicercaDTO ricerca, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws AssoServiceException {
        return magazzinoService.getTipologie(ricerca, user, request);
    }

    @RequestMapping(value = "/getTipologiaByID.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody Tipologia getTipologiaByID(@RequestParam String idTipologia, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws AssoServiceException {
        return magazzinoService.getTipologiaByID(idTipologia, user, request);
    }

    @RequestMapping(value = "/deleteTipologiaByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteTipologiaByID(@RequestBody Tipologia mp) throws AssoServiceException{
        String ret = magazzinoService.deleteTipologia(mp);
        return ResponseEntity.ok().body("\"" + ret + "\"");
    }

    // ------------------------------------------------------------------------
    // SOTTO TIPOLOGIA (CRUD)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/getSottoTipologie.json", method = RequestMethod.POST)
    @PreAuthorize(AUTH)
    public @ResponseBody ResultGrid getSottoTipologie(@RequestBody RicercaDTO ricerca, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws AssoServiceException {
        return magazzinoService.getSottoTipologie(ricerca, user, request);
    }

    @RequestMapping(value = "/getSottoTipologiaByID.json", method = RequestMethod.GET)
    @PreAuthorize(AUTH)
    public @ResponseBody SottoTipologia getSottoTipologiaByID(@RequestParam String idSottoTipologia, @AuthenticationPrincipal UserAuth user, HttpServletRequest request) throws AssoServiceException {
        return magazzinoService.getSottoTipologiaByID(idSottoTipologia, user, request);
    }

    @RequestMapping(value = "/saveSottoTipologia.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> saveSottoTipologia(@RequestBody SottoTipologia sottoTipologia) throws Exception {
        magazzinoService.saveSottoTipologia(sottoTipologia);
        return ResponseEntity.ok().body("\"" + Def.STR_SUCCESS + "\"");
    }

    @RequestMapping(value = "/deleteSottoTipologiaByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteSottoTipologiaByID(@RequestBody SottoTipologia mp) throws AssoServiceException{
        String ret = magazzinoService.deleteSottoTipologia(mp);
        return ResponseEntity.ok().body("\"" + ret + "\"");
    }

    // ------------------------------------------------------------------------
    // MAGAZZINO PRODOTTO (DELETE)
    // ------------------------------------------------------------------------

    @RequestMapping(value = "/deleteMagazzinoProdottoByID.json", method = RequestMethod.POST)
    @PreAuthorize(WRITE_ASSOCIAZIONE)
    public @ResponseBody ResponseEntity<String> deleteMagazzinoProdottoByID(@RequestBody MagazzinoProdotto mp) throws AssoServiceException{
        magazzinoService.deleteMagazzinoProdotto(mp);
        return ResponseEntity.ok().body("\"" + Def.STR_OK + "\"");
    }
}