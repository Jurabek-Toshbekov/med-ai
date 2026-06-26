package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uz.sdg.sos.exception.BusinessException;
import uz.sdg.sos.service.SmsService;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sms.mock", havingValue = "false", matchIfMissing = true)
public class EskizSmsServiceImpl implements SmsService {

    private static final String BASE_URL = "https://notify.eskiz.uz/api";

    private final RestTemplate restTemplate;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();

    @Value("${eskiz.email}")
    private String email;

    @Value("${eskiz.password}")
    private String password;

    @Value("${eskiz.from}")
    private String from;

    @PostConstruct
    public void init() {
        try {
            fetchAndStoreToken();
        } catch (Exception e) {
            log.warn("[Eskiz] Startup token olishda xatolik: {}. Birinchi SMS so'rovda qayta uriniladi.", e.getMessage());
        }
    }

    @Override
    public void sendOtp(String phoneNumber, String code) {
        send(stripPlus(phoneNumber), buildOtpMessage(code));
    }

    @Override
    public void sendNotification(String phoneNumber, String patientFullName, int hours) {
        send(stripPlus(phoneNumber), buildNotificationMessage(patientFullName, hours));
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void send(String mobile, String message) {
        try {
            doSend(mobile, message);
        } catch (HttpClientErrorException.Unauthorized e) {
            fetchAndStoreToken();
            doSend(mobile, message);
        }
    }

    private void doSend(String mobilePhone, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(requireToken());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("mobile_phone", mobilePhone);
        body.add("message", message);
        body.add("from", from);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/message/sms/send", entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[Eskiz] SMS yuborilmadi: status={}", response.getStatusCode());
            throw new BusinessException(503, "SMS yuborishda xatolik");
        }
        log.info("[Eskiz] SMS yuborildi → {}", mobilePhone);
    }

    private void fetchAndStoreToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", email);
        body.add("password", password);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/auth/login", new HttpEntity<>(body, headers), Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error("[Eskiz] Token olishda xatolik: status={}", response.getStatusCode());
            throw new BusinessException(503, "SMS servisi ishlamayapti");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        String token = (String) data.get("token");
        cachedToken.set(token);
        log.info("[Eskiz] Token olindi va xotirada saqlandi");
    }

    private String requireToken() {
        String token = cachedToken.get();
        if (token == null) {
            fetchAndStoreToken();
            token = cachedToken.get();
        }
        return token;
    }

    private static String buildOtpMessage(String code) {
        return "Tasdiqlash kodi: " + code;
    }

    private static String buildNotificationMessage(String patientFullName, int hours) {
        return "Sizning kuzatuvingizda bo'lgan fuqaro " + maskName(patientFullName) +
               " uyiga chiqarildi. DMED sahifasini ko'rib, " + hours +
               " soat ichida holati haqida xabar bering.";
    }

    static String maskName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "***";
        String[] parts = fullName.trim().split("\\s+");
        String maskedLast  = maskLastName(parts[0]);
        String firstInitial = parts.length > 1 ? parts[1].charAt(0) + "." : "";
        String midInitial   = parts.length > 2 ? parts[2].charAt(0) + "." : "";
        return (maskedLast + " " + firstInitial + midInitial).trim();
    }

    private static String maskLastName(String name) {
        if (name.length() <= 4) return name;
        int keep = Math.min(4, name.length() - 1);
        return name.substring(0, keep) + "***" + name.charAt(name.length() - 1);
    }

    private static String stripPlus(String phone) {
        return phone.startsWith("+") ? phone.substring(1) : phone;
    }
}
