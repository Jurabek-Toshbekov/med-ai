package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.dmed.DmedPatientData;
import uz.sdg.sos.dto.intake.UnifiedIntakeRequest;
import uz.sdg.sos.dto.labevent.LabAnalysisResult;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.LabEntity;
import uz.sdg.sos.entity.LabEventEntity;
import uz.sdg.sos.entity.MedicalEventEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.entity.enums.MedicalEventStatus;
import uz.sdg.sos.repository.ClinicRepository;
import uz.sdg.sos.repository.LabEventRepository;
import uz.sdg.sos.repository.LabRepository;
import uz.sdg.sos.repository.MedicalEventRepository;
import uz.sdg.sos.service.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static uz.sdg.sos.service.impl.EskizSmsServiceImpl.maskName;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnifiedIntakeServiceImpl implements UnifiedIntakeService {

    private final ClinicRepository clinicRepository;
    private final LabRepository labRepository;
    private final MedicalEventRepository medicalEventRepository;
    private final LabEventRepository labEventRepository;
    private final GeminiService geminiService;
    private final DmedSyncService dmedSyncService;
    private final DmedMockService dmedMockService;
    private final NotificationService notificationService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;

    private static final String JSHSHIR_REGEX = "^[0-9]{14}$";

    @Override
    @Transactional
    public ApiResponse<?> processIntake(String authHeader, String secretKey, UnifiedIntakeRequest request) {

        // Layer 1 — JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return new ApiResponse<>(false, "INVALID_JWT");
        }

        // Layer 2 — Secret Key: avval klinika, keyin lab
        if (secretKey == null || secretKey.isBlank()) {
            return new ApiResponse<>(false, "KEY_NOT_FOUND");
        }

        Optional<ClinicEntity> clinicOpt = clinicRepository.findBySecretKey(secretKey);
        if (clinicOpt.isPresent()) {
            return processClinicIntake(clinicOpt.get(), request);
        }

        Optional<LabEntity> labOpt = labRepository.findBySecretKey(secretKey);
        if (labOpt.isPresent()) {
            return processLabIntake(labOpt.get(), request);
        }

        return new ApiResponse<>(false, "KEY_NOT_FOUND");
    }

    // ─── Klinika oqimi ────────────────────────────────────────────────────────
    private ApiResponse<?> processClinicIntake(ClinicEntity clinic, UnifiedIntakeRequest request) {
        if (clinic.getStatus() != ClinicStatus.ACTIVE) {
            return new ApiResponse<>(false, "CLINIC_INACTIVE");
        }
        if (clinic.getSecretKeyExpiresAt() == null || LocalDateTime.now().isAfter(clinic.getSecretKeyExpiresAt())) {
            return new ApiResponse<>(false, "KEY_EXPIRED");
        }
        if (request.getJshshir() == null || !request.getJshshir().matches(JSHSHIR_REGEX)) {
            return new ApiResponse<>(false, "INVALID_JSHSHIR");
        }
        if (request.getDiagnosis() == null || request.getDiagnosis().isBlank()) {
            return new ApiResponse<>(false, "DIAGNOSIS_REQUIRED");
        }

        // DMED dan bemor ma'lumotlari (mavjud bo'lsa), yo'q bo'lsa requestdan
        String patientFullName = request.getPatientFullName();
        String familyDoctorId = request.getFamilyDoctorId();
        String familyDoctorPhone = request.getFamilyDoctorPhone();
        String familyDoctorFcmToken = request.getFamilyDoctorFcmToken();

        Optional<DmedPatientData> patientOpt = dmedMockService.findByJshshir(request.getJshshir());
        if (patientOpt.isPresent()) {
            DmedPatientData p = patientOpt.get();
            patientFullName    = p.getFullName();
            familyDoctorId     = p.getFamilyDoctor().getId();
            familyDoctorPhone  = p.getFamilyDoctor().getPhoneNumber();
            familyDoctorFcmToken = p.getFamilyDoctor().getFcmToken();
        }

        // Gemini AI urgency tahlili
        Integer hours = geminiService.analyzeUrgency(
                patientOpt.orElse(null),
                request.getDiagnosis(),
                request.getConclusion()
        );

        MedicalEventEntity event = MedicalEventEntity.builder()
                .jshshir(request.getJshshir())
                .patientFullName(patientFullName)
                .diagnosis(request.getDiagnosis())
                .conclusion(request.getConclusion())
                .clinicId(clinic.getId())
                .familyDoctorId(familyDoctorId)
                .familyDoctorPhone(familyDoctorPhone)
                .familyDoctorFcmToken(familyDoctorFcmToken)
                .hoursUntilContact(hours)
                .notifyAt(LocalDateTime.now().plusHours(hours))
                .status(MedicalEventStatus.PENDING)
                .build();
        medicalEventRepository.save(event);

        try {
            notificationService.scheduleNotification(event);
            dmedSyncService.syncToDmed(event, clinic);
            log.info("[INTAKE][CLINIC] clinicId={}, eventId={}, hours={}",
                    clinic.getId(), event.getId(), hours);
            return new ApiResponse<>(true, "Tibbiy hodisa muvaffaqiyatli qayd etildi", event.getId());
        } catch (Exception e) {
            event.setStatus(MedicalEventStatus.FAILED);
            medicalEventRepository.save(event);
            log.error("[INTAKE][CLINIC] xatolik: {}", e.getMessage());
            return new ApiResponse<>(false, "Server xatoligi: " + e.getMessage());
        }
    }

    // ─── Laboratoriya oqimi ───────────────────────────────────────────────────
    private ApiResponse<?> processLabIntake(LabEntity lab, UnifiedIntakeRequest request) {
        if (lab.getStatus() != ClinicStatus.ACTIVE) {
            return new ApiResponse<>(false, "LAB_INACTIVE");
        }
        if (lab.getSecretKeyExpiresAt() == null || LocalDateTime.now().isAfter(lab.getSecretKeyExpiresAt())) {
            return new ApiResponse<>(false, "KEY_EXPIRED");
        }
        if (request.getJshshir() == null || !request.getJshshir().matches(JSHSHIR_REGEX)) {
            return new ApiResponse<>(false, "INVALID_JSHSHIR");
        }
        if (request.getTestName() == null || request.getTestResult() == null) {
            return new ApiResponse<>(false, "TEST_NAME_AND_RESULT_REQUIRED");
        }

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
            LabAnalysisResult analysis = geminiService.analyzeLabResult(
                    request.getTestName(), request.getTestResult(),
                    request.getUnit(), request.getReferenceRange()
            );
            event.setFlag(analysis.getFlag());
            event.setHoursUntilContact(analysis.getHoursUntilContact());
            event.setConclusion(analysis.getConclusion());
            event.setNotifyAt(LocalDateTime.now().plusHours(analysis.getHoursUntilContact()));

            sendSms(event);
            sendPush(event);

            event.setStatus(MedicalEventStatus.NOTIFIED);
            labEventRepository.save(event);
            dmedSyncService.syncToDmed(event, lab);

            log.info("[INTAKE][LAB] labId={}, eventId={}, flag={}",
                    lab.getId(), event.getId(), event.getFlag());
            return new ApiResponse<>(true, "Laboratoriya natijasi muvaffaqiyatli qayd etildi", event.getId());
        } catch (Exception e) {
            event.setStatus(MedicalEventStatus.FAILED);
            labEventRepository.save(event);
            log.error("[INTAKE][LAB] xatolik: {}", e.getMessage());
            return new ApiResponse<>(false, "Server xatoligi: " + e.getMessage());
        }
    }

    private void sendSms(LabEventEntity event) {
        try {
            smsService.sendNotification(event.getFamilyDoctorPhone(),
                    event.getPatientFullName(), event.getHoursUntilContact());
        } catch (Exception e) {
            log.error("[SMS][LAB] eventId={}: {}", event.getId(), e.getMessage());
        }
    }

    private void sendPush(LabEventEntity event) {
        try {
            String masked = maskName(event.getPatientFullName());
            pushNotificationService.send(
                    event.getFamilyDoctorFcmToken(),
                    "Laboratoriya natijasi tayyor",
                    masked + " ning " + event.getTestName() + " natijasi. "
                            + event.getHoursUntilContact() + " soat ichida xabar bering.",
                    event.getId().toString()
            );
        } catch (Exception e) {
            log.error("[FCM][LAB] eventId={}: {}", event.getId(), e.getMessage());
        }
    }
}
