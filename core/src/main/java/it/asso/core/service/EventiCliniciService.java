package it.asso.core.service;

import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.salute.EventoClinicoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.model.animali.salute.EventoClinico;
import it.asso.core.model.documenti.Documento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventiCliniciService {

    private final EventoClinicoDAO eventoClinicoDao;
    private final DocumentoDAO documentoDao;

    // Costruttore con iniezione di tutte le dipendenze
    public EventiCliniciService(EventoClinicoDAO eventoClinicoDao, DocumentoDAO documentoDao) {
        this.eventoClinicoDao = eventoClinicoDao;
        this.documentoDao = documentoDao;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI LETTURA E POPOLAMENTO
    // ------------------------------------------------------------------------

    /**
     * Recupera gli eventi clinici per un animale e popola i documenti associati.
     */
    @Transactional(readOnly = true)
    public List<EventoClinico> getEventiCliniciByIdAnimale(String idAnimale) throws AssoServiceException {
        List<EventoClinico> eventiClinici = eventoClinicoDao.getEventiCliniciByIdAnimale(idAnimale);

        for (EventoClinico ec : eventiClinici) {
            // Popolamento documenti (logica migrata dal Controller)
            if (ec.getDocumenti() != null) { // Assumiamo la lista sia inizializzata
                ArrayList<Documento> documenti = documentoDao.getDocumentiByIDEvento(ec.getId_evento());
                ec.getDocumenti().addAll(documenti);
            }
        }
        return eventiClinici;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI SCRITTURA E CANCELLAZIONE (TRANSAZIONALE)
    // ------------------------------------------------------------------------

    /**
     * Elimina un evento clinico e tutti i documenti collegati in modo transazionale.
     */
    @Transactional // Garantisce che l'eliminazione sia atomica (o tutto o niente)
    public String deleteEventoClinicoAndDocuments(String idEvento)
            throws AssoServiceException, SQLIntegrityConstraintViolationException, IllegalStateException, IOException {

        // 1. Recupera la lista dei documenti collegati all'evento (idEvento)
        List<Documento> documenti = documentoDao.getDocumentiByIDEvento(idEvento);

        // 2. Elimina i collegamenti e i documenti uno per uno
        for (Documento doc : documenti) {
            // a. Elimina il collegamento tra Documento ed Evento Clinico
            documentoDao.deleteDocumentoEventoClinico(doc.getId_documento(), idEvento);

            // b. Elimina il Documento dal sistema/database
            documentoDao.deleteByID(doc.getId_documento());
        }

        // 3. Elimina l'Evento Clinico
        String result = eventoClinicoDao.delete(idEvento);

        return result;
    }
}