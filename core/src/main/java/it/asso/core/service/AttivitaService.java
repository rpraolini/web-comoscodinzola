package it.asso.core.service;

import it.asso.core.dao.animali.attivita.AttivitaDAO;
import it.asso.core.model.animali.attivita.Attivita;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttivitaService {
    private final AttivitaDAO attivitaDao;

    public AttivitaService(AttivitaDAO attivitaDao) {
        this.attivitaDao = attivitaDao;
    }


    @Transactional()
    public String save(Attivita attivita) {
        return attivitaDao.save(attivita);
    }
}
