package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.log.LogAttivitaDAO;
import it.asso.core.dao.statistiche.StatisticaDAO;
import it.asso.core.model.log.AttivitaSintetico;
import it.asso.core.model.statistiche.Marker;
import it.asso.core.model.statistiche.SimpleResultSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class StatisticaService {

    private final StatisticaDAO statisticaDAO;
    private final LogAttivitaDAO logAttivitaDAO;

    // Configurazione del percorso (iniettata da application.properties)
    private final String pathPublicAssets;

    // COSTRUTTORE CON INIEZIONE
    public StatisticaService(StatisticaDAO statisticaDAO, LogAttivitaDAO logAttivitaDAO, @Value("${path_public_assets}") String pathPublicAssets) {
        this.statisticaDAO = statisticaDAO;
        this.logAttivitaDAO = logAttivitaDAO;
        this.pathPublicAssets = pathPublicAssets;
    }

    // ------------------------------------------------------------------------
    // UTILITY: RISOLUZIONE URL/TENANT
    // ------------------------------------------------------------------------

    /**
     * Risolve il Tenant ID dal nome host della richiesta (es. 'asso' da 'asso.local').
     */
    private String resolveTenantFromRequest(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        // Cerca l'indice dopo "://" e poi cerca il primo punto "."
        int start = url.indexOf("//") + 2;
        int end = url.indexOf(".");

        if (start < url.length() && end > start) {
            return url.substring(start, end);
        }
        return "default"; // Fallback
    }

    /**
     * Costruisce il prefisso URL per le immagini.
     */
    private String buildImageUrlPrefix(HttpServletRequest request, String tenant) {
        // Logica per costruire l'URL: rimuove il contesto e aggiunge /images/tenant/
        String urlImage = request.getHeader("referer").substring(0, request.getHeader("referer").indexOf(request.getContextPath())) + "/images";
        return urlImage + "/" + tenant + "/";
    }


    // ------------------------------------------------------------------------
    // ENDPOINT DI LETTURA E POPOLAMENTO (DELEGATI)
    // ------------------------------------------------------------------------

    /**
     * Recupera gli animali adottati e popola i marker con il percorso dell'immagine (tenant-aware).
     */
    @Transactional(readOnly = true)
    public List<Marker> getAnimaliAdottatiMarker(HttpServletRequest request) throws AssoServiceException {
        String tenant = resolveTenantFromRequest(request);
        String urlPrefix = buildImageUrlPrefix(request, tenant);

        // Il DAO riceve il prefisso URL per costruire il path della foto
        return statisticaDAO.getAnimaliAdottati(urlPrefix);
    }

    /**
     * Recupera il conteggio delle attività, gestendo l'argomento speciale per tutti.
     */
    @Transactional(readOnly = true)
    public List<AttivitaSintetico> getCountAttivita(String arg) throws AssoServiceException {

        if (Def.NUM_QUATTRO.equals(arg)) {
            return logAttivitaDAO.getCountAttivita();
        } else {
            return logAttivitaDAO.getCountAttivita(arg);
        }
    }
}