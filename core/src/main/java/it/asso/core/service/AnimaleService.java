package it.asso.core.service;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.documenti.FotoDAO;
import it.asso.core.dao.localizzazione.LocalizzazioneDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.animale.RicercaDTO;
import it.asso.core.model.documenti.Foto;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AnimaleService {

    @Value("${app.images.base-url:}")
    private String remoteImageBaseUrl;

    @Autowired
    private HttpServletRequest request;

    private final AnimaleDAO animaleDao;
    private final LocalizzazioneDAO localizzazioneDao;
    private final FotoDAO fotoDao;
    private final TagService  tagService;
    private final DocumentoService  documentoService;

    // Iniezione del DAO
    public AnimaleService(AnimaleDAO animaleDao, LocalizzazioneDAO localizzazioneDao, FotoDAO fotoDao, TagService tagService, DocumentoService documentoService) {
        this.animaleDao = animaleDao;
        this.localizzazioneDao = localizzazioneDao;
        this.fotoDao = fotoDao;
        this.tagService = tagService;
        this.documentoService = documentoService;
    }

    public Animale findById(String id) {
        String tenant = resolveTenantFromRequest(request);
        Animale animale = animaleDao.getById(id);
        animale.setFoto(populateFotoUrl(fotoDao.getFotoProfiloById(animale.getId_animale()), tenant));
        animale.setTags(tagService.getTagsByAnimale(id));
        animale.setDocumenti(documentoService.getDocumentiPerAnimale(id));
        return animale;
    }

    public String resolveTenantFromRequest(HttpServletRequest request) {
        // 1. Ottieni solo il dominio (es. "asso.local", "www.asso.it", "localhost")
        String domain = request.getServerName();

        // 2. Gestione Localhost (Sviluppo)
        if (domain.equalsIgnoreCase("localhost") || domain.equals("127.0.0.1")) {
            return "asso"; // O "asso", se vuoi testare quel tenant in locale
        }

        // 3. Gestione "www." (Produzione)
        // Se il dominio inizia con www., lo togliamo per prendere il vero nome
        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }

        // 4. Estrazione del Tenant (la prima parte del dominio)
        // Es. "asso.local" -> "asso"
        // Es. "tenant1.miodominio.com" -> "tenant1"
        int dotIndex = domain.indexOf(".");
        if (dotIndex != -1) {
            return domain.substring(0, dotIndex);
        }

        // 5. Fallback: Se non ci sono punti, restituisci l'intero dominio o default
        return domain;
    }
    /**
     * Costruisce l'URL completo per una foto (profilo o post-adozione).
     */
    private Foto populateFotoUrl(Foto f, String tenant) {
        String baseUrl;
        // Costruzione dell'URL base dell'immagine (logica complessa migrata dal controller)
        if (StringUtils.hasText(remoteImageBaseUrl)) {
            baseUrl = remoteImageBaseUrl;
        } else {
            baseUrl = ServletUriComponentsBuilder.fromCurrentRequestUri()
                    .replacePath(null)
                    .build()
                    .toUriString();
        }

        // 2. Aggiunge la cartella images
        String urlImage = baseUrl + "/images";
        if (f == null) {
            f = new Foto();
            f.setUrl(urlImage + "/empty.png");
        } else {
            f.setUrl(urlImage + "/" + tenant + "/" + f.getId_animale() + "/" + f.getNome_file());
        }
        return f;
    }

    // Metodo per recuperare gli ultimi animali inseriti
    // Usiamo numToList = 50 (o quello che preferisci) e filterBy = "1" (se vuoi tutti) o vuoto a seconda di come gestisci il filtro
    public List<Animale> getUltimiAnimaliInseriti(int limite, String filtro) {
        // Puoi anche usare getBySearch("") o getAdottabiliBySearch("")
        return animaleDao.getLastInsert(limite, filtro);
    }

    public List<Animale> getRicerca(RicercaDTO criteri) {
        // Puoi anche usare getBySearch("") o getAdottabiliBySearch("")
        return animaleDao.getRicerca(criteri);
    }

    public int getCountBySearch(RicercaDTO criteri) {
        // Puoi anche usare getBySearch("") o getAdottabiliBySearch("")
        return animaleDao.getCountBySearch(criteri);
    }

    @Transactional(readOnly = true)
    public List<Regione> getRegioni(String nazione) throws AssoServiceException {
        return localizzazioneDao.getRegioneByNazioneUsed(nazione);
    }

    @Transactional(readOnly = true)
    public List<Provincia> getProvinceByRegioneUsed(String idRegione) throws AssoServiceException {
        return localizzazioneDao.getProvincieByRegioneUsed(idRegione);
    }

    @Transactional
    public String inserisciNuovoAnimale(Animale animale) throws AssoServiceException {
        if (animale.getNum_microchip() != null && !animale.getNum_microchip().isEmpty()) {
            if (animale.getNum_microchip().length() != 15) {
                throw new AssoServiceException("Il microchip deve contenere esattamente 15 cifre.");
            }
        }
        animale.setDt_nascita(normalizzaData(animale.getDt_nascita()));
        return animaleDao.save(animale);
    }

    @Transactional
    public String aggiorna(String id, Animale animale) throws AssoServiceException {
        if (animale.getNum_microchip() != null && !animale.getNum_microchip().isEmpty()) {
            if (animale.getNum_microchip().length() != 15) {
                throw new AssoServiceException("Il microchip deve contenere esattamente 15 cifre.");
            }
        }
        animale.setDt_nascita(normalizzaData(animale.getDt_nascita()));
        animale.setId_animale(id);
        return animaleDao.update(animale);
    }

    @Transactional
    public void elimina(String id) {
        animaleDao.deleteByID(id);
    }

    private String normalizzaData(String data) throws AssoServiceException {
        if (data == null || data.isEmpty()) return data;
        try {
            if (data.contains("T")) {
                LocalDate date = Instant.parse(data)
                        .atZone(ZoneId.of("Europe/Rome"))
                        .toLocalDate();
                return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return data;
        } catch (Exception e) {
            throw new AssoServiceException("Formato data non valido: " + data);
        }
    }

}