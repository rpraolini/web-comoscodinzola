package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.SendPushFCM; // Classe per l'invio delle notifiche push
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.gestione.ProcessoDAO;
import it.asso.core.dao.animali.storia.EventoStoricoDAO;
import it.asso.core.model.animali.storia.EventoStorico;
import it.asso.core.security.UserAuth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProcessoService {

    private final ProcessoDAO processoDao;
    private final AnimaleDAO animaleDao;
    private final EventoStoricoDAO eventoStoricoDao;
    private final SendPushFCM sendPushFCM; // Iniezione della classe per le push

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public ProcessoService(ProcessoDAO processoDao, AnimaleDAO animaleDao, EventoStoricoDAO eventoStoricoDao, SendPushFCM sendPushFCM) {
        this.processoDao = processoDao;
        this.animaleDao = animaleDao;
        this.eventoStoricoDao = eventoStoricoDao;
        this.sendPushFCM = sendPushFCM;
    }

    @Transactional
    public void registraDecesso(String idAnimale, String dtDecesso, UserAuth user) throws AssoServiceException {
        // Normalizzazione data ISO 8601 → dd/MM/yyyy (stessa logica dell'aggiorna)
        String dataFormattata = null;
        if (dtDecesso != null && !dtDecesso.isEmpty()) {
            try {
                LocalDate date = Instant.parse(dtDecesso)
                        .atZone(ZoneId.of("Europe/Rome"))
                        .toLocalDate();
                dataFormattata = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                throw new AssoServiceException("Formato data decesso non valido: " + dtDecesso);
            }
        }

        processoDao.registraDecesso(idAnimale, dataFormattata, user.getUtente());
    }

    // ------------------------------------------------------------------------
    // LOGICA DI VALIDAZIONE (valida)
    // ------------------------------------------------------------------------

    /**
     * Controlla se esistono eventi storici di origine (Def.NUM_UNO). Se esistono,
     * procede con la validazione del processo dell'animale.
     */
    @Transactional
    public String validaAnimale(String idAnimale, UserAuth user) throws AssoServiceException {
        List<EventoStorico> result = eventoStoricoDao.getEventiStoriciByIdAnimale(idAnimale);

        // Verifica se esiste almeno un evento di origine (Def.NUM_UNO)
        boolean hasOrigine = result.stream()
                .anyMatch(o -> Def.NUM_UNO.equals(o.getId_tipo_evento()));

        if (hasOrigine) {
            // Esegue la validazione nel DAO
            processoDao.valida(idAnimale, user.getUtente());
            return Def.STR_OK;
        } else {
            // Restituisce l'errore se non c'è l'evento storico di origine
            return Def.STR_ERROR_1000;
        }
    }

    // ------------------------------------------------------------------------
    // LOGICA ADOTTABILE (adottabile)
    // ------------------------------------------------------------------------

    /**
     * Imposta l'animale come adottabile e invia la notifica push.
     */
    @Transactional
    public String adottabileAnimale(String idAnimale, UserAuth user) throws Exception {
        String nomeAnimale = animaleDao.getNomeById(idAnimale);

        // 1. Logica di aggiornamento stato
        String result = processoDao.adottabile(idAnimale, user.getUtente());

        // 2. Invio della notifica push
        sendPushFCM.sendPushAdottabile(nomeAnimale);

        return result;
    }

    @Transactional
    public void revocaAdottabile(String idAnimale, UserAuth user) {
        processoDao.revocaAdottabile(idAnimale, user.getUtente());
    }


}