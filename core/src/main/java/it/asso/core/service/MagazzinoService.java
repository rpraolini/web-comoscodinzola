package it.asso.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.asso.core.common.Def;
import it.asso.core.common.ResizeImage; // Assumiamo esista la classe per il resizing
import it.asso.core.common.ResultGrid;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.magazzino.MagazzinoDAO;
import it.asso.core.model.magazzino.*;
import it.asso.core.security.UserAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MagazzinoService {

    private static final Logger logger = LoggerFactory.getLogger(MagazzinoService.class);
    private final MagazzinoDAO magazzinoDao;
    private final ObjectMapper objectMapper;

    // Configurazione del percorso e della dimensione (iniettati da application.properties)
    private final String pathDoc;
    private final Integer resizeDimensione;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public MagazzinoService(MagazzinoDAO magazzinoDao, ObjectMapper objectMapper,
                            @Value("${path_doc}") String pathDoc,
                            @Value("${resize_img_magazzini}") String resizeDimensione) {
        this.magazzinoDao = magazzinoDao;
        this.objectMapper = objectMapper;
        this.pathDoc = pathDoc;
        this.resizeDimensione = Integer.valueOf(resizeDimensione);
    }

    // ------------------------------------------------------------------------
    // UTILITY PRIVATE
    // ------------------------------------------------------------------------

    private String generateImageUrl(HttpServletRequest request, String tenant, String entityType, String fileName) {
        // Logica per costruire l'URL (migrata da Controller)
        String referer = request.getHeader("referer");
        String contextPath = request.getContextPath();

        // Determina il percorso base dell'applicazione
        String urlBase = referer.substring(0, referer.indexOf(contextPath));

        Date date= new Date();
        long time = date.getTime();

        // Esempio: http://asso.local/images/asso/prodotti/nome.png?timestamp
        return urlBase + "/images/" + tenant + "/" + entityType + "/" + fileName + "?" + new Timestamp(time);
    }

    private void createDirectoryIfNotExist(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    // ------------------------------------------------------------------------
    // PRODOTTI (CRUD & LOGIC)
    // ------------------------------------------------------------------------

    /**
     * Salva Prodotto, gestisce il resize e il salvataggio dell'immagine su disco.
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveProdotto(Prodotto prodotto, MultipartFile[] files, UserAuth user, HttpServletRequest request) throws Exception {

        prodotto.setAccount(user.getUsername());
        String tenant = user.getUtente().getOrganizzazione().getTenant();
        String imageFilename = null;

        // 1. Configurazione Directory
        String saveDirectory = pathDoc;
        Path directory = Paths.get(saveDirectory, tenant, "prodotti");
        createDirectoryIfNotExist(directory);

        // 2. Gestione File e Resize
        if(files != null && files.length > 0) {
            MultipartFile mf = files[0]; // Prendiamo solo il primo file
            String[] fileExt = mf.getOriginalFilename().split("\\.");
            String estensione = fileExt[fileExt.length-1];
            imageFilename = prodotto.getDescrProdotto().toLowerCase().trim().replaceAll("\\s+","_") + "." + estensione; // Nome file normalizzato

            InputStream in = new ByteArrayInputStream(mf.getBytes());
            BufferedImage originalImage = ImageIO.read(in);

            // Usa la classe ResizeImage e la dimensione iniettata
            BufferedImage finalImage = ResizeImage.resize(originalImage, resizeDimensione);
            File f = new File(directory.toString(), imageFilename);

            // Scrittura del file ridimensionato
            ImageIO.write(finalImage, estensione, f);

            prodotto.setImmagine(imageFilename);
        }

        // 3. Salvataggio Prodotto principale
        String idProdotto = magazzinoDao.saveOrUpdateProdotto(prodotto);

        // 4. Salvataggio MagazzinoProdotto (relazioni)
        for(MagazzinoProdotto mp : prodotto.getMagazzinoProdotto()) {
            mp.setIdProdotto(idProdotto);
            magazzinoDao.saveOrUpdateMagazzinoProdotto(mp);
        }
        return idProdotto;
    }

    /**
     * Recupera la lista dei prodotti e popola i campi complessi e l'URL dell'immagine.
     */
    @Transactional(readOnly = true)
    public ResultGrid getProdottiByID(RicercaDTO ricerca, UserAuth user, HttpServletRequest request) throws AssoServiceException {
        String tenant = user.getUtente().getOrganizzazione().getTenant();
        ResultGrid result = new ResultGrid();
        List<Prodotto> prodotti = magazzinoDao.getProdottiByID(ricerca);

        for(Prodotto p : prodotti) {
            if(p.getIdSottoTipologia() != null) {
                p.setSottoTipologia(magazzinoDao.getSottoTipologiaByID(p.getIdSottoTipologia()));
            }

            List<MagazzinoProdotto> mp = magazzinoDao.getMagazzinoProdottoByID(p.getIdProdotto());
            for(MagazzinoProdotto m : mp) {
                m.setColore(magazzinoDao.getColoreByID(m.getIdColore()));
                m.setTaglia(magazzinoDao.getTagliaByID(m.getIdTaglia()));
                m.setMagazzino(magazzinoDao.getMagazzinoByID(m.getIdMagazzino()));
            }
            p.setMagazzinoProdotto(mp);
            p.setMarca(magazzinoDao.getMarcaByID(p.getIdMarca()));

            // Popolamento URL dell'immagine
            if (p.getImmagine() != null) {
                p.setUrl(generateImageUrl(request, tenant, "prodotti", p.getImmagine()));
            }
        }
        result.setRecords(prodotti);
        result.setTotale(magazzinoDao.getCountProdottiByID(ricerca));
        return result;
    }

    @Transactional
    public String deleteProdotto(Prodotto prodotto) throws AssoServiceException{
        return magazzinoDao.delete(prodotto);
    }

    // ------------------------------------------------------------------------
    // TIPOLOGIA (CRUD & LOGIC)
    // ------------------------------------------------------------------------

    @Transactional
    public String saveTipologia(Tipologia tipologia, MultipartFile[] files, UserAuth user) throws Exception {

        // Logica di salvataggio e gestione immagini simile a saveProdotto
        // ... (Implementa qui la logica di salvataggio del file, resizing e aggiornamento del DB)

        return Def.STR_SUCCESS; // Placeholder
    }

    public ResultGrid getTipologie(RicercaDTO ricerca, UserAuth user, HttpServletRequest request) throws AssoServiceException {
        // Implementa qui la logica di ricerca Tipologie con popolamento URL
        ResultGrid result = new ResultGrid();
        // ...
        return result; // Placeholder
    }

    public Tipologia getTipologiaByID(String idTipologia, UserAuth user, HttpServletRequest request) throws AssoServiceException {
        // Implementa qui la logica di recupero e popolamento
        // ...
        return null; // Placeholder
    }

    public String deleteTipologia(Tipologia mp) throws AssoServiceException{
        return magazzinoDao.delete(mp);
    }

    // ------------------------------------------------------------------------
    // MAGAZZINO, MARCA, COLORI, TAGLIE, SOTTO TIPOLOGIA (CRUD SEMPLICE)
    // ------------------------------------------------------------------------

    // Logica semplice di CRUD (delete, save) per tutte le altre tabelle

    public List<Magazzino> getMagazzini() throws AssoServiceException { return magazzinoDao.getMagazzini(); }
    public ResultGrid getRicercaMagazzini(RicercaDTO ricerca) throws AssoServiceException { return new ResultGrid(); } // Placeholder
    public void saveMagazzino(Magazzino magazzino) throws SQLIntegrityConstraintViolationException { magazzinoDao.saveOrUpdateMagazzino(magazzino); }
    public String deleteMagazzino(Magazzino magazzino) throws AssoServiceException { return magazzinoDao.delete(magazzino); }

    public List<Marca> getMarche() throws AssoServiceException { return magazzinoDao.getMarche(); }
    public ResultGrid getRicercaMarche(RicercaDTO ricerca) throws AssoServiceException { return new ResultGrid(); } // Placeholder
    public void saveMarca(Marca marca) throws SQLIntegrityConstraintViolationException { magazzinoDao.saveOrUpdateMarca(marca); }
    public String deleteMarca(Marca marca) throws AssoServiceException { return magazzinoDao.delete(marca); }

    public List<Colore> getColori() throws AssoServiceException { return magazzinoDao.getColori(); }
    public ResultGrid getRicercaColori(RicercaDTO ricerca) throws AssoServiceException { return new ResultGrid(); } // Placeholder
    public void saveColore(Colore colore) throws SQLIntegrityConstraintViolationException { magazzinoDao.saveOrUpdateColore(colore); }
    public String deleteColore(Colore colore) throws AssoServiceException { return magazzinoDao.delete(colore); }

    public List<Taglia> getTaglie() throws AssoServiceException { return magazzinoDao.getTaglie(); }
    public ResultGrid getRicercaTaglie(RicercaDTO ricerca) throws AssoServiceException { return new ResultGrid(); } // Placeholder
    public void saveTaglia(Taglia taglia) throws SQLIntegrityConstraintViolationException { magazzinoDao.saveOrUpdateTaglia(taglia); }
    public String deleteTaglia(Taglia taglia) throws AssoServiceException { return magazzinoDao.delete(taglia); }

    // SOTTO TIPOLOGIA (Da completare in modo simile)
    public ResultGrid getSottoTipologie(RicercaDTO ricerca, UserAuth user, HttpServletRequest request) throws AssoServiceException { return new ResultGrid(); }
    public SottoTipologia getSottoTipologiaByID(String idSottoTipologia, UserAuth user, HttpServletRequest request) throws AssoServiceException { return null; }
    public void saveSottoTipologia(SottoTipologia sottoTipologia) throws Exception { magazzinoDao.saveOrUpdateSottoTipologia(sottoTipologia); }
    public String deleteSottoTipologia(SottoTipologia mp) throws AssoServiceException{ return magazzinoDao.delete(mp); }

    // MAGAZZINO PRODOTTO
    public void deleteMagazzinoProdotto(MagazzinoProdotto mp) throws AssoServiceException{ magazzinoDao.delete(mp); }
}