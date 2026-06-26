package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.dashboard.AdminDashboardResponse;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.enums.*;
import uz.sdg.sos.repository.*;
import uz.sdg.sos.service.AdminDashboardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final MedicalEventRepository medicalEventRepository;
    private final ClinicRepository clinicRepository;
    private final ClinicApplicationRepository clinicApplicationRepository;
    private final DmedSyncRepository dmedSyncRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<?> getDashboard() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime thisWeekStart = now.minusDays(7);
            LocalDateTime thisMonthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

            AdminDashboardResponse response = AdminDashboardResponse.builder()
                    .generatedAt(now)
                    .medicalEvents(buildMedicalEventStats(todayStart, thisWeekStart, thisMonthStart))
                    .clinics(buildClinicStats(now, thisMonthStart))
                    .clinicApplications(buildApplicationStats(thisMonthStart))
                    .dmedSync(buildDmedSyncStats(todayStart))
                    .users(buildUserStats())
                    .systemAlerts(buildSystemAlerts(now))
                    .build();

            return new ApiResponse<>(true, response);
        } catch (Exception e) {
            log.error("Error building admin dashboard", e);
            return new ApiResponse<>(e.getClass().getSimpleName() + ": " + e.getMessage(), false);
        }
    }

    private AdminDashboardResponse.MedicalEventStats buildMedicalEventStats(
            LocalDateTime todayStart, LocalDateTime thisWeekStart, LocalDateTime thisMonthStart) {

        long total = medicalEventRepository.count();
        long today = medicalEventRepository.countByCreatedAtAfter(todayStart);
        long thisWeek = medicalEventRepository.countByCreatedAtAfter(thisWeekStart);
        long thisMonth = medicalEventRepository.countByCreatedAtAfter(thisMonthStart);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put(MedicalEventStatus.PENDING.name(), medicalEventRepository.countByStatus(MedicalEventStatus.PENDING));
        byStatus.put(MedicalEventStatus.NOTIFIED.name(), medicalEventRepository.countByStatus(MedicalEventStatus.NOTIFIED));
        byStatus.put(MedicalEventStatus.FAILED.name(), medicalEventRepository.countByStatus(MedicalEventStatus.FAILED));

        Map<String, Long> byUrgency = new LinkedHashMap<>();
        byUrgency.put("1h", medicalEventRepository.countByHoursUntilContact(1));
        byUrgency.put("2h", medicalEventRepository.countByHoursUntilContact(2));
        byUrgency.put("4h", medicalEventRepository.countByHoursUntilContact(4));
        byUrgency.put("8h", medicalEventRepository.countByHoursUntilContact(8));
        byUrgency.put("24h", medicalEventRepository.countByHoursUntilContact(24));

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> rawDailyCounts = medicalEventRepository.countByDayAfter(thirtyDaysAgo);
        List<AdminDashboardResponse.DailyCount> last30Days = rawDailyCounts.stream()
                .map(row -> AdminDashboardResponse.DailyCount.builder()
                        .date(row[0] != null ? row[0].toString() : "")
                        .count(row[1] != null ? ((Number) row[1]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());

        List<Object[]> rawTopClinics = medicalEventRepository.topClinicsByEventCount();
        List<AdminDashboardResponse.ClinicEventCount> topClinics = rawTopClinics.stream()
                .map(row -> {
                    String clinicIdStr = row[0] != null ? row[0].toString() : "";
                    long count = row[1] != null ? ((Number) row[1]).longValue() : 0L;
                    String clinicName = "";
                    try {
                        UUID clinicUuid = UUID.fromString(clinicIdStr);
                        clinicName = clinicRepository.findById(clinicUuid)
                                .map(ClinicEntity::getName)
                                .orElse("Noma'lum klinika");
                    } catch (IllegalArgumentException e) {
                        clinicName = "Noma'lum klinika";
                    }
                    return AdminDashboardResponse.ClinicEventCount.builder()
                            .clinicId(clinicIdStr)
                            .clinicName(clinicName)
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());

        return AdminDashboardResponse.MedicalEventStats.builder()
                .total(total)
                .today(today)
                .thisWeek(thisWeek)
                .thisMonth(thisMonth)
                .byStatus(byStatus)
                .byUrgency(byUrgency)
                .last30Days(last30Days)
                .topClinics(topClinics)
                .build();
    }

    private AdminDashboardResponse.ClinicStats buildClinicStats(
            LocalDateTime now, LocalDateTime thisMonthStart) {

        long total = clinicRepository.count();
        long active = clinicRepository.countByStatus(ClinicStatus.ACTIVE);
        long inactive = clinicRepository.countByStatus(ClinicStatus.INACTIVE);
        long newThisMonth = clinicRepository.countByCreatedAtAfter(thisMonthStart);

        Map<String, Long> byType = new LinkedHashMap<>();
        for (ClinicType type : ClinicType.values()) {
            byType.put(type.name(), clinicRepository.countByClinicType(type));
        }

        LocalDateTime thirtyDaysFromNow = now.plusDays(30);
        long expiringKeysIn30Days = clinicRepository.findExpiringKeys(now, thirtyDaysFromNow).size();

        return AdminDashboardResponse.ClinicStats.builder()
                .total(total)
                .active(active)
                .inactive(inactive)
                .byType(byType)
                .newThisMonth(newThisMonth)
                .expiringKeysIn30Days(expiringKeysIn30Days)
                .build();
    }

    private AdminDashboardResponse.ApplicationStats buildApplicationStats(LocalDateTime thisMonthStart) {
        long pending = clinicApplicationRepository.countByStatus(ClinicApplicationStatus.PENDING);
        long totalApproved = clinicApplicationRepository.countByStatus(ClinicApplicationStatus.APPROVED);
        long totalRejected = clinicApplicationRepository.countByStatus(ClinicApplicationStatus.REJECTED);
        long approvedThisMonth = clinicApplicationRepository
                .countByStatusAndCreatedAtAfter(ClinicApplicationStatus.APPROVED, thisMonthStart);
        long rejectedThisMonth = clinicApplicationRepository
                .countByStatusAndCreatedAtAfter(ClinicApplicationStatus.REJECTED, thisMonthStart);

        long totalDecided = totalApproved + totalRejected;
        double approvalRate = totalDecided == 0 ? 0.0 : (totalApproved * 100.0) / totalDecided;

        return AdminDashboardResponse.ApplicationStats.builder()
                .pending(pending)
                .approvedThisMonth(approvedThisMonth)
                .rejectedThisMonth(rejectedThisMonth)
                .totalApproved(totalApproved)
                .totalRejected(totalRejected)
                .approvalRate(Math.round(approvalRate * 100.0) / 100.0)
                .build();
    }

    private AdminDashboardResponse.DmedSyncStats buildDmedSyncStats(LocalDateTime todayStart) {
        long total = dmedSyncRepository.count();
        long success = dmedSyncRepository.countByStatus(DmedSyncStatus.SUCCESS);
        long failed = dmedSyncRepository.countByStatus(DmedSyncStatus.FAILED);
        long failedToday = dmedSyncRepository.countByStatusAndCreatedAtAfter(DmedSyncStatus.FAILED, todayStart);
        long successToday = dmedSyncRepository.countByStatusAndCreatedAtAfter(DmedSyncStatus.SUCCESS, todayStart);

        double successRate = total == 0 ? 0.0 : (success * 100.0) / total;

        return AdminDashboardResponse.DmedSyncStats.builder()
                .total(total)
                .success(success)
                .failed(failed)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .failedToday(failedToday)
                .successToday(successToday)
                .build();
    }

    private AdminDashboardResponse.UserStats buildUserStats() {
        long total = userRepository.count();
        long admins = userRepository.countAllByAccountType(AccountTypeEnum.ADMIN);
        long operators = userRepository.countAllByAccountType(AccountTypeEnum.OPERATOR);
        long doctors = userRepository.countAllByAccountType(AccountTypeEnum.DOCTOR);

        return AdminDashboardResponse.UserStats.builder()
                .total(total)
                .admins(admins)
                .operators(operators)
                .doctors(doctors)
                .build();
    }

    private List<AdminDashboardResponse.SystemAlert> buildSystemAlerts(LocalDateTime now) {
        List<AdminDashboardResponse.SystemAlert> alerts = new ArrayList<>();

        long failedDmed = dmedSyncRepository.countByStatus(DmedSyncStatus.FAILED);
        if (failedDmed > 0) {
            alerts.add(AdminDashboardResponse.SystemAlert.builder()
                    .type("DMED_SYNC_FAILED")
                    .message(failedDmed + " ta DMED sync muvaffaqiyatsiz — qayta urinish kerak")
                    .severity("CRITICAL")
                    .build());
        }

        long expiringKeys = clinicRepository.findExpiringKeys(now, now.plusDays(30)).size();
        if (expiringKeys > 0) {
            alerts.add(AdminDashboardResponse.SystemAlert.builder()
                    .type("SECRET_KEY_EXPIRING")
                    .message(expiringKeys + " ta klinikaning secret key 30 kun ichida tugaydi")
                    .severity("WARNING")
                    .build());
        }

        long pendingApplications = clinicApplicationRepository.countByStatus(ClinicApplicationStatus.PENDING);
        if (pendingApplications > 0) {
            alerts.add(AdminDashboardResponse.SystemAlert.builder()
                    .type("PENDING_APPLICATIONS")
                    .message(pendingApplications + " ta klinika arizasi ko'rib chiqilmagan")
                    .severity("WARNING")
                    .build());
        }

        if (alerts.isEmpty()) {
            alerts.add(AdminDashboardResponse.SystemAlert.builder()
                    .type("SYSTEM_OK")
                    .message("Tizim normal ishlayapti")
                    .severity("INFO")
                    .build());
        }

        return alerts;
    }
}
