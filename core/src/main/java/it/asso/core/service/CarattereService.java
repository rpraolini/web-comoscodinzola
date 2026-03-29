package it.asso.core.service;

import it.asso.core.dao.animali.animale.CaratteriDAO;
import it.asso.core.model.animali.animale.Caratteri;
import it.asso.core.model.animali.animale.TipoCarattere;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class CarattereService {

    private final CaratteriDAO caratteriDao;

    public CarattereService(CaratteriDAO caratteriDao) {
        this.caratteriDao = caratteriDao;
    }

    public List<Caratteri> getCaratteriByAnimale(String idAnimale) {
        List<Caratteri> list = caratteriDao.getCaratteriByIdAnimale(idAnimale);
        return list != null ? list : Collections.emptyList();
    }

    public List<TipoCarattere> getTipiCarattere() {
        List<TipoCarattere> tipi = caratteriDao.getTipoCarattere();
        if (tipi != null) {
            tipi.forEach(t -> t.setCaratteri(
                    caratteriDao.getCaratteriByTipo(t.getId_tipo_carattere())
            ));
        }
        return tipi != null ? tipi : Collections.emptyList();
    }

    @Transactional
    public String aggiungi(String idAnimale, Caratteri carattere) {
        carattere.setId_animale(idAnimale);
        carattere.setId_caratteri(null); // forza insert
        return caratteriDao.save(carattere);
    }

    @Transactional
    public void aggiornaNota(String idCaratteri, String idCarattere, String note) {
        Caratteri c = new Caratteri();
        c.setId_caratteri(idCaratteri);
        c.setId_carattere(idCarattere);
        c.setNote(note);
        caratteriDao.update(c);
    }

    @Transactional
    public void elimina(String idCaratteri) {
        caratteriDao.deleteByID(idCaratteri);
    }
}
