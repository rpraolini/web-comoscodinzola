package it.asso.core.service;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.animale.CaratteriDAO;
import it.asso.core.dao.animali.salute.EventoClinicoDAO;
import it.asso.core.dao.documenti.FotoDAO;
import it.asso.core.dao.documenti.VideoDAO;
import it.asso.core.dao.localizzazione.LocalizzazioneDAO;
import it.asso.core.dao.organizzazione.OrganizzazioneDAO;
import it.asso.core.dto.ricerca.FiltroRicerca;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.animale.Caratteri;
import it.asso.core.model.documenti.Foto;
import it.asso.core.model.documenti.Video;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;
import it.asso.core.model.organizzazione.Organizzazione;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Service
public class PublicService {

    // DAO
    private final AnimaleDAO animaleDao;
    private final LocalizzazioneDAO amministrazioneDao;
    private final FotoDAO fotoDao;
    private final VideoDAO videoDao;
    private final CaratteriDAO caratteriDao;
    private final OrganizzazioneDAO organizzazioneDao;
    private final EventoClinicoDAO eventoClinicoDao; // Necessario per isSterilizzato

    @Value("${app.images.base-url:}")
    private String remoteImageBaseUrl;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public PublicService(AnimaleDAO animaleDao, LocalizzazioneDAO amministrazioneDao, FotoDAO fotoDao, VideoDAO videoDao, CaratteriDAO caratteriDao, OrganizzazioneDAO organizzazioneDao, EventoClinicoDAO eventoClinicoDao, @Value("${path_public_assets}") String pathPublicAssets) {
        this.animaleDao = animaleDao;
        this.amministrazioneDao = amministrazioneDao;
        this.fotoDao = fotoDao;
        this.videoDao = videoDao;
        this.caratteriDao = caratteriDao;
        this.organizzazioneDao = organizzazioneDao;
        this.eventoClinicoDao = eventoClinicoDao;
    }

    // ------------------------------------------------------------------------
    // UTILITY: RISOLUZIONE TENANT E URL
    // ------------------------------------------------------------------------

    /**
     * Risolve il Tenant ID dal nome host della richiesta (es. 'asso' da 'asso.local').
     */
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
    private Foto populateFotoUrl(HttpServletRequest request, Foto f, String tenant) {
        String baseUrl;
        if (StringUtils.hasText(remoteImageBaseUrl)) {
            baseUrl = remoteImageBaseUrl;
        } else {
            baseUrl = ServletUriComponentsBuilder.fromCurrentRequestUri()
                    .replacePath(request.getContextPath())
                    .build()
                    .toUriString();
        }

        String urlBase = baseUrl + "/images";
        if (f == null) {
            return null;
        } else {
            String urlDir = urlBase + "/" + tenant + "/" + f.getId_animale() + "/";
            f.setUrl(urlDir + f.getNome_file());
            if (f.getNome_file_t() != null && !f.getNome_file_t().isEmpty()) {
                f.setUrl_t(urlDir + f.getNome_file_t());
            }
            f.setPercorso(null); // non esporre il path del filesystem su endpoint pubblico
        }
        return f;
    }

    // ------------------------------------------------------------------------
    // ENDPOINT DI LETTURA E POPOLAMENTO
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Organizzazione getOrganizzazioneByTenant(String tenantId) throws AssoServiceException {
        return organizzazioneDao.getByTenant(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Animale> getRandomAnimali(HttpServletRequest request) {
        String tenant = resolveTenantFromRequest(request);
        List<Animale> animali = animaleDao.getRandom();

        for (Animale animale : animali) {
            Foto f = fotoDao.getFotoProfiloById(animale.getId_animale());
            animale.setFoto(populateFotoUrl(request, f, tenant));
            animale.setLocation(animaleDao.getLocation(animale.getId_animale()));
        }
        return animali;
    }

    @Transactional(readOnly = true)
    public List<Animale> getLietiFine(HttpServletRequest request, String anno) {
        String tenant = resolveTenantFromRequest(request);
        List<Animale> animali = animaleDao.getLietiFine(anno);

        for (Animale animale : animali) {
            Foto f = fotoDao.getFotoPostAdozione(animale.getId_animale());
            animale.setFoto(populateFotoUrl(request, f, tenant));

            // Logica di fallback se la foto post-adozione non esiste
            if (animale.getFoto() == null) {
                animale.setFoto(populateFotoUrl(request, fotoDao.getFotoProfiloById(animale.getId_animale()), tenant));
            }
        }
        return animali;
    }

    @Transactional(readOnly = true)
    public Animale getAnimaleByIdWithDetails(HttpServletRequest request, String id) throws AssoServiceException {
        String tenant = resolveTenantFromRequest(request);
        Animale animale = animaleDao.getById(id);

        animale.setFoto(populateFotoUrl(request, fotoDao.getFotoProfiloById(animale.getId_animale()), tenant));
        animale.setSterilizzato(eventoClinicoDao.isSterilizzato(id));
        //animale.setProprietario(animaleDao.getProprietarioByIDAnimale(id));
        animale.setLocation(animaleDao.getLocation(animale.getId_animale()));

        return animale;
    }

    @Transactional(readOnly = true)
    public List<Foto> getFotoPubbliche(HttpServletRequest request, String idAnimale) throws AssoServiceException {
        String tenant = resolveTenantFromRequest(request);


        List<Foto> foto = fotoDao.getFotoByIdAnimale(idAnimale);
        foto.removeIf(f -> !"1".equals(f.getPubblica())); // Mantieni solo le foto pubbliche

        for (Foto f : foto) {
            if (f != null && f.getNome_file() != null) {
                // Popola l'url solo se la foto ha effettivamente un file
                populateFotoUrl(request, f, tenant);
            }
        }
        return foto;
    }

    @Transactional(readOnly = true)
    public List<Video> getVideoPubblici(String idAnimale) throws AssoServiceException {
        return videoDao.getVideoPubbliciByIdAnimale(idAnimale);
    }

    @Transactional(readOnly = true)
    public List<Caratteri> getCaratteriByAnimale(String idAnimale) throws AssoServiceException {
        return caratteriDao.getCaratteriByIdAnimale(idAnimale);
    }

    @Transactional(readOnly = true)
    public List<Animale> ricercaAnimali(HttpServletRequest request, FiltroRicerca filtri){
        String tenant = resolveTenantFromRequest(request);
        List<Animale> animali = animaleDao.getRicercaPubblica(filtri.getTipo(), filtri.getEta(), filtri.getTaglia(), filtri.getSesso(), filtri.getRegione(), filtri.getProvincia());

        for (Animale animale : animali) {
            animale.setFoto(populateFotoUrl(request, fotoDao.getFotoProfiloById(animale.getId_animale()), tenant));
            animale.setLocation(animaleDao.getLocation(animale.getId_animale()));
        }
        return animali;
    }

    // ------------------------------------------------------------------------
    // METODI DI LOCALIZZAZIONE (DELEGATI)
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Provincia> getProvinceByRegioneUsed(String idRegione) throws AssoServiceException {
        return amministrazioneDao.getProvincieByRegioneUsed(idRegione);
    }

    @Transactional(readOnly = true)
    public List<Regione> getRegioni() throws AssoServiceException {
        return amministrazioneDao.getRegioni();
    }

    @Transactional(readOnly = true)
    public List<Regione> getRegioniByNazioneUsed(String nazione) throws AssoServiceException {
        return amministrazioneDao.getRegioneByNazioneUsed(nazione);
    }
}