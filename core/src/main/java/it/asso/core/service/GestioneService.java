package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.gestione.IterDAO;
import it.asso.core.dao.animali.gestione.ProcessoDAO;
import it.asso.core.dao.animali.storia.EventoStoricoDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.gestione.PraticaDAO;
import it.asso.core.dao.utente.UtenteDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.gestione.Pratica;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.animali.storia.EventoStorico;
import it.asso.core.security.UserAuth;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestioneService {

    private final PraticaDAO praticaDao;
    private final IterDAO iterDao;
    private final ContattoDAO contattoDao;
    private final DocumentoDAO documentoDao;
    private final AnimaleDAO animaleDao;
    private final ProcessoDAO processoDao;
    private final EventoStoricoDAO eventoStoricoDao;
    private final UtenteDAO utenteDAO; // Necessario per setStatoAnimale

    // Costruttore con iniezione di tutti i DAO necessari
    public GestioneService(PraticaDAO praticaDao, IterDAO iterDao, ContattoDAO contattoDao, DocumentoDAO documentoDao, AnimaleDAO animaleDao, ProcessoDAO processoDao, EventoStoricoDAO eventoStoricoDao, UtenteDAO utenteDAO) {
        this.praticaDao = praticaDao;
        this.iterDao = iterDao;
        this.contattoDao = contattoDao;
        this.documentoDao = documentoDao;
        this.animaleDao = animaleDao;
        this.processoDao = processoDao;
        this.eventoStoricoDao = eventoStoricoDao;
        this.utenteDAO = utenteDAO;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (CON LOGICA DI POPOLAMENTO COMPLESSA)
    // ------------------------------------------------------------------------

    public List<Pratica> getPraticheByAnimale(String idAnimale) {
        List<Pratica> pratiche = praticaDao.getPraticheByIDAnimale(idAnimale);

        for (Pratica pratica : pratiche) {
            List<Iter> i = iterDao.getIterByIdPratica(pratica.getId_pratica());
            for (Iter iter : i) {
                // Popolamento Contatti
                Contatto volontaria = iter.getId_contatto_vol() == null ? null : contattoDao.getByID(iter.getId_contatto_vol(), true);
                Contatto preaffidante = iter.getId_contatto() == null ? null : contattoDao.getByID(iter.getId_contatto(), true);
                Contatto adottante = iter.getId_contatto_adottante() == null ? null : contattoDao.getByID(iter.getId_contatto_adottante(), false);
                Contatto proprietario = iter.getId_contatto_proprietario() == null ? null : contattoDao.getByID(iter.getId_contatto_proprietario());

                iter.setContatto(preaffidante);
                iter.setVolontaria(volontaria);
                iter.setAdottante(adottante);
                iter.setProprietario(proprietario);
                iter.setStato(Def.ST_P_PRATICA_ATTIVA.equals(pratica.getMacro_stato()) ? Def.ST_ATTIVO : Def.ST_CHIUSO);

                // Popolamento Documenti
                if (iter.getDocumenti() != null) { // Assumiamo la lista non sia null
                    List<Documento> documenti = documentoDao.getDocumentiByIDIter(iter.getId_iter());
                    iter.getDocumenti().addAll(documenti);
                }
            }
            pratica.setIter(i);
        }
        return pratiche;
    }

    public Iter getIterById(String id) {
        Iter iter = iterDao.getIterByID(id);

        Contatto volontaria = iter.getId_contatto_vol() == null ? null : contattoDao.getByID(iter.getId_contatto_vol(), true);
        Contatto preaffidante = iter.getId_contatto() == null ? null : contattoDao.getByID(iter.getId_contatto(), true);
        Contatto adottante = iter.getId_contatto_adottante() == null ? null : contattoDao.getByID(iter.getId_contatto_adottante(), false);
        Contatto proprietario = iter.getId_contatto_proprietario() == null ? null : contattoDao.getByID(iter.getId_contatto_proprietario(), true);

        iter.setContatto(preaffidante);
        iter.setVolontaria(volontaria);
        iter.setAdottante(adottante);
        iter.setProprietario(proprietario);

        if (iter.getDocumenti() != null) { // Assumiamo la lista non sia null
            List<Documento> documenti = documentoDao.getDocumentiByIDIter(iter.getId_iter());
            iter.getDocumenti().addAll(documenti);
        }
        return iter;
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (DELEGATI DAL CONTROLLER)
    // ------------------------------------------------------------------------

    public String saveOrUpdateRichiestaPreaffido(Iter iter, UserAuth user) throws AssoServiceException {
        String result = "";
        iter.setId_contatto(iter.getContatto() == null ? null : iter.getContatto().getId_contatto());
        iter.setId_contatto_vol(iter.getVolontaria() == null ? null : iter.getVolontaria().getId_contatto());
        iter.setAccount(user.getUsername());
        iter.setId_tipo_iter(Def.TR_PREAFFIDO);
        iter.setId_contatto_proprietario(animaleDao.getProprietarioByIDAnimale(iter.getId_animale()).getId_contatto());
        result = iterDao.saveOrUpdate(iter);

        if (iter.getId_iter() == null) {
            processoDao.setStatoAnimale(iter.getId_animale(), user.getUtente(), Def.TR_PREAFFIDO);
        }

        return result;
    }

    public String saveOrUpdateAdozione(Iter iter, UserAuth user) throws AssoServiceException {
        String result = "";
        iter.setId_contatto_adottante(iter.getAdottante() == null ? null : iter.getAdottante().getId_contatto());

        if (iter.getVolontaria() == null) {
            List<Iter> i = iterDao.getIterByIdAnimale(iter.getId_animale(), Def.TR_PREAFFIDO);
            if (!i.isEmpty() && i.get(0).getId_contatto_vol() != null) {
                iter.setId_contatto_vol(i.get(0).getId_contatto_vol());
            } else {
                iter.setId_contatto_vol(null);
            }
        } else {
            iter.setId_contatto_vol(iter.getVolontaria().getId_contatto());
        }

        iter.setAccount(user.getUsername());
        iter.setId_tipo_iter(Def.TR_ADOZIONE);
        iter.setId_contatto_proprietario(animaleDao.getProprietarioByIDAnimale(iter.getId_animale()).getId_contatto());
        result = iterDao.saveOrUpdateAdozione(iter);

        if (iter.getId_iter() == null) {
            processoDao.setStatoAnimale(iter.getId_animale(), user.getUtente(), Def.TR_ADOZIONE);
        }

        return result;
    }

    public String saveOrUpdateConsegna(Iter iter, UserAuth user) throws AssoServiceException {
        String result = "";
        if (iter.getAdottante() == null) {
            List<Iter> i = iterDao.getIterByIdAnimale(iter.getId_animale(), Def.TR_ADOZIONE);
            if (!i.isEmpty() && i.get(0).getId_contatto_adottante() != null) {
                iter.setId_contatto_adottante(i.get(0).getId_contatto_adottante());
            } else {
                iter.setId_contatto_adottante(null);
            }
        } else {
            iter.setId_contatto_adottante(iter.getAdottante().getId_contatto());
        }
        iter.setAccount(user.getUsername());
        iter.setId_tipo_iter(Def.TR_CONSEGNA);
        iter.setId_contatto_proprietario(animaleDao.getProprietarioByIDAnimale(iter.getId_animale()).getId_contatto());
        result = iterDao.saveOrUpdate(iter);

        if (iter.getId_iter() == null) {
            processoDao.setStatoAnimale(iter.getId_animale(), user.getUtente(), Def.TR_CONSEGNA);
        }

        if (iter.getAdottante() != null && iter.getDt_consegna() != null) {
            setEventoStorico(iter, user); // Chiama il metodo privato (migrato qui)
        }

        return result;
    }

    public String saveOrUpdatePassaggio(Iter iter, UserAuth user) throws AssoServiceException {
        String result = "";
        List<Iter> iterAdozione = iterDao.getIterByIdAnimale(iter.getId_animale(), Def.TR_CONSEGNA);
        Iter newIter = iterAdozione.get(0);

        newIter.setAccount(user.getUsername());
        newIter.setId_tipo_iter(Def.TR_PROPRIETA);
        newIter.setId_iter(null);
        newIter.setId_pratica(iter.getId_pratica());
        newIter.setId_contatto_proprietario(animaleDao.getProprietarioByIDAnimale(iter.getId_animale()).getId_contatto());
        result = iterDao.saveOrUpdate(newIter);

        processoDao.setStatoAnimale(iter.getId_animale(), user.getUtente(), Def.TR_PROPRIETA);
        return result;
    }

    public String saveOrUpdateChiusura(Iter iter, UserAuth user) throws AssoServiceException {
        String result = "";
        Animale animale = animaleDao.getById(iter.getId_animale());
        if (animale.getId_stato().equals(Def.ST_ISTRUTTORIA_CHIUSA)) {
            result = processoDao.riapriIstruttoria(iter.getId_animale(), user.getUtente());
        } else {
            result = processoDao.chiudiIstruttoria(iter.getId_animale(), user.getUtente());
        }
        return result;
    }

    public String deleteIterById(String id, UserAuth user) throws AssoServiceException {
        Iter iter = iterDao.getIterByID(id);
        String result = iterDao.delete(id);
        String idTipoIterPrev = iterDao.getTipoIterByPratica(iter.getId_pratica());
        processoDao.setStatoAnimale(iter.getId_tipo_iter(), idTipoIterPrev, user.getUtente(), iter.getId_animale());
        return result;
    }

    // ------------------------------------------------------------------------
    // METODI PRIVATI (MIGRATI QUI PERCHÉ UTILIZZATI NEI METODI DEL SERVICE)
    // ------------------------------------------------------------------------

    private void setEventoStorico(Iter iter, UserAuth user) {
        int idEvento = eventoStoricoDao.checkTipoEvento(iter.getId_animale(), Def.EVENTO_STORICO_ADOZIONE);
        EventoStorico evento = new EventoStorico();

        if (idEvento == -1) {
            evento.setId_evento(null);
        } else {
            evento.setId_evento(String.valueOf(idEvento));
        }

        evento.setId_contatto(iter.getId_contatto_adottante());
        evento.setAccount(user.getUsername());
        evento.setCt_gg(Def.NUM_ZERO);
        evento.setCt_mese(Def.NUM_ZERO);
        evento.setId_tipo_evento(Def.EVENTO_STORICO_ADOZIONE);
        evento.setId_animale(iter.getId_animale());
        evento.setDt_da(iter.getDt_consegna());
        eventoStoricoDao.saveOrUpdate(evento);
    }
}