package it.asso.core.service;

import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.model.contatto.ContattoAutocompleteDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ContattoService {

    private final ContattoDAO contattoDao;

    public ContattoService(ContattoDAO contattoDao) {
        this.contattoDao = contattoDao;
    }

    public List<ContattoAutocompleteDTO> cercaPerAutocomplete(String query, String idTipoEvento) {
        if (query == null || query.trim().length() < 2) return Collections.emptyList();
        List<ContattoAutocompleteDTO> risultati = contattoDao.getBySearchLight(query.trim(), idTipoEvento != null ? idTipoEvento : "");
        return risultati != null ? risultati : Collections.emptyList();
    }
}
