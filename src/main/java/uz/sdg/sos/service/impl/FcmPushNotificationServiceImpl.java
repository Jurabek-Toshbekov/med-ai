package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.sdg.sos.service.PushNotificationService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushNotificationServiceImpl implements PushNotificationService {

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    private final RestTemplate restTemplate;

    @Value("${fcm.server.key:}")
    private String serverKey;

    @Override
    public void send(String fcmToken, String title, String body, String eventId) {
        if (serverKey == null || serverKey.isBlank()) {
            log.warn("[FCM] fcm.server.key sozlanmagan — push yuborilmadi (eventId={})", eventId);
            return;
        }
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("[FCM] FCM token yo'q — push yuborilmadi (eventId={})", eventId);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "key=" + serverKey);

            Map<String, Object> notification = Map.of(
                    "title", title,
                    "body", body,
                    "sound", "default"
            );
            Map<String, Object> data = Map.of(
                    "eventId", eventId,
                    "type", "MEDICAL_DISCHARGE"
            );
            Map<String, Object> payload = Map.of(
                    "to", fcmToken,
                    "notification", notification,
                    "data", data,
                    "priority", "high"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(FCM_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[FCM] Push yuborildi → token={}...  (eventId={})",
                        fcmToken.substring(0, Math.min(20, fcmToken.length())), eventId);
            } else {
                log.error("[FCM] Push yuborilmadi: status={} (eventId={})", response.getStatusCode(), eventId);
            }
        } catch (Exception e) {
            log.error("[FCM] Push xatolik (eventId={}): {}", eventId, e.getMessage());
        }
    }
}
