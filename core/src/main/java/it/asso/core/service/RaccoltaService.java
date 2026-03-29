package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.MailController;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.raccolta.EventoDAO;
import it.asso.core.dao.raccolta.MerceDAO;
import it.asso.core.dao.raccolta.TurnoDAO;
import it.asso.core.model.contatto.ContattiWrapper;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.raccolta.ContattiEvento;
import it.asso.core.model.raccolta.Evento;
import it.asso.core.model.raccolta.Turno;
import it.asso.core.security.UserAuth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RaccoltaService {

    private final EventoDAO eventoDao;
    private final TurnoDAO turnoDao;
    private final ContattoDAO contattoDao;
    private final MerceDAO merceDao;
    private final MailController mailController;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public RaccoltaService(EventoDAO eventoDao, TurnoDAO turnoDao, ContattoDAO contattoDao, MerceDAO merceDao, MailController mailController) {
        this.eventoDao = eventoDao;
        this.turnoDao = turnoDao;
        this.contattoDao = contattoDao;
        this.merceDao = merceDao;
        this.mailController = mailController;
    }

    // ------------------------------------------------------------------------
    // UTILITY: POPOLAMENTO EVENTO
    // ------------------------------------------------------------------------

    private Evento populateEventoDetails(Evento evento) {
        if (evento == null) return null;

        String idEvento = evento.getId_evento();

        // 1. Popolamento Contatto (Punto di Raccolta)
        evento.setContatto(contattoDao.getPuntoRaccoltaByID(evento.getId_punto_raccolta()));

        // 2. Popolamento Turni e Contatti
        List<Turno> turni = turnoDao.getByIDEvento(idEvento);
        evento.setTurni(turni);

        for (Turno turno : turni) {
            turno.setContatti(turnoDao.getContattiByIDTurno(turno.getId_turno()));
        }

        // 3. Popolamento Merce Raccolta
        evento.setMerce(eventoDao.getMerceByIDEvento(idEvento));

        return evento;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA E RICERCA (DELEGATI)
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Evento getEventoByID(String idEvento) throws AssoServiceException {
        Evento result = eventoDao.getByID(idEvento);
        return populateEventoDetails(result);
    }

    @Transactional(readOnly = true)
    public List<Evento> getAllEventiWithContatti() throws AssoServiceException {
        List<Evento> result = eventoDao.getAll();
        for (Evento evento : result) {
            evento.setContatto(contattoDao.getPuntoRaccoltaByID(evento.getId_punto_raccolta()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Evento> getEventiBySearch(String strToSearch) throws AssoServiceException {
        List<Evento> result = new ArrayList<>();
        List<Evento> eventi = eventoDao.getAll();

        for (Evento evento : eventi) {
            Contatto c = contattoDao.getPuntoRaccoltaByID(evento.getId_punto_raccolta());
            if (c.getRag_sociale().toUpperCase().contains(strToSearch.toUpperCase())) { // Usa contains invece di indexOf >= 0
                evento.setContatto(c);
                result.add(evento);
            }
        }
        return result;
    }

    @Transactional
    public Turno saveTurno(Turno turno) throws AssoServiceException {
        // Logica di salvataggio del DAO
        String idTurno = turnoDao.saveOrUpdate(turno);
        // Recupera e popola il Turno salvato per restituirlo (se necessario per il frontend)
        return turnoDao.getByID(idTurno);
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA E LOGICA COMPLESSA (TRANSAZIONALI)
    // ------------------------------------------------------------------------

    /**
     * Salva l'Evento e gestisce la creazione automatica dei turni.
     */
    @Transactional(rollbackFor = Exception.class)
    public Evento saveEvento(Evento evento, UserAuth user) throws AssoServiceException {

        evento.setAccount(user.getUsername());
        String idEvento = eventoDao.saveOrUpdate(evento);

        // Logica di creazione automatica dei turni
        int numSettingTurni = Integer.valueOf(evento.getNumTurni() == null ? "1" : evento.getNumTurni());

        List<Turno> existingTurni = evento.getTurni();
        int numEffectiveTurni = existingTurni != null ? existingTurni.size() : 0;

        if (numSettingTurni > numEffectiveTurni) {
            int cicli = numSettingTurni - numEffectiveTurni;
            for (int i = 0; cicli > 0; i++) {
                Turno turno = new Turno();
                turno.setId_evento(idEvento);
                turnoDao.saveOrUpdate(turno);
                cicli--;
            }
        } else if (existingTurni == null && numSettingTurni > 0) {
            for (int i = 0; numSettingTurni > 0; i++) {
                Turno turno = new Turno();
                turno.setId_evento(idEvento);
                turnoDao.saveOrUpdate(turno);
                numSettingTurni--;
            }
        }

        return getEventoByID(idEvento);
    }

    /**
     * Salva un contatto nel turno e popola il risultato.
     */
    @Transactional
    public Turno saveContatto(String idTurno, String idContatto, String idEvento) throws AssoServiceException {
        ContattiEvento ce = new ContattiEvento();
        ce.setId_contatto(idContatto);
        ce.setId_evento(idEvento);
        ce.setId_turno(idTurno);
        eventoDao.saveOrUpdate(ce);

        return turnoDao.getByID(idTurno);
    }

    /**
     * Salva la merce raccolta per un evento.
     */
    @Transactional(rollbackFor = Exception.class)
    public Evento saveProdottoRaccolta(String idEvento, String idMerce, String quantita, String pesoTot) throws AssoServiceException {
        eventoDao.saveOrUpdate(idEvento, idMerce, quantita, pesoTot);
        return getEventoByID(idEvento);
    }

    /**
     * Elimina la merce raccolta per un evento.
     */
    @Transactional(rollbackFor = Exception.class)
    public Evento deleteProdottoRaccolta(String idEvento, String idMerce) throws AssoServiceException, SQLIntegrityConstraintViolationException {
        eventoDao.deleteEventoMerce(idEvento, idMerce);
        return getEventoByID(idEvento);
    }

    // ------------------------------------------------------------------------
    // LOGICA DI INVIO MAIL
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public String inviaMailRaccoltaCibo(ContattiWrapper contattiWrapper, UserAuth user, HttpServletRequest request) throws AssoServiceException {
        try {
            Evento evento = contattiWrapper.getEvento();
            Contatto puntoDiRaccolta = contattoDao.getPuntoRaccoltaByID(evento.getId_punto_raccolta());
            for (Contatto c : contattiWrapper.getContatti()) {
                // MailController dovrà essere aggiornato per non dipendere direttamente da HttpServletRequest per il tenant
                mailController.sendMailToRaccoltaCiboHtml(c, puntoDiRaccolta, evento, user, request);
            }
            return Def.STR_OK;
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public String inviaMailRingraziamentoRaccoltaCibo(ContattiWrapper contattiWrapper, UserAuth user, HttpServletRequest request) throws AssoServiceException {
        try {
            Evento evento = contattiWrapper.getEvento();
            Contatto puntoDiRaccolta = contattoDao.getPuntoRaccoltaByID(evento.getId_punto_raccolta());
            for (Contatto c : contattiWrapper.getContatti()) {
                mailController.sendMailRingraziamentoForRaccoltaCiboHtml(c, puntoDiRaccolta, evento, user, request);
            }
            return Def.STR_OK;
        } catch (Exception e) {
            throw new AssoServiceException(Def.STR_ERROR_000 + e.getMessage());
        }
    }
}