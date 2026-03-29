package it.asso.core.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.asso.core.common.Def;
import it.asso.core.common.MailController;
import it.asso.core.common.Utils;
import it.asso.core.common.exception.AssoServiceException;
import it.asso.core.dao.contabilita.ScadenziarioDAO;
import it.asso.core.dao.vaccinazioni.VaccinazioniScheduledDAO;
import it.asso.core.model.contabilita.Scadenziario;
import it.asso.core.model.vaccinazioni.Vaccinazioni;
import it.asso.core.multitenancy.TenantContext;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Configuration
@EnableScheduling
@Service
public class InvioMail {

	@Autowired
    ScadenziarioDAO scadenziarioDao;
	@Autowired
    MailController mailController;
	@Autowired
    VaccinazioniScheduledDAO vaccinazioniScheduledDao;
	
	private static Logger logger = LoggerFactory.getLogger(InvioMail.class);
	
	/*---------------------------------------------------------------------------------------------------------------------------------------*/
	/// schedulata per girare alle 01:01 ogni giorno
	@Scheduled(cron="0 1 1 * * *", zone="Europe/Rome") // fixedDelay = 3600000 in millisecondi cron="0 1 1 * * *", zone="Europe/Rome"
	public void promemoriaRateInScadenzaScheduleTask() {
		List<Scadenziario> result = new ArrayList<Scadenziario>();
		
		for (Scadenziario s : scadenziarioDao.getScadenziario()) {
			if(Def.NUM_UNO.equals(s.getProssima())) {
				LocalDate dtRata = Utils.convertStringToLocalDate(s.getDt_rata()).minusDays(5);
				LocalDate dtNow = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(2);
				long days = Period.between(dtNow, dtRata).getDays();
				if(days <= 0) {
					result.add(s);
				}
			}
		}
		System.out.println("nulla da inviare A ");
		if(!result.isEmpty()) {
			//mailController.sendPromemoriaRateInScadenza(result);
		    logger.info("Task testScheduleTask eseguito alle : " + Utils.getActualDateFormatted());
		}
		logger.info("Task eseguito alle : " + Utils.getActualDateFormatted() + " Mail non inviata");
	}
	
	/*---------------------------------------------------------------------------------------------------------------------------------------*/
	/// schedulata per girare tutti i giorni alle 07:01 del mattino
	@Scheduled(cron="0 1 7 * * *", zone="Europe/Rome") // fixedDelay = 3600000 in millisecondi cron="0 1 7 * * *", zone="Europe/Rome"
	public void promemoriaVacciniTask() throws AssoServiceException {
		logger.info("Inizio controllo vaccinazioni : " + Utils.getActualDateFormatted());
		//System.out.println("Inizio controllo vaccinazioni : " + Utils.getActualDateFormatted());
		List<Vaccinazioni> result_7 = new ArrayList<Vaccinazioni>();
		List<Vaccinazioni> result_15 = new ArrayList<Vaccinazioni>();
		/* Creare la classe sqlquery per iniettare correttamente il datasource per il task schedulato */
		for (Vaccinazioni s : vaccinazioniScheduledDao.getVaccinazioni()) {
			if(Def.NUM_UNO.equals(s.getDa_inviare_7())) {
				result_7.add(s);
			}else if(Def.NUM_UNO.equals(s.getDa_inviare_15())) {
				result_15.add(s);
			}
		}
		
		if(!result_7.isEmpty() || !result_15.isEmpty()) {
            String tenantId = TenantContext.getCurrentTenant();

            if (tenantId == null || tenantId.isEmpty() || "default".equals(tenantId)) {
                // Fallback: Tentiamo di risolverlo dal DAO o lanciamo un errore
                throw new AssoServiceException("Impossibile inviare la mail: Tenant ID non risolto dal contesto.");
            }
			mailController.sendPromemoriaScadenzaVaccinazioni(result_7, result_15, tenantId);
		    logger.info("Task ScheduleTask Vaccinazioni eseguito alle : " + Utils.getActualDateFormatted());
		}
		logger.info("Task eseguito alle : " + Utils.getActualDateFormatted() + " Mail di promemoria vaccinazioni non inviata");
		//System.out.println("Task eseguito alle : " + Utils.getActualDateFormatted() + " Mail di promemoria vaccinazioni non inviata");
	}
	
	/*---------------------------------------------------------------------------------------------------------------------------------------*/
	/// schedulata per girare alle 01:01 ogni due lunedi
	@Scheduled(cron="0 1 1 * * MON", zone="Europe/Rome") // fixedDelay = 3600000  in millisecondi
	public void promemoriaRateScaduteScheduleTask() {
		List<Scadenziario> result = new ArrayList<Scadenziario>();
		
//		for (Scadenziario s : scadenziarioDao.getScadenziario()) {
//			if(!Def.NUM_UNO.equals(s.getProssima())) {
//				result.add(s);
//			}
//		}
		
		if(!result.isEmpty()) {
			//mailController.sendPromemoriaRateScadute(result);
		    logger.info("Task testScheduleTask eseguito alle : " + Utils.getActualDateFormatted());
		}
		logger.info("Task eseguito alle : " + Utils.getActualDateFormatted() + " Mail non inviata");
	}
	
	
	/*---------------------------------------------------------------------------------------------------------------------------------------*/
	
	 

}
