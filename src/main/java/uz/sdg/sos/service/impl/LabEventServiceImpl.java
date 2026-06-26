package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.labevent.LabAnalysisResult;
import uz.sdg.sos.dto.labevent.LabEventIntakeRequest;
import uz.sdg.sos.entity.LabEntity;
import uz.sdg.sos.entity.LabEventEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.entity.enums.MedicalEventStatus;
import uz.sdg.sos.repository.LabEventRepository;
import uz.sdg.sos.repository.LabRepository;
import uz.sdg.sos.service.DmedSyncService;
import uz.sdg.sos.service.GeminiService;
import uz.sdg.sos.service.LabEventService;
import uz.sdg.sos.service.PushNotificationService;
import uz.sdg.sos.service.SmsService;

import java.time.LocalDateTime;
import java.util.Optional;

import static uz.sdg.sos.service.impl.EskizSmsServiceImpl.maskName;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabEventServiceImpl implements LabEventService {

    private final LabRepository labRepository;
    private final LabEventRepository labEventRepository;
    private final GeminiService geminiService;
    private final DmedSyncService dmedSyncService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;

    private static final String JSHSHIR_REGEX = "^[0-9]{14}$";

    @Transactional
    @Override
    public ApiResponse<?> processIntake(String authHeader, String secretKey, LabEventIntakeRequest request) {

        // Layer 1 — JWT validation
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return new ApiResponse<>(false, "INVALID_JWT");
        }

        // Layer 2 — Secret Key validation
        if (secretKey == null || secretKey.isBlank()) {
            return new ApiResponse<>(false, "KEY_NOT_FOUND");
        }
        Optional<LabEntity> labOpt = labRepository.findBySecretKey(secretKey);
        if (labOpt.isEmpty()) {
            return new ApiResponse<>(false, "KEY_NOT_FOUND");
        }
        LabEntity lab = labOpt.get();
        if (lab.getStatus() != ClinicStatus.ACTIVE) {
            return new ApiResponse<>(false, "LAB_INACTIVE");
        }
        if (lab.getSecretKeyExpiresAt() == null || LocalDateTime.now().isAfter(lab.getSecretKeyExpiresAt())) {
            return new ApiResponse<>(false, "KEY_EXPIRED");
        }

        // Layer 3 — JSHSHIR validation
        if (request.getJshshir() == null || !request.getJshshir().matches(JSHSHIR_REGEX)) {
            return new ApiResponse<>(false, "INVALID_JSHSHIR");
        }

        // Save initial entity with PENDING status
        LabEventEntity event = LabEventEntity.builder()
                .jshshir(request.getJshshir())
                .patientFullName(request.getPatientFullName())
                .labId(lab.getId())
                .testName(request.getTestName())
                .testResult(request.getTestResult())
                .unit(request.getUnit())
                .referenceRange(request.getReferenceRange())
                .familyDoctorId(request.getFamilyDoctorId())
                .familyDoctorPhone(request.getFamilyDoctorPhone())
                .familyDoctorFcmToken(request.getFamilyDoctorFcmToken())
                .status(MedicalEventStatus.PENDING)
                .build();
        labEventRepository.save(event);

        try {
            // AI analysis
            LabAnalysisResult analysis = geminiService.analyzeLabResult(
                    request.getTestName(),
                    request.getTestResult(),
                    request.getUnit(),
                    request.getReferenceRange()
            );

            event.setFlag(analysis.getFlag());
            event.setHoursUntilContact(analysis.getHoursUntilContact());
            event.setConclusion(analysis.getConclusion());
            event.setNotifyAt(LocalDateTime.now().plusHours(analysis.getHoursUntilContact()));

            // Notifications
            sendSms(event);
            sendPush(event);

            event.setStatus(MedicalEventStatus.NOTIFIED);
            labEventRepository.save(event);

            // DMED sync
            dmedSyncService.syncToDmed(event, lab);

            log.info("Lab hodisa qayd etildi → eventId={}, labId={}, flag={}",
                    event.getId(), lab.getId(), event.getFlag());

            return new ApiResponse<>(true, "Laboratoriya natijasi muvaffaqiyatli qayd etildi", event.getId());

        } catch (Exception e) {
            event.setStatus(MedicalEventStatus.FAILED);
            labEventRepository.save(event);
            log.error("Lab intake xatolik (eventId={}): {}", event.getId(), e.getMessage());
            return new ApiResponse<>(false, "Server xatoligi: " + e.getMessage());
        }
    }

    private void sendSms(LabEventEntity event) {
        try {
            smsService.sendNotification(
                    event.getFamilyDoctorPhone(),
                    event.getPatientFullName(),
                    event.getHoursUntilContact()
            );
        } catch (Exception e) {
            log.error("[SMS][LAB] Xatolik (eventId={}): {}", event.getId(), e.getMessage());
        }
    }

    private void sendPush(LabEventEntity event) {
        try {
            String masked = maskName(event.getPatientFullName());
            String title = "Laboratoriya natijasi tayyor";
            String body = masked + " ning " + event.getTestName() + " natijasi keldi. " +
                    event.getHoursUntilContact() + " soat ichida holati haqida xabar bering.";

            pushNotificationService.send(
                    event.getFamilyDoctorFcmToken(),
                    title,
                    body,
                    event.getId().toString()
            );
        } catch (Exception e) {
            log.error("[FCM][LAB] Xatolik (eventId={}): {}", event.getId(), e.getMessage());
        }
    }
}
