package it.asso.core.service;

import it.asso.core.common.MailController;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.gestione.IterDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.questionario.QuestionarioDAO;
import it.asso.core.dao.organizzazione.OrganizzazioneDAO; // Necessario per getOrganizzazioneByTenant
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.animali.gestione.IterCompleto;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.organizzazione.Organizzazione;
import it.asso.core.model.questionario.Questionario;
import it.asso.core.model.questionario.QuestionarioSezioni;
import it.asso.core.multitenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionarioService {

    private final IterDAO richiestaDao;
    private final ContattoDAO referentiDao;
    private final QuestionarioDAO questionarioDAO;
    private final AnimaleDAO animaleDao;
    private final MailController mailController;
    private final OrganizzazioneDAO organizzazioneDao; // DAO per recuperare l'organizzazione

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public QuestionarioService(IterDAO richiestaDao, ContattoDAO referentiDao, QuestionarioDAO questionarioDAO, AnimaleDAO animaleDao, MailController mailController, OrganizzazioneDAO organizzazioneDao) {
        this.richiestaDao = richiestaDao;
        this.referentiDao = referentiDao;
        this.questionarioDAO = questionarioDAO;
        this.animaleDao = animaleDao;
        this.mailController = mailController;
        this.organizzazioneDao = organizzazioneDao;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI LETTURA E POPOLAMENTO
    // ------------------------------------------------------------------------

    /**
     * Recupera l'Iter (richiesta) e popola i dettagli del Contatto (preaffidante/volontaria).
     */
    @Transactional(readOnly = true)
    public Iter getRichiestaByKey(String key) throws AssoServiceException {
        Iter richiesta = richiestaDao.getIterByKey(key);
        if(richiesta != null) {
            // Popolamento Contatti
            Contatto preaffidante = referentiDao.getByID(richiesta.getId_contatto(), true);
            Contatto volontaria = referentiDao.getByID(richiesta.getId_contatto_vol());
            richiesta.setContatto(preaffidante);
            richiesta.setVolontaria(volontaria);
        }
        return richiesta;
    }

    /**
     * Recupera le sezioni del questionario e popola le domande (logica incrociata).
     */
    @Transactional(readOnly = true)
    public List<QuestionarioSezioni> getQuestionarioSezioni(String idRichiesta) throws AssoServiceException {
        List<QuestionarioSezioni> questionarioSezioni = questionarioDAO.getSezioniByIdRichiesta(idRichiesta);

        for (QuestionarioSezioni qs : questionarioSezioni) {
            qs.setQuestionario(questionarioDAO.getQuestionarioByIdRichiestaAndSezione(idRichiesta, qs.getId_sezione()));
        }
        return questionarioSezioni;
    }

    /**
     * Recupera l'oggetto Organizzazione necessario per il report PDF.
     */
    @Transactional(readOnly = true)
    public Organizzazione getOrganizzazioneByTenant(String tenant) throws AssoServiceException {
        // Assume che il DAO abbia un metodo per recuperare l'Org usando il tenant ID
        return organizzazioneDao.getByTenant(tenant);
    }

    // ------------------------------------------------------------------------
    // LOGICA DI SCRITTURA E INVIO (TRANSAZIONALE)
    // ------------------------------------------------------------------------

    /**
     * Salva le risposte del questionario.
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionarioSezioni(List<QuestionarioSezioni> sezioni) throws AssoServiceException {
        for (QuestionarioSezioni qs : sezioni) {
            for(Questionario q : qs.getQuestionario()) {
                if(q.getRisposta() != null) {
                    questionarioDAO.updateQuestionario(q);
                }
            }
        }
    }

    /**
     * Salva le risposte e invia la notifica di questionario compilato.
     */
    @Transactional(rollbackFor = Exception.class)
    public void inviaQuestionario(IterCompleto richiestaCompleta) throws AssoServiceException {
        Iter richiesta = richiestaCompleta.getIter();

        // 1. Salvataggio risposte (simile a saveQuestionarioSezioni)
        for (QuestionarioSezioni qs : richiestaCompleta.getSezioni()) {
            for(Questionario q : qs.getQuestionario()) {
                if(q.getRisposta() != null) {
                    questionarioDAO.updateQuestionario(q);
                }
            }
        }

        // 2. Aggiornamento stato del questionario
        questionarioDAO.inviaQuestionario(richiesta.getId_iter());

        // 3. Invio mail di conferma (Logica migrata dal Controller)
        String tenantId = TenantContext.getCurrentTenant();

        if (tenantId == null || tenantId.isEmpty() || "default".equals(tenantId)) {
            // Fallback: Tentiamo di risolverlo dal DAO o lanciamo un errore
            throw new AssoServiceException("Impossibile inviare la mail: Tenant ID non risolto dal contesto.");
        }

        Animale animale = animaleDao.getById(richiesta.getId_animale());
        mailController.sendConfermaQuestionarioCompilato(richiesta, animale, tenantId);
    }
}