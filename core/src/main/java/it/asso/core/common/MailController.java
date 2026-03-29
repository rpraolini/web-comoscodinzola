package it.asso.core.common;

import it.asso.core.dao.configurazione.ConfigurazioneDAO;
import it.asso.core.dao.contatto.ContattoDAO;
import it.asso.core.dao.log.LogMailDAO;
import it.asso.core.dao.organizzazione.OrganizzazioneDAO;
import it.asso.core.dao.raccolta.EventoDAO;
import it.asso.core.model.animali.animale.Animale;
import it.asso.core.model.animali.gestione.Iter;
import it.asso.core.model.configurazione.Configurazione;
import it.asso.core.model.contabilita.Scadenziario;
import it.asso.core.model.contatto.Contatto;
import it.asso.core.model.organizzazione.Organizzazione;
import it.asso.core.model.raccolta.Evento;
import it.asso.core.model.vaccinazioni.Vaccinazioni;
import it.asso.core.security.UserAuth;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component("mailService")
public class MailController {

    private static Logger logger = LoggerFactory.getLogger(MailController.class);

    private final ContattoDAO contattoDao;
    private final LogMailDAO logMailDao;
    private final EventoDAO eventoDao;
    private final ConfigurazioneDAO configurazioneDAO;
    private final JavaMailSender mailSender;
    private final OrganizzazioneDAO organizzazioneDAO;

    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    private Organizzazione organizzazione;

    // VARIABILI STATICH E DINAMICHE
    private String mail_from;
    private String mail_cc;
    private String mail_replyto;
    private String url_tenant;
    private String cibo_cane;
    private String str_footer;
    private String url_logo;


    // COSTRUTTORE CON INIEZIONE
    public MailController(ContattoDAO contattoDao, LogMailDAO logMailDao, EventoDAO eventoDao, ConfigurazioneDAO configurazioneDAO, JavaMailSender mailSender, OrganizzazioneDAO organizzazioneDAO) {
        this.contattoDao = contattoDao;
        this.logMailDao = logMailDao;
        this.eventoDao = eventoDao;
        this.configurazioneDAO = configurazioneDAO;
        this.mailSender = mailSender;
        this.organizzazioneDAO = organizzazioneDAO;
    }


    /**
     * Carica e memorizza nella cache le configurazioni statiche.
     */
    private void configure(String tenantId) {
        if (configCache.isEmpty()) {
            List<Configurazione> c = configurazioneDAO.getAllConfigurazioni("%");
            for (Configurazione configurazione : c) {
                configCache.put(configurazione.getChiave(), configurazione.getDescrizione());
            }
        }

        if (organizzazione == null) {
            try {
                this.organizzazione = organizzazioneDAO.getByTenant(tenantId);
            } catch (Exception e) {
                logger.error("Impossibile caricare l'organizzazione per il tenant " + tenantId, e);
            }
        }

        mail_from = getConfig("mail_from");
        mail_cc = getConfig("mail_cc");
        mail_replyto = getConfig("mail_replyto");
        url_tenant = getConfig("url_tenant");
        cibo_cane = getConfig("cibo_cane");

        // Costruisci URL e Footer
        url_logo = "https://" + url_tenant + "/images/" + organizzazione.getTenant()+ "/logo.png";
        str_footer = "<hr><div align='center'>" + organizzazione.getRag_sociale() + " " + organizzazione.getSigla_tipo_organizzazione() + "<br>" + organizzazione.getIscrizione() + "<br>" +
                "C.F. " + organizzazione.getCf() + " iban " + (organizzazione.getCc() != null ? organizzazione.getCc().getIban() : "xxxxxxxxxxxx") + " intestato a " + organizzazione.getRag_sociale() + " " + organizzazione.getSigla_tipo_organizzazione() + "</div>";
    }

    // Metodi di utilità per accedere alla configurazione
    private String getConfig(String key) { return configCache.getOrDefault(key, "N/A"); }
    private String getMailFrom() { return mail_from; }
    private String getMailCc() { return mail_cc; }
    private String getMailReplyTo() { return mail_replyto; }
    private String getUrlTenant() { return url_tenant; }
    private String getStrFooter() { return str_footer; }
    private String getUrlLogo() { return url_logo; }
    private String getCiboCane() { return cibo_cane; }


    public void sendMail(String to, String subject, String body, String tenantId) {
        configure(tenantId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getMailFrom());
        message.setCc(getMailCc());
        message.setTo(to);
        message.setReplyTo(getMailReplyTo());
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        logMailDao.save(getMailCc(), Def.STR_GENERICA);
    }

    // avviso agli admin dell'avvenuta compilazione del questionario
    public void sendConfermaQuestionarioCompilato(Iter richiesta, Animale animale, String tenantId) {
        configure(tenantId);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getMailFrom());
        message.setCc(getMailCc());
        message.setTo(getConfig("mail_to"));
        message.setReplyTo(getMailReplyTo());
        message.setSubject("ADMIN : Compilato questionario per " + animale.getNome());
        message.setText(richiesta.getNome() + " ha sottomesso il questionario di preaffido per " + animale.getNome() +".");
        mailSender.send(message);
        logMailDao.save(getConfig("mail_to"), Def.STR_QUESTIONARIO);
    }



    // invio della mail al preaffidante
    public String sendMailToPreaffidanteHtml(Iter richiesta, Animale animale, HttpServletRequest request) {
        String result = Def.STR_OK;
        String tenantId = request.getHeader("Host").split("\\.")[0];
        try {
            MimeMessagePreparator messaggio = getBodyMailToPreaffidanteHtml(richiesta, animale, request, tenantId);
            mailSender.send(messaggio);
            logMailDao.save(getConfig("mail_to"), Def.STR_PREAFFIDANTE);
        }catch(Exception e) {
            logger.error("Errore invio mail preaffido: " + e.getMessage(), e);
            result = Def.STR_KO;
        }
        return result;
    }

    // invio della mail al volontario per raccolta cibo
    public String sendMailToRaccoltaCiboHtml(Contatto contatto, Contatto puntoDiRaccolta, Evento evento, UserAuth user, HttpServletRequest request) {
        String result = Def.STR_OK;
        String tenantId = request.getHeader("Host").split("\\.")[0];
        try {
            MimeMessagePreparator messaggio = getBodyMailToRaccoltaCiboHtml(contatto, puntoDiRaccolta, evento, user, request, tenantId);
            mailSender.send(messaggio);
            logMailDao.save(contatto.getEmail(), Def.STR_VOLONTARIO_CIBO);
        }catch(Exception e) {
            logger.error("Errore invio mail raccolta cibo: " + e.getMessage(), e);
            result = Def.STR_KO;
        }
        return result;
    }

    // invio della mail di ringraziamento al volontario per raccolta cibo
    public String sendMailRingraziamentoForRaccoltaCiboHtml(Contatto contatto, Contatto puntoDiRaccolta, Evento evento, UserAuth user, HttpServletRequest request) {
        String result = Def.STR_OK;
        String tenantId = request.getHeader("Host").split("\\.")[0];
        try {
            MimeMessagePreparator messaggio = getBodyMailRingraziamentoForRaccoltaCiboHtml(contatto, puntoDiRaccolta, evento, user, request, tenantId);
            mailSender.send(messaggio);
            logMailDao.save(contatto.getEmail(), Def.STR_VOLONTARIO_CIBO_RINGRAZIAMENTO);
        }catch(Exception e) {
            logger.error("Errore invio mail ringraziamento: " + e.getMessage(), e);
            result = Def.STR_KO;
        }
        return result;
    }


    // invio della mail per scadenze rate passate
    public String sendPromemoriaRateScadute(List<Scadenziario> scadenze, String tenantId) {
        String result = Def.STR_OK;
        try {
            MimeMessagePreparator messaggio = getBodyPromemoriaRateScadute(scadenze, tenantId);
            mailSender.send(messaggio);
            logMailDao.save(getConfig("mail_to"), Def.STR_RATE_SCADUTE);
        }catch(Exception e) {
            logger.error("Errore invio Promemoria Rate Scadute: " + e.getMessage(), e);
            result = Def.STR_KO;
        }
        return result;
    }

    // invio della mail per scadenze prossime rate
    public String sendPromemoriaRateInScadenza(List<Scadenziario> scadenze, String tenantId) {
        String result = Def.STR_OK;
        try {
            MimeMessagePreparator messaggio = getBodyPromemoriaRateInScadenza(scadenze, tenantId);
            mailSender.send(messaggio);
            logMailDao.save(getConfig("mail_to"), Def.STR_RATE_IN_SCADENZA);
            logger.info("Invio mail rate in scadenza eseguito alle : " +  Utils.getActualDateFormatted());
        }catch(Exception e) {
            logger.error("Errore invio mail rate in scadenza : " + e.getMessage());
            result = Def.STR_KO;
        }
        return result;
    }

    // invio della mail per scadenze vaccinazioni
    public String sendPromemoriaScadenzaVaccinazioni(List<Vaccinazioni> result_7, List<Vaccinazioni> result_15, String tenantId) {
        String result = Def.STR_OK;
        try {
            for (Vaccinazioni s : result_7) {
                MimeMessagePreparator messaggio = getBodyPromemoriaVaccinoInScadenza(s, tenantId);
                mailSender.send(messaggio);
                logMailDao.save(getConfig("mail_to"), Def.STR_VACCINO_IN_SCADENZA_7);
                logger.info("Invio mail vaccino in scadenza fra 7 giorni eseguito alle : " +  Utils.getActualDateFormatted());
            }
            for (Vaccinazioni s : result_15) {
                MimeMessagePreparator messaggio = getBodyPromemoriaVaccinoInScadenza(s, tenantId);
                mailSender.send(messaggio);
                logMailDao.save(getConfig("mail_to"), Def.STR_VACCINO_IN_SCADENZA_15);
                logger.info("Invio mail vaccino in scadenza fra 15 giorni eseguito alle : " +  Utils.getActualDateFormatted());
            }
        }catch(Exception e) {
            logger.error("Errore invio Promemoria Scadenza Vaccinazioni : " + e.getMessage());
            result = Def.STR_KO;
        }
        return result;
    }


    // ------------------------------------------------------------------------
    // METODI DI PREPARAZIONE MESSAGGIO (MimeMessagePreparator)
    // ------------------------------------------------------------------------

    private MimeMessagePreparator getBodyPromemoriaRateScadute(List<Scadenziario> scadenze, String tenantId) {
        return new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                configure(tenantId);
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(getMailFrom());
                helper.setBcc(getMailCc());
                helper.setTo(getConfig("mail_to"));
                helper.setReplyTo(getMailReplyTo());

                helper.setSubject("Promemoria rate scadute  " + Utils.getActualDateFormatted());

                StringBuilder content = new StringBuilder();
                content.append("<html><body><p>Promemoria rate scadute :  <br>");

                content.append("<table>");

                for (Scadenziario s : scadenze) {
                    content.append("<tr><td>" + s.getContatto() + "</td><td>" + s.getDt_rata() + "</td><td>" + s.getImporto() + " &euro;</td></tr>");
                }
                content.append("</table>");
                content.append("<p>Administrator</p>");
                content.append("<p><div align='center'><img src='" + getUrlLogo() + "' style='width:100px;'></div></p>");
                content.append(getStrFooter());
                content.append("</body></html>");

                helper.setText(content.toString(), true);
            }
        };
    }

    // NOTA: IL VECCHIO getBodyPromemoriaRateInScadenza ORA CHIAMA getBodyPromemoriaRateScadute
    private MimeMessagePreparator getBodyPromemoriaRateInScadenza(List<Scadenziario> scadenze, String tenantId) {
        return getBodyPromemoriaRateScadute(scadenze, tenantId);
    }


    private MimeMessagePreparator getBodyPromemoriaVaccinoInScadenza(Vaccinazioni vaccinazione, String tenantId) {
        return new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                configure(tenantId);
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(getMailFrom());
                helper.setBcc(getMailCc());
                helper.setTo(getConfig("mail_to"));
                helper.setReplyTo(getMailReplyTo());

                helper.setSubject("Promemoria scadenza vaccinazione di " +  vaccinazione.getNome());

                StringBuilder content = new StringBuilder();

                if(Def.NUM_UNO.equals(vaccinazione.getDa_inviare_7())) {
                    content.append("<html><body><p>Promemoria vaccino in scadenza fra 7 giorni :  <br>");
                }else if (Def.NUM_UNO.equals(vaccinazione.getDa_inviare_15())) {
                    content.append("<html><body><p>Promemoria vaccino in scadenza fra 15 giorni :  <br>");
                }else {
                    content.append("<html><body><p>Promemoria vaccino in scadenza :  <br>");
                }

                content.append("<table>");
                content.append("<tr><td>NOME</td><td>DATA RICHIAMO</td><td>TIPOLOGIA</td></tr>");
                content.append("<tr><td>" + vaccinazione.getNome() + "</td><td>" + vaccinazione.getDt_richiamo() + "</td><td>" + vaccinazione.getDt_evento() + "</td></tr>");

                content.append("</table>");
                content.append("<p>Administrator</p>");
                content.append("<p><div align='center'><img src='" + getUrlLogo() + "' style='width:100px;'></div></p>");
                content.append(getStrFooter());
                content.append("</body></html>");

                helper.setText(content.toString(), true);

            }
        };
    }


    private MimeMessagePreparator getBodyMailToPreaffidanteHtml(Iter richiesta, Animale animale, jakarta.servlet.http.HttpServletRequest request, String tenantId) {

        return new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                configure(tenantId);
                String urlLogo = getUrlLogo();
                String sUrlServlet = "https://" + getUrlTenant() + request.getContextPath() + "/jsp/public/questionario.jsp?key=" + richiesta.getQuest_key();

                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(getMailFrom());
                helper.setBcc(getMailCc());
                helper.setTo(richiesta.getEmail());
                helper.setReplyTo(getMailReplyTo());

                helper.setSubject("Questionario di preaffido per : " + animale.getNome());

                StringBuilder content = new StringBuilder();
                content.append("<html><body><p>Spett.le " + richiesta.getNome() + ", <br>ti invio il link a cui puoi trovare il questionario online da compilare.</p>");
                content.append("<p>Cordiali saluti<br>Lo staff di " + organizzazione.getRag_sociale() + " " + organizzazione.getSigla_tipo_organizzazione() + "</p>");
                content.append("<p><a href='" + sUrlServlet + "' target=''>Questionario per " + animale.getNome() + "</a></p>");
                content.append("<p><div align='center'><img src='" + urlLogo + "' style='width:100px;'></div></p>");
                content.append(getStrFooter());
                content.append("</body></html>");

                helper.setText(content.toString(), true);
            }
        };
    }

    private MimeMessagePreparator getBodyMailToRaccoltaCiboHtml(Contatto contatto, Contatto puntoDiRaccolta, Evento evento, UserAuth user, jakarta.servlet.http.HttpServletRequest request, String tenantId) {
        Contatto contattoLogged = contattoDao.getByID(user.getUtente().getId_contatto(), true);
        return new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws Exception {
                configure(tenantId);
                String urlLogo = getUrlLogo();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(getMailFrom());
                helper.setBcc(getMailCc());
                helper.setTo(contatto.getEmail());
                helper.setReplyTo(getMailReplyTo());

                helper.setSubject("Richiesta disponibilita' raccolta cibo per il giorno " + evento.getDt_evento());

                StringBuilder content = new StringBuilder();
                content.append("<html><body><p>Ciao " + contatto.getNome() + ",<br>stiamo organizzando una raccolta cibo presso :<br>");
                content.append("<p><b>" + puntoDiRaccolta.getDescrizione() + "</b> situato in <b>" + puntoDiRaccolta.getIndirizzo() + "</b><br>per il giorno <b>" + evento.getDt_evento() + "</b>.</p>");
                content.append("<p>Se avessi voglia di regalarci un'ora del tuo tempo, daresti una mano ai nostri pelosi che avranno sempre la pancia piena :)");
                content.append("<br>Rispondi a questa mail con gli orari in cui saresti disponibile o chiamaci.<br>");
                content.append("<p>Grazie di cuore!<br>");
                content.append(getInfoContatto( contattoLogged));
                content.append("Responsabile raccolte cibo<br>" + organizzazione.getRag_sociale() + " " + organizzazione.getSigla_tipo_organizzazione() + "</p>");
                content.append("<p><div align='center'><img src='" + urlLogo + "' style='width:100px;'></div></p>");
                content.append(getStrFooter());
                content.append("</body></html>");

                helper.setText(content.toString(), true);

            }
        };
    }

    private MimeMessagePreparator getBodyMailRingraziamentoForRaccoltaCiboHtml(Contatto contatto, Contatto puntoDiRaccolta, Evento evento, UserAuth user, jakarta.servlet.http.HttpServletRequest request, String tenantId) {
        Contatto contattoLogged = contattoDao.getByID(user.getUtente().getId_contatto(), true);
        return new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws Exception {
                configure(tenantId);
                String urlLogo = getUrlLogo();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(getMailFrom());
                helper.setBcc(getMailCc());
                helper.setTo(contatto.getEmail());
                helper.setReplyTo(getMailReplyTo());

                String totale = eventoDao.getRaccoltoTotaleByID(evento.getId_evento());

                double pasti = 0.0;
                try {
                    double totaleKg = Double.parseDouble(totale);
                    double ciboCaneKg = Double.parseDouble(getCiboCane());
                    if (ciboCaneKg > 0) {
                        pasti = Math.round(totaleKg / ciboCaneKg);
                    }
                } catch (NumberFormatException ex) {
                    logger.error("Errore di conversione nel calcolo dei pasti: " + ex.getMessage());
                }
                String numPasti = String.valueOf((int)pasti);


                helper.setSubject("Ringraziamento per la raccolta cibo del " + evento.getDt_evento());

                StringBuilder content = new StringBuilder();
                content.append("<html><body><p>Ciao " + contatto.getNome() + ",<br>a nome dei nostri pelosi ti ringraziamo per aver regalato un po' del tuo tempo per la raccolta cibo.<br>");
                content.append("<p>Presso <b>" + puntoDiRaccolta.getRag_sociale() + "</b> abbiamo raccolto un totale di <b>" + totale + " kg</b>, che corrispondono a <b>" + numPasti + "</b> pasti 0,400 kg a pasto<br></p>");
                content.append("<p>Pance piene per tutti!</p>");
                content.append("<p>Grazie di cuore!</p><br>");
                content.append(getInfoContatto( contattoLogged));
                content.append("Responsabile raccolte cibo<br>" + organizzazione.getRag_sociale() + " " + organizzazione.getSigla_tipo_organizzazione() + "</p>");
                content.append("<p><div align='center'><img src='" + urlLogo + "' style='width:100px;'></div></p>");
                content.append(getStrFooter());
                content.append("</body></html>");

                helper.setText(content.toString(), true);
            }
        };
    }


    private String getInfoContatto(Contatto contattoLogged) {
        String s = contattoLogged.getDescrizione();
        if(contattoLogged.getCellulare() != null) {s = s + "<br>cel. " + contattoLogged.getCellulare();}
        if(contattoLogged.getTelefono_1() != null) {s = s + " tel. " + contattoLogged.getTelefono_1();}
        if(contattoLogged.getTelefono_2() != null) {s = s + " " + contattoLogged.getTelefono_2() + "<br>";}else {s = s + "<br>";}
        return s;
    }
}