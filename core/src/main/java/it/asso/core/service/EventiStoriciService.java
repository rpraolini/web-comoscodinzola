package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.Utils;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.storia.EventoStoricoDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.utente.UtenteDAO;
import it.asso.core.model.animali.storia.EventoStorico;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.security.UserAuth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventiStoriciService {

    private final EventoStoricoDAO eventoStoricoDao;
    private final ContattoDAO contattoDao;
    private final AnimaleDAO animaleDao;

    // Non strettamente necessario qui, ma utile per il contesto
    private final UtenteDAO utenteDAO;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public EventiStoriciService(EventoStoricoDAO eventoStoricoDao, ContattoDAO contattoDao, AnimaleDAO animaleDao, UtenteDAO utenteDAO) {
        this.eventoStoricoDao = eventoStoricoDao;
        this.contattoDao = contattoDao;
        this.animaleDao = animaleDao;
        this.utenteDAO = utenteDAO;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI LETTURA E POPOLAMENTO (getEventiStoriciByIdAnimale)
    // ------------------------------------------------------------------------

    /**
     * Recupera gli eventi storici e popola il Contatto associato (logica migrata dal controller).
     */
    @Transactional(readOnly = true)
    public List<EventoStorico> getEventiStoriciByIdAnimale(String idAnimale) throws AssoServiceException {
        List<EventoStorico> result = eventoStoricoDao.getEventiStoriciByIdAnimale(idAnimale);

        for (EventoStorico evento : result) {
            // Logica di popolamento Contatto
            Contatto contatto = evento.getId_contatto() == null ? null : contattoDao.getByID(evento.getId_contatto(), true);
            evento.setContatto(contatto);
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI SCRITTURA E AGGIORNAMENTO PROPRIETARIO (saveOrUpdateEventoStorico)
    // ------------------------------------------------------------------------

    /**
     * Salva o aggiorna l'evento storico e aggiorna il proprietario dell'animale se l'evento è di tipo 'origine'.
     */
    @Transactional
    public String saveOrUpdateEventoStorico(EventoStorico evento, UserAuth user) throws AssoServiceException {
        String result = "";

        // Logica di pre-salvataggio migrata dal controller
        evento.setId_contatto(evento.getContatto() == null ? null : evento.getContatto().getId_contatto());
        evento.setAccount(user.getUsername());
        evento.setCt_gg(Utils.isNullOrBlank(evento.getCt_gg()) ? Def.NUM_ZERO : evento.getCt_gg());   // costo/giorno
        evento.setCt_mese(Utils.isNullOrBlank(evento.getCt_mese()) ? Def.NUM_ZERO : evento.getCt_mese()); // costo/mese

        // Salvataggio effettivo
        result = eventoStoricoDao.saveOrUpdate(evento);

        /* Aggiorno il proprietario se inserisco un evento di origine (Def.NUM_UNO) */
        if (Def.NUM_UNO.equals(evento.getId_tipo_evento())) {
            animaleDao.updateProprietario(evento.getId_animale(), evento.getId_contatto(), evento.getDt_da());
        }

        return result;
    }


}