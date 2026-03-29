package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.gestione.IterDAO;
import it.asso.core.dao.animali.gestione.ProcessoDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.gestione.PraticaDAO;
import it.asso.core.dao.questionario.QuestionarioDAO;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.animali.gestione.TipoIter;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.gestione.Pratica;
import it.asso.core.security.UserAuth;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdozioneService {

    private static final List<String> SEQUENZA_ITER = List.of("1", "2", "3", "4");

    private final IterDAO iterDao;
    private final PraticaDAO praticaDao;
    private final ProcessoDAO processoDao;
    private final ContattoDAO contattoDao;
    private final DocumentoDAO documentoDao;
    private final FileService fileService;
    private final AnimaleDAO animaleDao;
    private final QuestionarioDAO questionarioDao;

    public AdozioneService(IterDAO iterDao, PraticaDAO praticaDao, ProcessoDAO processoDao,
                           ContattoDAO contattoDao, DocumentoDAO documentoDao, FileService fileService,
                           AnimaleDAO animaleDao, QuestionarioDAO questionarioDao) {
        this.iterDao = iterDao;
        this.praticaDao = praticaDao;
        this.processoDao = processoDao;
        this.contattoDao = contattoDao;
        this.documentoDao = documentoDao;
        this.fileService = fileService;
        this.animaleDao = animaleDao;
        this.questionarioDao = questionarioDao;
    }

    // ------------------------------------------------------------------------
    // LETTURA
    // ------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Pratica> getPraticheByAnimale(String idAnimale) {
        List<Pratica> pratiche = praticaDao.getPraticheByIDAnimale(idAnimale);
        if (pratiche == null) return Collections.emptyList();
        Contatto proprietario = animaleDao.getProprietarioByIDAnimale(idAnimale);
        pratiche.forEach(p -> {
            List<Iter> iters = iterDao.getIterByIdPratica(p.getId_pratica());
            if (iters != null) {
                iters.forEach(this::popolaContatti);
                iters.forEach(iter -> iter.setProprietario(proprietario));
                // Popola documenti per ogni iter
                iters.forEach(iter -> {
                    List<Documento> docs = documentoDao.getDocumentiByIDIter(iter.getId_iter());
                    iter.setDocumenti(docs != null ? docs : new ArrayList<>());
                });
            }
            p.setIter(iters != null ? iters : new ArrayList<>());
        });
        return pratiche;
    }

    public List<TipoIter> getTipiIter() {
        List<TipoIter> tipi = iterDao.getTipiIter();
        return tipi != null ? tipi : Collections.emptyList();
    }

    // ------------------------------------------------------------------------
    // PRATICA
    // ------------------------------------------------------------------------

    @Transactional
    public Pratica nuovaPratica(String idAnimale, UserAuth user) throws AssoServiceException {
        // Verifica che non esista già una pratica attiva
        String praticaAttiva = praticaDao.getExistPraticaAttivaByIDAnimale(idAnimale);
        if (praticaAttiva != null) {
            throw new AssoServiceException("Esiste già una pratica attiva per questo animale. Chiuderla prima di aprirne una nuova.");
        }

        Pratica pratica = new Pratica();
        pratica.setId_animale(idAnimale);
        pratica.setId_stato(Def.ST_P_PRATICA_APERTA); // stato "aperta"
        pratica.setAccount(user.getUsername());
        praticaDao.saveOrUpdate(pratica);
        return praticaDao.getByID(pratica.getId_pratica());
    }

    @Transactional
    public void chiudiPratica(String idPratica) {
        praticaDao.close(idPratica);
    }

    @Transactional
    public void chiudiIstruttoria(String idAnimale, UserAuth user) {
        processoDao.chiudiIstruttoria(idAnimale, user.getUtente());
    }

    @Transactional
    public void eliminaPratica(String idPratica, String idAnimale, UserAuth user) throws AssoServiceException {
        Pratica pratica = praticaDao.getByID(idPratica);
        if (pratica == null) throw new AssoServiceException("Pratica non trovata");

        // Elimina tutti gli iter della pratica e retrocede lo stato
        List<Iter> iters = iterDao.getIterByIdPratica(idPratica);
        if (iters != null) {
            iters.forEach(iter -> iterDao.delete(iter.getId_iter()));
        }
        praticaDao.deleteByID(idPratica);
    }

    // ------------------------------------------------------------------------
    // ITER
    // ------------------------------------------------------------------------

    @Transactional
    public String aggiungiIter(Iter iter, UserAuth user) throws AssoServiceException {
        Pratica pratica = praticaDao.getByID(iter.getId_pratica());
        if (pratica == null) throw new AssoServiceException("Pratica non trovata");
        if (!pratica.isAperta()) throw new AssoServiceException("La pratica è chiusa. Non è possibile aggiungere iter.");

        // Recupera iter esistenti nella pratica
        List<Iter> esistenti = iterDao.getIterByIdPratica(iter.getId_pratica());
        if (esistenti == null) esistenti = new ArrayList<>();

        // Verifica duplicati
        boolean duplicato = esistenti.stream()
                .anyMatch(e -> e.getId_tipo_iter().equals(iter.getId_tipo_iter()));
        if (duplicato) throw new AssoServiceException("Questo tipo di iter è già presente nella pratica.");

        // Verifica sequenza
        if (!esistenti.isEmpty()) {
            String maxTipoEsistente = esistenti.stream()
                    .map(Iter::getId_tipo_iter)
                    .max(Comparator.comparingInt(SEQUENZA_ITER::indexOf))
                    .orElse("0");
            int indexNuovo = SEQUENZA_ITER.indexOf(iter.getId_tipo_iter());
            int indexMax = SEQUENZA_ITER.indexOf(maxTipoEsistente);
            if (indexNuovo != indexMax + 1) {
                throw new AssoServiceException(
                        "Sequenza non valida. Il prossimo iter deve essere: " +
                                getTipoIterSuccessivo(maxTipoEsistente));
            }
        } else {
            // Prima iter della pratica deve essere Preaffido (1)
            if (!"1".equals(iter.getId_tipo_iter())) {
                throw new AssoServiceException("Il primo iter deve essere Preaffido.");
            }
        }

        if (iter.getContributo() != null && iter.getContributo().trim().isEmpty()) {
            iter.setContributo(null);
        }
        iter.setAccount(user.getUsername());
        normalizzaDate(iter);
        if (iter.getAdottante() != null) iter.setId_contatto_adottante(iter.getAdottante().getId_contatto());
        if (iter.getVolontaria() != null) iter.setId_contatto_vol(iter.getVolontaria().getId_contatto());
        if (iter.getContatto() != null) iter.setId_contatto(iter.getContatto().getId_contatto());

        // Genera chiave univoca per il questionario di preaffido
        if ("1".equals(iter.getId_tipo_iter())) {
            iter.setQuest_key(UUID.randomUUID().toString());
        }

        String idIter = iterDao.save(iter);

        // Inizializza le domande del questionario per il preaffido
        if ("1".equals(iter.getId_tipo_iter())) {
            questionarioDao.initQuestionario(idIter);
        }

        // Avanza stato animale
        processoDao.setStatoAnimale(iter.getId_animale(), user.getUtente(), iter.getId_tipo_iter());

        return idIter;
    }

    @Transactional
    public void aggiornaIter(Iter iter, UserAuth user) throws AssoServiceException {
        if (iter.getContributo() != null && iter.getContributo().trim().isEmpty()) {
            iter.setContributo(null);
        }
        iter.setAccount(user.getUsername());
        normalizzaDate(iter);
        if (iter.getAdottante() != null) iter.setId_contatto_adottante(iter.getAdottante().getId_contatto());
        if (iter.getVolontaria() != null) iter.setId_contatto_vol(iter.getVolontaria().getId_contatto());
        if (iter.getContatto() != null) iter.setId_contatto(iter.getContatto().getId_contatto());
        iterDao.update(iter);
    }

    @Transactional
    public void saveDocumentoIter(String idIter, String idAnimale, Documento doc, MultipartFile file, UserAuth user)
            throws IOException, SQLIntegrityConstraintViolationException {
        doc.setAccount(user.getUsername());
        if (doc.getNum_documento() == null) doc.setNum_documento("");
        doc = documentoDao.saveOrUpdate(doc);
        documentoDao.saveDocumentoIter(doc.getId_documento(), idIter);
        if (file != null && !file.isEmpty()) {
            fileService.uploadFile(file, doc.getId_documento(), FileService.TipoSoggetto.ANIMALE, idAnimale);
        }
    }

    @Transactional
    public void eliminaIter(String idIter, String idAnimale, UserAuth user) {
        String idTipoIter = iterDao.getTipoIterByID(idIter);
        iterDao.delete(idIter);
        if (idTipoIter != null) {
            processoDao.setStatoAnimale(idTipoIter, null, user.getUtente(), idAnimale);
        }
    }

    // ------------------------------------------------------------------------
    // UTILITY
    // ------------------------------------------------------------------------

    private String getTipoIterSuccessivo(String idTipoCorrente) {
        return switch (idTipoCorrente) {
            case "1" -> "Adozione";
            case "2" -> "Consegna";
            case "3" -> "Passaggio di proprietà";
            default  -> "—";
        };
    }

    private void popolaContatti(Iter iter) {
        if (iter.getId_contatto_adottante() != null)
            iter.setAdottante(contattoDao.getByID(iter.getId_contatto_adottante(), true));
        if (iter.getId_contatto_vol() != null)
            iter.setVolontaria(contattoDao.getByID(iter.getId_contatto_vol(), true));
        if (iter.getId_contatto() != null)
            iter.setContatto(contattoDao.getByID(iter.getId_contatto(), true));
    }

    private void normalizzaDate(Iter iter) throws AssoServiceException {
        iter.setDt_colloquio(normalizzaData(iter.getDt_colloquio()));
        iter.setDt_consegna(normalizzaData(iter.getDt_consegna()));
    }

    private String normalizzaData(String data) throws AssoServiceException {
        if (data == null || data.isEmpty()) return data;
        try {
            if (data.contains("T")) {
                LocalDate date = Instant.parse(data)
                        .atZone(ZoneId.of("Europe/Rome"))
                        .toLocalDate();
                return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return data;
        } catch (Exception e) {
            throw new AssoServiceException("Formato data non valido: " + data);
        }
    }
}