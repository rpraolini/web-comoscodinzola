package it.asso.core.service;

import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.attivita.AttivitaDAO;
import it.asso.core.dto.proprietario.ProprietarioDTO;
import it.asso.core.model.animali.attivita.Attivita;
import it.asso.core.model.contatto.Contatto;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CronologiaService {

    private final AttivitaDAO attivitaDao;
    private final AnimaleDAO animaleDao;

    public CronologiaService(AttivitaDAO attivitaDao, AnimaleDAO animaleDao) {
        this.attivitaDao = attivitaDao;
        this.animaleDao = animaleDao;
    }

    public List<Attivita> getAttivita(String idAnimale) {
        List<Attivita> lista = attivitaDao.getAttivitaByAnimale(idAnimale);
        return lista != null ? lista : Collections.emptyList();
    }

    public ProprietarioDTO getProprietario(String idAnimale) {
        Contatto contatto = animaleDao.getProprietarioByIDAnimale(idAnimale);
        if (contatto == null) return null;
        ProprietarioDTO dto = ProprietarioDTO.from(contatto);
        dto.setDt_proprietario(animaleDao.getDtProprietario(idAnimale, contatto.getId_contatto()));
        return dto;
    }
}
