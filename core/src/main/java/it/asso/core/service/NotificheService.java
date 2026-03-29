package it.asso.core.service;

import it.asso.core.common.SendPushFCM;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.notifiche.NotificheDAO;
import it.asso.core.model.notifiche.Notifica;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificheService {

    private final NotificheDAO notificheDao;
    private final SendPushFCM sendPushFCM;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public NotificheService(NotificheDAO notificheDao, SendPushFCM sendPushFCM) {
        this.notificheDao = notificheDao;
        this.sendPushFCM = sendPushFCM;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA (DELEGATI)
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Notifica> getAll(String search) throws AssoServiceException {
        return notificheDao.getAll(search);
    }

    @Transactional(readOnly = true)
    // Usiamo List<?> perché la classe EventiNotifica non è definita
    public List<?> getEventi() throws AssoServiceException {
        return notificheDao.getEventi();
    }

    @Transactional(readOnly = true)
    public Notifica getByID(String id) throws AssoServiceException {
        return notificheDao.getByID(id);
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA (TRANSAZIONALI)
    // ------------------------------------------------------------------------

    @Transactional
    public String saveOrUpdate(Notifica notifica) throws Exception {
        // La logica di persistenza è nel DAO
        return notificheDao.saveOrUpdate(notifica);
    }

    @Transactional
    public String deleteByID(String id) throws AssoServiceException {
        return notificheDao.deleteByID(id);
    }

    // ------------------------------------------------------------------------
    // LOGICA DI BUSINESS (INVIO PUSH)
    // ------------------------------------------------------------------------

    /**
     * Invia la notifica tramite il servizio Firebase Cloud Messaging (FCM).
     */
    public String sendPushNotification(Notifica notifica) throws Exception {
        // La logica di invio è nel componente SendPushFCM
        return sendPushFCM.sendNotifica(notifica);
    }
}