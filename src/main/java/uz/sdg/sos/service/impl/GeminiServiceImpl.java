package uz.sdg.sos.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.sdg.sos.dto.dmed.DmedPatientData;
import uz.sdg.sos.dto.labevent.LabAnalysisResult;
import uz.sdg.sos.entity.enums.LabResultFlag;
import uz.sdg.sos.service.GeminiService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String apiUrl;

    private static final int DEFAULT_HOURS = 8;
    private static final int MAX_RETRIES = 2;

    @Override
    public Integer analyzeUrgency(DmedPatientData patient, String diagnosis, String conclusion) {
        String prompt = buildPrompt(patient, diagnosis, conclusion);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String rawResponse = callGemini(prompt);
                Integer hours = parseHours(rawResponse);
                if (hours != null) {
                    log.info("Gemini urgency analysis: {} hours (attempt {})", hours, attempt + 1);
                    return hours;
                }
                log.warn("Gemini returned unparseable response on attempt {}: '{}'", attempt + 1, rawResponse);
            } catch (Exception e) {
                log.warn("Gemini call failed on attempt {}: {}", attempt + 1, e.getMessage());
            }
        }

        log.warn("Gemini fallback: returning default {} hours", DEFAULT_HOURS);
        return DEFAULT_HOURS;
    }

    private String callGemini(String prompt) {
        String url = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        if (response == null) {
            throw new RuntimeException("Empty response from Gemini");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        return ((String) parts.get(0).get("text")).trim();
    }

    private Integer parseHours(String rawResponse) {
        if (rawResponse == null) return null;
        try {
            int value = Integer.parseInt(rawResponse.trim());
            if (value == 1 || value == 2 || value == 4 || value == 8 || value == 24) {
                return value;
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildPrompt(DmedPatientData patient, String diagnosis, String conclusion) {
        int age = calculateAge(patient.getBirthDate());
        String chronicDiseases = patient.getChronicDiseases() == null || patient.getChronicDiseases().isEmpty()
                ? "none" : String.join(", ", patient.getChronicDiseases());
        String medications = patient.getCurrentMedications() == null || patient.getCurrentMedications().isEmpty()
                ? "none" : String.join(", ", patient.getCurrentMedications());

        return String.format(
                "You are a medical urgency analyzer for a family doctor notification system in Uzbekistan.\n" +
                "Patient data:\n\n" +
                "Age: %d\n" +
                "Chronic diseases: %s\n" +
                "Current medications: %s\n" +
                "New diagnosis/event: %s\n" +
                "Doctor's conclusion: %s\n" +
                "Source: CLINIC\n\n" +
                "Your task: Determine how many hours the family doctor should wait before " +
                "contacting this patient after discharge/treatment.\n" +
                "Rules:\n" +
                "- Emergency/critical conditions: return 1\n" +
                "- Severe chronic disease complications: return 2\n" +
                "- Moderate conditions requiring follow-up: return 4\n" +
                "- Routine conditions with medication adjustment: return 8\n" +
                "- Minor conditions: return 24\n\n" +
                "CRITICAL: Your response must be a single integer only.\n" +
                "No explanation. No text. No punctuation. Just the number.\n" +
                "Examples of valid responses: 1, 2, 4, 8, 24, 48, 72",
                age, chronicDiseases, medications, diagnosis, conclusion
        );
    }

    private int calculateAge(String birthDate) {
        try {
            LocalDate dob = LocalDate.parse(birthDate);
            return Period.between(dob, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public LabAnalysisResult analyzeLabResult(String testName, String testResult, String unit, String referenceRange) {
        String prompt = buildLabPrompt(testName, testResult, unit, referenceRange);

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String rawResponse = callGemini(prompt);
                LabAnalysisResult result = parseLabResult(rawResponse);
                if (result != null) {
                    log.info("Gemini lab analysis: flag={}, hours={} (attempt {})",
                            result.getFlag(), result.getHoursUntilContact(), attempt + 1);
                    return result;
                }
                log.warn("Gemini returned unparseable lab response on attempt {}: '{}'", attempt + 1, rawResponse);
            } catch (Exception e) {
                log.warn("Gemini lab call failed on attempt {}: {}", attempt + 1, e.getMessage());
            }
        }

        log.warn("Gemini lab fallback: returning ABNORMAL with {} hours", DEFAULT_HOURS);
        return LabAnalysisResult.builder()
                .flag(LabResultFlag.ABNORMAL)
                .hoursUntilContact(DEFAULT_HOURS)
                .conclusion("AI tahlil qila olmadi. Qo'lda tekshirilsin.")
                .build();
    }

    private LabAnalysisResult parseLabResult(String rawResponse) {
        if (rawResponse == null) return null;
        try {
            String json = rawResponse.trim();
            int start = json.indexOf('{');
            int end = json.lastIndexOf('}');
            if (start == -1 || end == -1 || end <= start) return null;
            json = json.substring(start, end + 1);

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            String flagStr = (String) map.get("flag");
            Object hoursObj = map.get("hoursUntilContact");
            String conclusion = (String) map.get("conclusion");

            if (flagStr == null || hoursObj == null) return null;

            LabResultFlag flag = LabResultFlag.valueOf(flagStr.trim().toUpperCase());
            int hours = ((Number) hoursObj).intValue();

            return LabAnalysisResult.builder()
                    .flag(flag)
                    .hoursUntilContact(hours)
                    .conclusion(conclusion)
                    .build();
        } catch (Exception e) {
            log.warn("Lab result parse error: {}", e.getMessage());
            return null;
        }
    }

    private String buildLabPrompt(String testName, String testResult, String unit, String referenceRange) {
        return String.format(
                "Laboratoriya natijasini tahlil qil va JSON qaytар:\n" +
                "Test: %s\n" +
                "Natija: %s %s\n" +
                "Referens oraliq: %s\n\n" +
                "Quyidagi JSON formatda javob ber (boshqa hech narsa yozma):\n" +
                "{\n" +
                "  \"flag\": \"NORMAL|ABNORMAL|CRITICAL\",\n" +
                "  \"hoursUntilContact\": <1|2|4|8|24>,\n" +
                "  \"conclusion\": \"<qisqa tibbiy izoh uzbek tilida>\"\n" +
                "}\n\n" +
                "Qoidalar:\n" +
                "- NORMAL: referens oraliq ichida → hoursUntilContact: 24\n" +
                "- ABNORMAL: referens oraliqdan biroz chiqgan → hoursUntilContact: 8 yoki 4\n" +
                "- CRITICAL: xavfli qiymat → hoursUntilContact: 1 yoki 2",
                testName,
                testResult,
                unit != null ? unit : "",
                referenceRange != null ? referenceRange : "ko'rsatilmagan"
        );
    }
}
