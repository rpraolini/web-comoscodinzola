package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.storia.EventoStoricoDAO;
import it.asso.core.dao.contabilita.ContabilitaDAO;
import it.asso.core.model.animali.storia.EventoStorico;
import it.asso.core.model.contabilita.Pagamento;
import it.asso.core.model.contabilita.PrevisioneSpesa;
import it.asso.core.security.UserAuth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContabilitaService {

    private final ContabilitaDAO contabilitaDAO;
    private final EventoStoricoDAO eventoStoricoDao; // Necessario per recuperare l'evento storico

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public ContabilitaService(ContabilitaDAO contabilitaDAO, EventoStoricoDAO eventoStoricoDao) {
        this.contabilitaDAO = contabilitaDAO;
        this.eventoStoricoDao = eventoStoricoDao;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI GESTIONE E RIPARTIZIONE PAGAMENTI
    // ------------------------------------------------------------------------

    /**
     * Gestisce il salvataggio o la ripartizione complessa di un pagamento.
     * * Il metodo supporta:
     * 1. Pagamento singolo (tipoPagamento = 0)
     * 2. Pagamento ripartito su più previsioni di spesa collegate a un contatto (tipoPagamento != 0)
     */
    @Transactional // Garantisce che la ripartizione sia un'unica transazione
    public String handlePagamento(Pagamento pagamento, UserAuth user) throws AssoServiceException {
        // La logica di business è stata migrata dal Controller

        // 1. Pagamento standard
        if (Def.NUM_ZERO.equals(pagamento.getTipoPagamento())) {
            // L'utente autenticato viene usato per la tracciabilità (se il DAO lo supporta)
            // Anche se l'oggetto Pagamento non ha un campo 'account', l'utente viene passato
            // per future esigenze di logging.
            return contabilitaDAO.saveOrUpdatePagamento(pagamento);
        }

        // 2. Pagamento complesso con ripartizione (tipoPagamento != 0)
        else {
            if (pagamento.getId_evento() == null) {
                // Necessario un ID evento per sapere da chi recuperare le previsioni di spesa
                throw new AssoServiceException("ID Evento mancante per la ripartizione del pagamento.");
            }

            // A. Recupera l'evento storico e l'ID Contatto
            EventoStorico s = eventoStoricoDao.getEventiStoriciByIdEvento(pagamento.getId_evento());
            if (s == null || s.getId_contatto() == null) {
                throw new AssoServiceException("Evento Storico o Contatto non trovato per la ripartizione.");
            }

            // B. Recupera tutte le previsioni di spesa attive per quel contatto
            List<PrevisioneSpesa> list = contabilitaDAO.getPrevisioneSpesa(s.getId_contatto());

            if (list.isEmpty()) {
                throw new AssoServiceException("Nessuna previsione di spesa attiva trovata per il contatto.");
            }

            // C. Calcola la ripartizione
            // Nota: Si assume che il campo 'importo' sia gestito come Stringa nel modello
            // e che debba essere convertito per il calcolo.
            Long importoTotale = Long.valueOf(pagamento.getImporto());
            Long importoRipartito = importoTotale / list.size();

            // D. Inserisce i nuovi pagamenti ripartiti
            for (PrevisioneSpesa ps : list) {
                Pagamento p = new Pagamento();
                p.setImporto(importoRipartito.toString());
                p.setId_evento(ps.getId_evento()); // Collega il pagamento alla PREVISIONE
                p.setNote(pagamento.getNote());

                // Salva il pagamento ripartito
                contabilitaDAO.saveOrUpdatePagamento(p);
            }

            return Def.STR_OK;
        }
    }
}