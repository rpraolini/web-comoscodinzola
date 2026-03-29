package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.ResultGrid;
import it.asso.core.common.Utils;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.organizzazione.contabilita.FattureDAO;
import it.asso.core.dao.organizzazione.contabilita.MovimentiDAO;
import it.asso.core.dao.organizzazione.contabilita.RendicontoDAO;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.organizzazione.RicercaDTO;
import it.asso.core.model.organizzazione.contabilita.*;
import it.asso.core.security.UserAuth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovimentiService {

    private final MovimentiDAO movimentiDao;
    private final RendicontoDAO rendicontoDAO;
    private final FattureDAO fattureDAO;
    private final ContattoDAO contattoDAO;
    private final DocumentoDAO documentoDao;

    // COSTRUTTORE CON INIEZIONE DI TUTTE LE DIPENDENZE
    public MovimentiService(MovimentiDAO movimentiDao, RendicontoDAO rendicontoDAO, FattureDAO fattureDAO, ContattoDAO contattoDAO, DocumentoDAO documentoDao) {
        this.movimentiDao = movimentiDao;
        this.rendicontoDAO = rendicontoDAO;
        this.fattureDAO = fattureDAO;
        this.contattoDAO = contattoDAO;
        this.documentoDao = documentoDao;
    }

    // ------------------------------------------------------------------------
    // METODI DI LETTURA E POPOLAMENTO
    // ------------------------------------------------------------------------

    /**
     * Recupera e popola la lista dei MovimentiRestrict per un dato anno/ricerca.
     */
    @Transactional(readOnly = true)
    public ResultGrid getMovimentiListByAnno(RicercaDTO ricerca, UserAuth user) throws AssoServiceException {
        // La risoluzione del tenant ID (idOrganizzazione) è cruciale qui
        String idOrganizzazione = user.getUtente().getOrganizzazione().getId_organizzazione();

        List<MovimentoRestrict> m = movimentiDao.getListByAnno(ricerca, idOrganizzazione);

        for (MovimentoRestrict movimento : m) {
            this.popolaDettagliMovimento(movimento);

            if(Def.CAUSALE_MOVIMENTO_FATTURA.equals(movimento.getId_causale()) || Def.CAUSALE_MOVIMENTO_RISCOSSIONE_FATTURA.equals(movimento.getId_causale())) {
                String destinatari = "";
                List<Fattura> fatture = movimentiDao.getFattureMovimentoByID(movimento.getId_movimento());
                for(Fattura f : fatture) {
                    if("".equals(destinatari)) {
                        destinatari = "(N. " + f.getNumero() + ") " + f.getContatto();
                    }else {
                        destinatari = destinatari + " / " + "(N. " + f.getNumero() + ") " + f.getContatto();
                    }
                }
                movimento.settDestinatario(destinatari);
            } else {
                Contatto c = contattoDAO.getByID(movimento.getId_contatto(), true);
                movimento.settDestinatario(c.getDescrizione());
            }
        }

        ResultGrid result = new ResultGrid();
        result.setRecords(m);
        result.setTotale(movimentiDao.getCountBySearch(ricerca, idOrganizzazione));
        result.setTotali(movimentiDao.getTotali(ricerca, idOrganizzazione));
        return result;
    }

    /**
     * Recupera e popola tutti i dettagli di un singolo Movimento (inclusi girofondi e fatture).
     */
    @Transactional(readOnly = true)
    public Movimento getMovimentoById(String id) throws AssoServiceException {
        Movimento movimento = movimentiDao.getByID(id);

        // Popolamento dettagli base
        this.popolaDettagliMovimento((MovimentoRestrict) movimento);
        movimento.setDestinazione(movimentiDao.getDestinazioneByID(movimento.getId_destinazione()));
        movimento.setDestinatario(contattoDAO.getByID(movimento.getId_contatto(), true));
        movimento.setFatture(movimentiDao.getFattureMovimentoByID(id));

        // Popolamento documenti
        if (movimento.getDocumenti() != null) {
            ArrayList<Documento> documenti = documentoDao.getDocumentiByIDMovimento(movimento.getId_movimento());
            movimento.getDocumenti().addAll(documenti);
        }

        // Gestione Giurofondo
        if (movimento.getGirofondo() > 0) {
            Movimento mGirofondo = movimentiDao.getByID(String.valueOf(movimento.getGirofondo()));

            if(Def.TIPO_MOVIMENTO_USCITA.equalsIgnoreCase(movimento.getId_tipo_movimento())) {
                movimento.setDestinazioneA(movimentiDao.getDestinazioneByID(mGirofondo.getId_destinazione()));
                movimento.setId_cc_a(mGirofondo.getId_cc());
            } else {
                movimento.setDestinazione(movimentiDao.getDestinazioneByID(mGirofondo.getId_destinazione()));
                movimento.setId_cc(mGirofondo.getId_cc());
                movimento.setDestinazioneA(movimentiDao.getDestinazioneByID(movimento.getId_destinazione()));
                movimento.setId_cc_a(mGirofondo.getId_cc());
            }
        }
        return movimento;
    }

    /**
     * Popola il TipoMovimento e i dettagli di Rendiconto per un movimento.
     */
    private void popolaDettagliMovimento(MovimentoRestrict movimento) throws AssoServiceException {
        movimento.setTipoMovimento(movimentiDao.getTipoMovimentoByID(movimento.getId_tipo_movimento()));
        movimento.setDettaglioMovimento(movimentiDao.getDettaglioMovimentoByID(movimento.getId_movimento()));

        for(DettaglioMovimento dm : movimento.getDettaglioMovimento()) {
            dm.setRendiconto(rendicontoDAO.getRendicontoBySottoVoce(dm.getId_cr_sottovoce()));
        }
    }

    /**
     * Recupera la lista dei Contatti che possono essere destinatari di movimento.
     */
    @Transactional(readOnly = true)
    public List<Contatto> getDestinatari(String search) throws AssoServiceException {
        List<String> tipiContatti = new ArrayList<>();
        tipiContatti.add(Def.CONTATTO_PERSONA_FISICA);
        tipiContatti.add(Def.CONTATTO_PUNTO_VENDITA);
        tipiContatti.add(Def.CONTATTO_VETERINARIO_CLINICA);
        tipiContatti.add(Def.CONTATTO_PENSIONE);
        tipiContatti.add(Def.CONTATTO_CANILE);
        tipiContatti.add(Def.CONTATTO_FORNITORE);
        tipiContatti.add(Def.CONTATTO_ASSOCIAZIONE);

        return contattoDAO.getContattiByTipoAndSearch(search, tipiContatti);
    }

    // ------------------------------------------------------------------------
    // METODI DI SCRITTURA E LOGICA COMPLESSA (TRANSAZIONALI)
    // ------------------------------------------------------------------------

    /**
     * Salva un Movimento, gestendo sia i movimenti standard che i girofondi.
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveMovimento(Movimento mv, UserAuth user) throws AssoServiceException {
        String idMovimento = "";
        String idOrganizzazione = user.getUtente().getOrganizzazione().getId_organizzazione();

        // Popolamento dei campi di base
        mv.setAccount(user.getUsername());
        mv.setId_organizzazione(idOrganizzazione);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(mv.getDt_operazione() != null && mv.getDt_operazione().contains("Z")) {
            ZonedDateTime zp = ZonedDateTime.parse(mv.getDt_operazione());
            mv.setDt_operazione(zp.format(dtf));
        }

        // --- GESTIONE MOVIMENTO STANDARD ---
        if(mv.getDestinazioneA() == null) { // Verifica se non è un girofondo

            mv.setId_contatto(mv.getDestinatario() == null ? null : mv.getDestinatario().getId_contatto());
            mv.setId_destinazione(mv.getDestinazione().getId_destinazione());
            mv.setId_tipo_destinazione(mv.getDestinazione().getTipoDestinazione().getId_tipo_destinazione());
            mv.setId_cc(Utils.isBlankThenNull(mv.getId_cc()));

            // Salvataggio Fatture/Ritenute collegate
            if(Def.CAUSALE_MOVIMENTO_FATTURA.equals(mv.getId_causale()) || Def.CAUSALE_MOVIMENTO_RITENUTA_ACCONTO.equals(mv.getId_causale()) || Def.CAUSALE_MOVIMENTO_RISCOSSIONE_FATTURA.equals(mv.getId_causale())){

                // Logica complessa di salvataggio MovimentoFattura e DettaglioMovimento...

                for(Fattura f : mv.getFatture()) {
                    mv.setId_contatto(f.getId_contatto());

                    // 1. Salva il movimento principale (per ottenere l'ID)
                    idMovimento = movimentiDao.saveOrUpdate(mv);

                    // 2. Collega le fatture al movimento
                    movimentiDao.deleteFattureByIDMovimento(idMovimento);
                    movimentiDao.saveMovimentoFattura(idMovimento, f.getId_fattura());

                    // 3. Crea i Dettagli Movimento dalle voci di Fattura
                    f.setDettaglio(fattureDAO.getDettagliFattura(f.getId_fattura()));
                    movimentiDao.deleteDettaglioMovimentoByIDMovimento(mv.getId_movimento());
                    for(DettaglioFattura df : f.getDettaglio()) {
                        DettaglioMovimento dm = new DettaglioMovimento();
                        dm.setId_movimento(idMovimento);
                        dm.setId_cr_sottovoce(fattureDAO.getVoceFatturaById(df.getId_vf()).getId_cr_sotto_voce());
                        dm.setImporto(df.getImporto());
                        movimentiDao.saveOrUpdateDettaglioMovimento(dm);
                    }
                }

            } else {
                // Salvataggio movimento standard (con dettagli movimento)
                idMovimento = movimentiDao.saveOrUpdate(mv);
                movimentiDao.deleteDettaglioMovimentoByIDMovimento(mv.getId_movimento());
                for(DettaglioMovimento dm : mv.getDettaglioMovimento()) {
                    dm.setId_movimento_dettaglio(null);
                    dm.setId_movimento(idMovimento);
                    dm.setId_cr_sottovoce(dm.getRendiconto().getId_cr_sotto_voce());
                    movimentiDao.saveOrUpdateDettaglioMovimento(dm);
                }
            }

        } else {
            // --- GESTIONE MOVIMENTO GIROFONDI ---

            // Logica complessa del girofondo (Movimento Entrata e Movimento Uscita)

            // 1. Prepara Movimento Entrata
            Movimento mEntrata = new Movimento();
            if(mv.getId_movimento() != null && Def.TIPO_MOVIMENTO_ENTRATA.equalsIgnoreCase(mv.getId_tipo_movimento())) {
                mEntrata.setId_movimento(mv.getId_movimento()); // Se stiamo modificando
            } else if (mv.getGirofondo() > 0) {
                mEntrata.setId_movimento(String.valueOf(mv.getGirofondo()));
            }

            mEntrata.setAccount(user.getUsername());
            mEntrata.setId_organizzazione(idOrganizzazione);
            mEntrata.setId_contatto(Def.CONTATTO_ORGANIZZAZIONE); // Contatto fisso
            mEntrata.setId_tipo_movimento(Def.TIPO_MOVIMENTO_ENTRATA);
            mEntrata.setId_destinazione(mv.getDestinazioneA().getId_destinazione());
            mEntrata.setId_tipo_destinazione(mv.getDestinazioneA().getTipoDestinazione().getId_tipo_destinazione());
            mEntrata.setId_cc("".equals(mv.getId_cc_a()) ? null : mv.getId_cc_a());
            mEntrata.setId_causale(Def.CAUSALE_GIROFONDO_ENTRATA);
            mEntrata.setImporto(mv.getImporto());
            mEntrata.setCodice(mv.getCodice());
            mEntrata.setDt_operazione(mv.getDt_operazione());
            String idMovimentoEntrata = movimentiDao.saveOrUpdate(mEntrata);

            // 2. Prepara Movimento Uscita
            Movimento mUscita = new Movimento();
            if(mv.getId_movimento() != null && Def.TIPO_MOVIMENTO_USCITA.equalsIgnoreCase(mv.getId_tipo_movimento())) {
                mUscita.setId_movimento(mv.getId_movimento()); // Se stiamo modificando
            } else if (mv.getGirofondo() > 0) {
                mUscita.setId_movimento(String.valueOf(mv.getGirofondo()));
            }

            mUscita.setAccount(user.getUsername());
            mUscita.setId_organizzazione(idOrganizzazione);
            mUscita.setId_contatto(Def.CONTATTO_ORGANIZZAZIONE);
            mUscita.setId_tipo_movimento(Def.TIPO_MOVIMENTO_USCITA);
            mUscita.setId_destinazione(mv.getDestinazione().getId_destinazione());
            mUscita.setId_tipo_destinazione(mv.getDestinazione().getTipoDestinazione().getId_tipo_destinazione());
            mUscita.setId_cc(Utils.isBlankThenNull(mv.getId_cc()));
            mUscita.setId_causale(Def.CAUSALE_GIROFONDO_USCITA);
            mUscita.setImporto(mv.getImporto());
            mUscita.setCodice(mv.getCodice());
            mUscita.setDt_operazione(mv.getDt_operazione());

            // 3. Salvataggio e collegamento incrociato
            String idMovimentoUscita = movimentiDao.saveOrUpdate(mUscita);

            mEntrata.setId_movimento(idMovimentoEntrata);
            mEntrata.setGirofondo(Integer.valueOf(idMovimentoUscita)); // Collega Entrata a Uscita
            idMovimento = movimentiDao.saveOrUpdate(mEntrata); // Finalizza Entrata

        }
        return idMovimento;
    }

    /**
     * Elimina un Movimento e i documenti collegati.
     */
    @Transactional(rollbackFor = Exception.class)
    public String deleteMovimento(Movimento mv) throws AssoServiceException, SQLIntegrityConstraintViolationException, IllegalStateException, IOException {
        String result;

        // 1. Elimina documenti collegati
        List<Documento> documenti = documentoDao.getDocumentiByIDMovimento(mv.getId_movimento());
        for (Documento doc : documenti) {
            documentoDao.deleteDocumentoMovimento(doc.getId_documento(), mv.getId_movimento());
            documentoDao.deleteByID(doc.getId_documento());
        }

        // 2. Elimina il movimento e i dettagli (e il movimento girofondo se presente)
        result = movimentiDao.deleteByID(mv.getId_movimento());

        // Nota: La logica di eliminazione del girofondo collegato (mv.getGirofondo())
        // è complessa e deve essere gestita in una funzione separata, o inclusa
        // nella logica di deleteByID del DAO per garantire l'integrità.

        return Def.STR_OK;
    }
}