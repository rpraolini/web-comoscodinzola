package it.asso.core.common;

import java.io.FileInputStream;
import java.io.IOException;

import it.asso.core.dao.notifiche.NotificheDAO;
import it.asso.core.model.notifiche.Notifica;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;


@Controller
public class SendPushFCM {

	@Autowired
    NotificheDAO notificheDao;

//	@Value("${AUTH_KEY_FCM}")
//	private String authKey;
//	@Value("${API_URL_FCM}")
//	private String FMCurl;
	@Value("${path_doc}")
	private String path_doc;
	
	@Value("${path_firebase}")
	private String path_firebase;

	public void initializeFirebase() {
		try {
			/*if (path_firebase == null) {
				System.err.println("firebase.serviceAccountKey not found in app.properties!");
				System.exit(1);
			}*/

			FileInputStream serviceAccount = new FileInputStream(path_firebase);

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://app-comoscodinzola.firebaseio.com") // Sostituisci con il tuo Project ID
					.build();

			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    } 

	public String sendNotifica(Notifica notifica) throws Exception {

		try {
		    @SuppressWarnings("unused")
			FirebaseApp firebaseApp = FirebaseApp.getInstance();
		    // Se l'istanza esiste, puoi utilizzare firebaseApp per le operazioni necessarie
		} catch (IllegalStateException e) {
			initializeFirebase();
		}
		
		Message message = Message.builder()
				.setNotification(Notification.builder().setTitle(notifica.getTitolo_notifica())
						.setBody(notifica.getDescr_notifica()).build())
				.setTopic("/topics/" + notifica.getEvento().trim())

				.build();

		String response = FirebaseMessaging.getInstance().send(message);
		System.out.println("Successfully sent message: " + response);
		return "Successfully sent message: " + response;
	}

	// vecchia modalita di invio push
	/*
	 * public String send(Notifica notifica) { RestTemplate restTemplate = new
	 * RestTemplate(); HttpHeaders httpHeaders = new HttpHeaders();
	 * //httpHeaders.set("Authorization", "key=" + authKey);
	 * //httpHeaders.set("Content-Type", "application/json");
	 * 
	 * notifica.setEvento("test");
	 * 
	 * JSONObject json = new JSONObject(); json.put("to", "/topics/" +
	 * notifica.getEvento().trim()); json.put("priority", "high");
	 * json.put("collapse_key", "type_a"); json.put("analytics_label",
	 * notifica.getEvento().trim());
	 * 
	 * JSONObject info = new JSONObject(); info.put("title",
	 * notifica.getTitolo_notifica()); info.put("body",
	 * notifica.getDescr_notifica()); info.put("sound", "default");
	 * 
	 * info.put("badge", "8"); info.put("color", "#FF9900");
	 * info.put("click_action", "FCM_PLUGIN_ACTIVITY"); info.put("icon",
	 * "fcm_push_icon"); json.put("notification", info);
	 * 
	 * JSONObject infoData = new JSONObject(); infoData.put("title",
	 * notifica.getTitolo_testo()); infoData.put("body", notifica.getDescr_testo());
	 * json.put("data", infoData);
	 * 
	 * HttpEntity<String> httpEntity = new HttpEntity<>(json.toString(),
	 * httpHeaders); String result = ""; //restTemplate.postForObject(FMCurl,
	 * httpEntity, String.class); return result; }
	 */

	public String sendPushAdottabile(String nome) throws Exception {
		Notifica notifica = new Notifica();
		notifica.setTitolo_notifica(String.format(Def.EVENTO_PUSH_TITOLO_ADOTTABILE, nome));
		notifica.setDescr_notifica(String.format(Def.EVENTO_PUSH_TESTO_ADOTTABILE, nome));
		notifica.setTitolo_testo(String.format(Def.EVENTO_PUSH_TITOLO_ADOTTABILE, nome));
		notifica.setDescr_testo(String.format(Def.EVENTO_PUSH_TESTO_ADOTTABILE, nome));
		notifica.setId_evento(Def.EVENTO_PUSH_ADOTTABILE);
		notifica.setEvento(Def.EVENTO_PUSH_ADOTTABILE);
		notifica.setAttiva(Def.NUM_UNO);
		notificheDao.saveOrUpdate(notifica);
		return sendNotifica(notifica);

	}
}
