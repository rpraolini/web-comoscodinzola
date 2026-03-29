package it.asso.core.controller.configurazione;

import java.util.List;


import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.controller.BaseController;
import it.asso.core.dao.configurazione.ConfigurazioneDAO;
import it.asso.core.model.configurazione.Configurazione;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
public class ConfigurazioneController extends BaseController {

	@Autowired
    ConfigurazioneDAO configurazioneDAO;


	@RequestMapping(value = "/jsp/private/configurazione/getAll.json", method = RequestMethod.GET)
	public @ResponseBody List<Configurazione> getAll(HttpServletRequest request) throws AssoServiceException {
		return configurazioneDAO.getAllConfigurazioni("Pubblica");
	}

}


