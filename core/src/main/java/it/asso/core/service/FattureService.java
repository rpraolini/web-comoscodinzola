package it.asso.core.service;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.documenti.DocumentoDAO;
import it.asso.core.dao.organizzazione.contabilita.FattureDAO;
import it.asso.core.model.documenti.Documento;
import it.asso.core.model.organizzazione.contabilita.DettaglioFattura;
import it.asso.core.model.organizzazione.contabilita.Fattura;
import it.asso.core.security.UserAuth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FattureService {

    private final FattureDAO fattureDao;
    private final DocumentoDAO documentoDao;
    // NOTE: Altri DAO (RendicontoDAO) sarebbero iniettati qui se la logica fosse migrata.

    // COSTRUTTORE CON INIEZIONE
    public FattureService(FattureDAO fattureDao, DocumentoDAO documentoDao) {
        this.fattureDao = fattureDao;
        this.documentoDao = documentoDao;
    }

    // UTILITY PER L'ESTRAZIONE DEL TENANT (Dovrebbe essere qui, ma nel controller usiamo request.getSession())
    // In un progetto pulito, qui ci sarebbe il TenantContext.getCurrentTenant()

    // ------------------------------------------------------------------------
    // LOGICA DI LETTURA E POPOLAMENTO (getFatturaById)
    // ------------------------------------------------------------------------

    /**
     * Popola i documenti e i dettagli (dettaglio fattura) per l'oggetto Fattura.
     */
    @Transactional(readOnly = true)
    public Fattura popolaDettagliFattura(Fattura f) throws AssoServiceException {
        if (f == null || f.getId_fattura() == null) {
            return f;
        }

        // 1. Popola i documenti collegati
        if (f.getDocumenti() != null) {
            ArrayList<Documento> documenti = documentoDao.getDocumentiByIDFattura(f.getId_fattura());
            f.getDocumenti().addAll(documenti);
        }

        // 2. Popola i dettagli (voci della fattura)
        f.setDettaglio(fattureDao.getDettagliFattura(f.getId_fattura()));
        return f;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI SCRITTURA E RITENUTA D'ACCONTO (saveOrUpdateFattura)
    // ------------------------------------------------------------------------

    /**
     * Salva o aggiorna la fattura e, se contiene una ritenuta, ne crea la fattura collegata.
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateFattura(Fattura f, UserAuth u, String idOrganizzazione) throws AssoServiceException {
        // Logica di business migrata dal Controller

        f.setId_organizzazione(idOrganizzazione);
        f.setAccount(u.getUsername());

        // 1. Conversione delle date da ZonedDateTime a formato SQL/Java
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (f.getDt_emissione() != null && f.getDt_emissione().contains("Z")) {
            ZonedDateTime zp = ZonedDateTime.parse(f.getDt_emissione());
            f.setDt_emissione(zp.format(dtf));
        }
        if (f.getDt_scadenza() != null && f.getDt_scadenza().contains("Z")) {
            ZonedDateTime zp = ZonedDateTime.parse(f.getDt_scadenza());
            f.setDt_scadenza(zp.format(dtf));
        }

        // 2. Salvataggio della Fattura principale
        String idFatturaPrincipale = fattureDao.saveOrUpdate(f);

        // 3. Salvataggio Dettagli e Check Ritenuta
        boolean ritenutaPresente = false;
        fattureDao.deleteDettaglioFatturaByIDFattura(idFatturaPrincipale);

        for (DettaglioFattura d : f.getDettaglio()) {
            d.setId_fattura(idFatturaPrincipale);
            d.setId_fd(null);
            fattureDao.saveOrUpdate(d);
            if (Def.RITENUTA_ACCONTO.equalsIgnoreCase(d.getId_vf())) {
                ritenutaPresente = true;
            }
        }

        // 4. Gestione della Ritenuta d'Acconto (Fattura Collegata)
        if (ritenutaPresente) {
            // Clona la fattura originale per creare la ritenuta
            Fattura ritenutaFattura = f;
            ritenutaFattura.setAccount(u.getUsername());
            ritenutaFattura.setId_fattura(null); // Nuovo ID
            ritenutaFattura.setRitAccFattura(idFatturaPrincipale); // Collega la principale

            // Calcolo e data di scadenza (16 del mese successivo)
            LocalDate dataEmissione = LocalDate.parse(f.getDt_emissione(), dtf);
            dataEmissione = dataEmissione.plusMonths(1);
            LocalDate dtScadenza = LocalDate.of(dataEmissione.getYear(), dataEmissione.getMonthValue(), 16);

            ritenutaFattura.setDt_scadenza(dtScadenza.format(dtf));

            // Salvataggio della Fattura Ritenuta
            String idRitenuta = fattureDao.saveOrUpdate(ritenutaFattura);

            // Aggiornamento Dettagli con importi negativi
            for (DettaglioFattura d : f.getDettaglio()) {
                if (Def.RITENUTA_ACCONTO.equalsIgnoreCase(d.getId_vf())) {
                    Double importo = Double.valueOf(d.getImporto());
                    Double iva = Double.valueOf(d.getIva());
                    Double imponibile = Double.valueOf(d.getImponibile());

                    d.setId_fattura(idRitenuta);
                    d.setId_fd(null);
                    d.setImporto(String.valueOf(importo * -1));
                    d.setIva(String.valueOf(iva * -1));
                    d.setImponibile(String.valueOf(imponibile * -1));

                    fattureDao.saveOrUpdate(d);

                    // Aggiorna l'oggetto RitenutaFattura con i totali negativi
                    ritenutaFattura.setIva(d.getIva());
                    ritenutaFattura.setImponibile(d.getImponibile());
                    ritenutaFattura.setImporto(d.getImporto());
                    fattureDao.saveOrUpdate(ritenutaFattura);
                }
            }
        }

        return idFatturaPrincipale;
    }

    // ------------------------------------------------------------------------
    // LOGICA DI ELIMINAZIONE COMPLESSA (eliminaFattura)
    // ------------------------------------------------------------------------

    /**
     * Elimina una fattura principale, i suoi dettagli, i documenti collegati e l'eventuale fattura di ritenuta collegata.
     */
    @Transactional(rollbackFor = Exception.class)
    public String deleteFatturaCompletely(Fattura f) throws AssoServiceException, SQLIntegrityConstraintViolationException, IllegalStateException, IOException {

        // 1. Elimina Documenti e Dettagli della Fattura principale
        this.deleteFatturaComponents(f.getId_fattura());
        String idEliminato = fattureDao.deleteByID(f.getId_fattura());

        // 2. Gestione Fattura Collegata (Ritenuta)
        String fatturaCollegataId = f.getRitAccFattura(); // Id di una ritenuta collegata (se f è la principale)
        Fattura ritenutaCollegata = null;

        if (fatturaCollegataId != null) {
            // Se la fattura principale aveva una ritenuta (RitAccFattura != null)
            ritenutaCollegata = fattureDao.getById(fatturaCollegataId);
        } else {
            // Verifica se la fattura eliminata è una ritenuta collegata ad un'altra fattura
            ritenutaCollegata = fattureDao.getFatturaCollegataById(f.getId_fattura());
        }

        if (ritenutaCollegata != null) {
            // 3. Elimina la fattura di ritenuta (se trovata)
            this.deleteFatturaComponents(ritenutaCollegata.getId_fattura());
            fattureDao.deleteByID(ritenutaCollegata.getId_fattura());
        }

        return idEliminato;
    }

    // UTILITY: Elimina dettagli e documenti di una singola fattura
    private void deleteFatturaComponents(String idFattura) throws AssoServiceException, SQLIntegrityConstraintViolationException, IOException {
        // Elimina Documenti
        List<Documento> documenti = documentoDao.getDocumentiByIDFattura(idFattura);
        for (Documento doc : documenti) {
            documentoDao.deleteDocumentoFattura(doc.getId_documento(), idFattura);
            documentoDao.deleteByID(doc.getId_documento());
        }

        // Elimina Dettagli
        List<DettaglioFattura> dettagli = fattureDao.getDettagliFattura(idFattura);
        for (DettaglioFattura d : dettagli) {
            fattureDao.deleteDettaglioFatturaByID(d.getId_fd());
        }
    }
}