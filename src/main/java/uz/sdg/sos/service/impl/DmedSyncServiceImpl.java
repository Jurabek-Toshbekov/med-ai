package uz.sdg.sos.service.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import uz.sdg.sos.dto.dmed.DmedWriteRequest;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.DmedSyncEntity;
import uz.sdg.sos.entity.MedicalEventEntity;
import uz.sdg.sos.entity.enums.DmedSyncStatus;
import uz.sdg.sos.repository.DmedSyncRepository;
import uz.sdg.sos.service.DmedSyncService;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DmedSyncServiceImpl implements DmedSyncService {

    private final DmedSyncRepository dmedSyncRepository;
    private final RestTemplate restTemplate;
    private final Gson gson;

    @Value("${dmed.api.url}")
    private String dmedApiUrl;

    @Value("${dmed.api.timeout:5000}")
    private int timeout;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncToDmed(MedicalEventEntity event, ClinicEntity clinic) {

        DmedWriteRequest payload = DmedWriteRequest.builder()
                .jshshir(event.getJshshir())
                .medicalEventId(event.getId())
                .clinicId(clinic.getId())
                .clinicName(clinic.getName())
                .diagnosis(event.getDiagnosis())
                .conclusion(event.getConclusion())
                .eventDate(event.getCreatedAt() != null
                        ? event.getCreatedAt().toString()
                        : java.time.LocalDateTime.now().toString())
                .source("PRIVATE_CLINIC")
                .build();

        String requestJson = gson.toJson(payload);

        DmedSyncEntity syncLog = DmedSyncEntity.builder()
                .medicalEventId(event.getId())
                .jshshir(event.getJshshir())
                .requestBody(requestJson)
                .status(DmedSyncStatus.PENDING)
                .sentAt(LocalDateTime.now())
                .build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpRequest = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    dmedApiUrl, HttpMethod.POST, httpRequest, String.class);

            syncLog.setHttpStatus(response.getStatusCodeValue());
            syncLog.setResponseBody(response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                syncLog.setStatus(DmedSyncStatus.SUCCESS);
                log.info("[DMED-SYNC] SUCCESS → eventId={}, jshshir={}, httpStatus={}",
                        event.getId(), event.getJshshir(), response.getStatusCodeValue());
            } else {
                syncLog.setStatus(DmedSyncStatus.FAILED);
                log.warn("[DMED-SYNC] FAILED → eventId={}, httpStatus={}, body={}",
                        event.getId(), response.getStatusCodeValue(), response.getBody());
            }

        } catch (Exception e) {
            syncLog.setStatus(DmedSyncStatus.FAILED);
            syncLog.setResponseBody("Exception: " + e.getMessage());
            log.error("[DMED-SYNC] Exception → eventId={}, error={}", event.getId(), e.getMessage());
        } finally {
            try {
                dmedSyncRepository.save(syncLog);
            } catch (Exception saveEx) {
                log.error("[DMED-SYNC] DB saqlashda xatolik (eventId={}): {}", event.getId(), saveEx.getMessage());
            }
        }
    }
}
