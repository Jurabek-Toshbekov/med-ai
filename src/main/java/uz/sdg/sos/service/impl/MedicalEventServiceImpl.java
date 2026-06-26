package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.dmed.DmedPatientData;
import uz.sdg.sos.dto.medicalevent.MedicalEventIntakeRequest;
import uz.sdg.sos.dto.medicalevent.MedicalEventIntakeResponse;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.MedicalEventEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.entity.enums.MedicalEventStatus;
import uz.sdg.sos.repository.ClinicRepository;
import uz.sdg.sos.repository.MedicalEventRepository;
import uz.sdg.sos.service.DmedMockService;
import uz.sdg.sos.service.DmedSyncService;
import uz.sdg.sos.service.GeminiService;
import uz.sdg.sos.service.MedicalEventService;
import uz.sdg.sos.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalEventServiceImpl implements MedicalEventService {

    private final ClinicRepository clinicRepository;
    private final MedicalEventRepository medicalEventRepository;
    private final DmedMockService dmedMockService;
    private final DmedSyncService dmedSyncService;
    private final GeminiService geminiService;
    private final NotificationService notificationService;

    private static final String JSHSHIR_REGEX = "^[0-9]{14}$";

    @Transactional
    @Override
    public ApiResponse<?> processIntake(String secretKey, MedicalEventIntakeRequest request) {

        // Layer 1 — JWT validation
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return new ApiResponse<>(false, "INVALID_JWT");
        }

        // Layer 2 — Secret Key validation
        if (secretKey == null || secretKey.isBlank()) {
            return new ApiResponse<>(false, "KEY_NOT_FOUND");
        }
        Optional<ClinicEntity> clinicOpt = clinicRepository.findBySecretKey(secretKey);
        if (clinicOpt.isEmpty()) {
            return new ApiResponse<>(false, "KEY_NOT_FOUND");
        }
        ClinicEntity clinic = clinicOpt.get();
        if (clinic.getStatus() != ClinicStatus.ACTIVE) {
            return new ApiResponse<>(false, "CLINIC_INACTIVE");
        }
        if (clinic.getSecretKeyExpiresAt() == null || LocalDateTime.now().isAfter(clinic.getSecretKeyExpiresAt())) {
            return new ApiResponse<>(false, "KEY_EXPIRED");
        }

        // Layer 3 — JSHSHIR validation
        if (request.getJshshir() == null || !request.getJshshir().matches(JSHSHIR_REGEX)) {
            return new ApiResponse<>(false, "INVALID_JSHSHIR");
        }

        // DMED lookup
        Optional<DmedPatientData> patientOpt = dmedMockService.findByJshshir(request.getJshshir());
        if (patientOpt.isEmpty()) {
            return new ApiResponse<>(false, "PATIENT_NOT_IN_DMED");
        }
        DmedPatientData patient = patientOpt.get();

        // AI urgency analysis
        Integer hoursUntilContact = geminiService.analyzeUrgency(patient, request.getDiagnosis(), request.getConclusion());

        // Save event
        LocalDateTime now = LocalDateTime.now();
        MedicalEventEntity event = MedicalEventEntity.builder()
                .jshshir(request.getJshshir())
                .patientFullName(patient.getFullName())
                .diagnosis(request.getDiagnosis())
                .conclusion(request.getConclusion())
                .clinicId(clinic.getId())
                .familyDoctorId(patient.getFamilyDoctor().getId())
                .familyDoctorPhone(patient.getFamilyDoctor().getPhoneNumber())
                .familyDoctorFcmToken(patient.getFamilyDoctor().getFcmToken())
                .hoursUntilContact(hoursUntilContact)
                .notifyAt(now.plusHours(hoursUntilContact))
                .status(MedicalEventStatus.PENDING)
                .build();
        medicalEventRepository.save(event);

        // Schedule notification
        notificationService.scheduleNotification(event);

        // DMED medkartasiga yozish (mock — hozircha shartnoma yo'q)
        dmedSyncService.syncToDmed(event, clinic);

        MedicalEventIntakeResponse response = MedicalEventIntakeResponse.builder()
                .eventId(event.getId())
                .patientName(patient.getFullName())
                .familyDoctorPhone(patient.getFamilyDoctor().getPhoneNumber())
                .hoursUntilContact(hoursUntilContact)
                .notifyAt(event.getNotifyAt())
                .status("SCHEDULED")
                .build();

        return new ApiResponse<>(true, "Tibbiy hodisa muvaffaqiyatli qayd etildi", response);
    }
}
