package it.asso.core.controller.mobile;

import java.util.ArrayList;
import java.util.List;


import it.asso.core.common.Def;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.model.notifiche.EventiNotifica;
import it.asso.core.dao.animali.animale.AnimaleDAO;
import it.asso.core.dao.animali.animale.CaratteriDAO;
import it.asso.core.dao.documenti.FotoDAO;
import it.asso.core.dao.localizzazione.LocalizzazioneDAO;
import it.asso.core.dao.notifiche.NotificheDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.animale.Caratteri;
import it.asso.core.model.documenti.Foto;
import it.asso.core.model.localizzazione.Provincia;
import it.asso.core.model.localizzazione.Regione;
import it.asso.core.model.notifiche.Notifica;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@SuppressWarnings("unused")
public class MobileController {

	@Autowired
    AnimaleDAO animaleDao;
	@Autowired
    FotoDAO fotoDao;
	@Autowired
    LocalizzazioneDAO localizzazioneDao;
	@Autowired
    CaratteriDAO caratteriDao;
	@Autowired
    NotificheDAO notificheDao;
	

	@RequestMapping(value = "/jsp/public/mobile/getRandom.json", method = RequestMethod.GET)
	public @ResponseBody List<Animale> getRandom(HttpServletRequest request){
		List<Animale> animali = animaleDao.getRandom();
		animali = setFotoAnimali(request, animali);
		return animali;
	}
	
	@RequestMapping(value = "/jsp/public/mobile/getRegioniByNazione.json", method = RequestMethod.GET)
	public @ResponseBody List<Regione> getRegioniByNazione(HttpServletRequest request, String nazione) throws AssoServiceException {
		List<Regione> regioni = localizzazioneDao.getRegioneByNazioneUsed(nazione);
		return regioni;
	}
	
	@RequestMapping(value = "/jsp/public/mobile/getProvinceByRegione.json", method = RequestMethod.GET)
	public @ResponseBody List<Provincia> getProvince(HttpServletRequest request, String regione) throws AssoServiceException {
		List<Provincia> provincia = localizzazioneDao.getProvincieByRegioneUsed(regione);
		return provincia;
	}
	
	@RequestMapping(value = "/jsp/public/mobile/ricerca.json", method = RequestMethod.GET)
	public @ResponseBody List<Animale> ricerca(HttpServletRequest request, String tipo, String eta, String taglia, String sesso, String regione, String provincia){
		List<Animale> animali = animaleDao.getRicercaPubblica(tipo, eta, taglia, sesso, regione, provincia);
		animali = setFotoAnimali(request, animali);
		return animali;
	}
	
	@RequestMapping(value = "/jsp/public/mobile/getFotoById.json", method = RequestMethod.GET)
	public @ResponseBody List<Foto> getFotoById(HttpServletRequest request, String id) throws AssoServiceException {
		String tenant = getTenant(request);
		String url_image = getUrlImage(request);
		List<Foto> fotoPubbliche = new ArrayList<Foto>(); 
		List<Foto> foto = fotoDao.getFotoByIdAnimale(id);
    	for (Foto f : foto) {
    		if(Def.NUM_UNO.equals(f.getPubblica())) {
    			f.setUrl(url_image + "/" + tenant + "/" + f.getId_animale() + "/" + f.getNome_file());
    			f.setUrl_t(url_image + "/" + tenant + "/"+ f.getId_animale() + "/" + f.getNome_file_t());
    			fotoPubbliche.add(f);
    		}
		}
		return fotoPubbliche;
	}
	
	@RequestMapping(value = "/jsp/public/mobile/getCaratteriById.json", method = RequestMethod.GET)
	public @ResponseBody List<Caratteri> getCaratteriById(HttpServletRequest request, String id) throws AssoServiceException {
		List<Caratteri> c =  caratteriDao.getCaratteriByIdAnimale(id);
		return c;
	}
	
	
	@RequestMapping(value = "/jsp/public/mobile/getNotifiche.json", method = RequestMethod.GET)
	public @ResponseBody List<Notifica> getNotifiche(HttpServletRequest request, String offset, String limit) throws AssoServiceException{
		List<Notifica> notifiche = new ArrayList<Notifica>();
		notifiche = notificheDao.getRange(offset, limit);
		return notifiche;
	}
	
	@RequestMapping(value = "/jsp/public/mobile/getEventiNotifiche.json", method = RequestMethod.GET)
	public @ResponseBody List<EventiNotifica> getEventiNotifiche(HttpServletRequest request) throws AssoServiceException{
		List<EventiNotifica> notifiche = new ArrayList<EventiNotifica>();
		notifiche = notificheDao.getEventi();
		return notifiche;
	}
	/*----------------------------------------------------------------------------------------------------------------------------*/
	private List<Animale> setFotoAnimali(HttpServletRequest request, List<Animale> animali){
		String url_image = getUrlImage(request);
		String tenant = getTenant(request);
		for (Animale animale : animali) {
    		animale.setFoto(getFotoProfilo(animale, tenant, url_image));
    		animale.setLocation(animaleDao.getLocation(animale.getId_animale()));
		}
		return animali;
	}
	
	private String getTenant(HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		String tenant = url.substring(url.indexOf("//") + 2, url.indexOf("."));
		return tenant;
	}
	private String getUrlImage(HttpServletRequest request) {
		StringBuffer url = request.getRequestURL();
		String uri = request.getRequestURI();
		String url_image = url.substring(0, url.indexOf(uri)) + "/images";
		return url_image;
	}
	
	private Foto getFotoProfilo(Animale animale, String tenant, String url_image) {
		Foto ft = new Foto();
		Foto f = fotoDao.getFotoProfiloById(animale.getId_animale());
		
		if(f == null) {
			ft.setUrl(url_image + "/empty.png");
		}else {
			ft.setId_animale(f.getId_animale());		
			ft.setUrl(url_image + "/" + tenant + "/" + f.getId_animale() + "/" + f.getNome_file());
			ft.setUrl_t(url_image + "/" + tenant + "/" + f.getId_animale() + "/" + f.getNome_file_t());
		}
		return ft;
	}
}
