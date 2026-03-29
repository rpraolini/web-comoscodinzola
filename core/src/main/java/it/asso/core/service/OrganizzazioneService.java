package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.ResultGrid;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.organizzazione.OrganizzazioneDAO;
import it.asso.core.dao.organizzazione.TipoOrganizzazioneDAO;
import it.asso.core.model.organizzazione.Organizzazione;
import it.asso.core.model.organizzazione.Protocollo;
import it.asso.core.model.organizzazione.RicercaDTO;
import it.asso.core.model.organizzazione.TipoOrganizzazione;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizzazioneService {

    private final OrganizzazioneDAO organizzazioneDao;
    private final TipoOrganizzazioneDAO tipoOrganizzazioneDAO;
    private final DocumentoDAO documentoDao;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public OrganizzazioneService(OrganizzazioneDAO organizzazioneDao, TipoOrganizzazioneDAO tipoOrganizzazioneDAO, DocumentoDAO documentoDao) {
        this.organizzazioneDao = organizzazioneDao;
        this.tipoOrganizzazioneDAO = tipoOrganizzazioneDAO;
        this.documentoDao = documentoDao;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI LETTURA E POPOLAMENTO (Organizzazione)
    // ------------------------------------------------------------------------

    /**
     * Recupera l'organizzazione e popola la sigla del Tipo Organizzazione.
     */
    @Transactional(readOnly = true)
    public Organizzazione getOrganizzazioneWithDetails(String id) throws AssoServiceException {
        Organizzazione org = organizzazioneDao.getByID(id);

        if (org == null) {
            throw new AssoServiceException("Organizzazione non trovata per ID: " + id);
        }

        TipoOrganizzazione to = tipoOrganizzazioneDAO.getByID(org.getId_tipo_organizzazione());

        if (to != null) {
            org.setSigla_tipo_organizzazione(to.getSigla());
        }
        return org;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI LETTURA E POPOLAMENTO (Protocolli)
    // ------------------------------------------------------------------------

    /**
     * Recupera i protocolli e li popola con i documenti collegati.
     */
    @Transactional(readOnly = true)
    public ResultGrid getProtocolli(RicercaDTO ricerca) throws AssoServiceException {
        ResultGrid result = new ResultGrid();
        List<Protocollo> p = organizzazioneDao.getProtocolli(ricerca);

        // Popolamento dei documenti per ogni protocollo (logica migrata dal Controller)
        for (Protocollo protocollo : p) {
            protocollo.setDoc(documentoDao.getDocumentoByID(protocollo.getId_documento()));
        }

        result.setRecords(p);
        result.setTotale(organizzazioneDao.getProtocolliCountBySearch(ricerca));
        return result;
    }

    /**
     * Recupera un singolo protocollo e popola il documento collegato.
     */
    @Transactional(readOnly = true)
    public Protocollo getProtocolloWithDocument(String idProtocollo) throws AssoServiceException {
        Protocollo p = organizzazioneDao.getProtocolloByID(idProtocollo);

        if (p != null) {
            p.setDoc(documentoDao.getDocumentoByID(p.getId_documento()));
        }
        return p;
    }
}