package it.asso.core.controller;

import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.common.exception.AutenticazioneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class ExceptionController {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class); // Inizializzazione Logger moderna

    final String ASSO_EXCEPTION = " (AssoServiceException)";
    final String AUTENTICAZIONE_EXCEPTION = " (AutenticazioneException)";
    final String DATA_INTEGRITY_EXCEPTION = " (DataIntegrityViolationException)";


    /**
     * Gestisce le eccezioni custom AssoServiceException (Errore di Business Logic)
     */
    @ExceptionHandler(AssoServiceException.class)
    // Ritorna ResponseEntity per uno stato HTTP gestito (es. 400 Bad Request)
    public @ResponseBody ResponseEntity<String> gestoreEccezioni(AssoServiceException ex) {
        logger.error("ASSO SERVICE EXCEPTION: {}", ex.getMessage());

        String errorMessage = Def.STR_ERROR + ex.getMessage() + ASSO_EXCEPTION;

        // Generalmente un errore di logica di business o validazione -> HTTP 400
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("\"" + errorMessage + "\"");
    }

    /**
     * Gestisce le eccezioni di Autenticazione custom.
     */
    @ExceptionHandler(AutenticazioneException.class)
    public @ResponseBody ResponseEntity<String> gestoreEccezioni(AutenticazioneException ex) {
        logger.error("AUTENTICAZIONE EXCEPTION: {}", ex.getMessage());

        String errorMessage = Def.STR_ERROR + ex.getMessage() + AUTENTICAZIONE_EXCEPTION;

        // Fallimento di autenticazione -> HTTP 401 Unauthorized
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("\"" + errorMessage + "\"");
    }

    /**
     * Gestisce le eccezioni di violazione di integrità del database (es. Chiave Esterna, Duplicati).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public @ResponseBody ResponseEntity<String> gestoreEccezioni(DataIntegrityViolationException ex) {
        logger.error("DATA INTEGRITY VIOLATION EXCEPTION: {}", ex.getMessage());

        String errorMessage = Def.STR_ERROR + "Violazione di integrità dei dati. Operazione non permessa." + DATA_INTEGRITY_EXCEPTION;

        // Violazione DB -> HTTP 409 Conflict
        return ResponseEntity.status(HttpStatus.CONFLICT).body("\"" + errorMessage + "\"");
    }

    /**
     * Cattura tutte le eccezioni generiche non gestite (Fallback).
     */
    @ExceptionHandler(Exception.class)
    public @ResponseBody ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("ECCEZIONE NON CATTURATA: ", ex);

        String errorMessage = Def.STR_ERROR + "Errore interno del server.";

        // Errore generico/non gestito -> HTTP 500 Internal Server Error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("\"" + errorMessage + "\"");
    }
}