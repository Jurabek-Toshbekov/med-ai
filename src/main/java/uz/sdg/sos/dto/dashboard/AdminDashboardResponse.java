package uz.sdg.sos.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private LocalDateTime generatedAt;

    private MedicalEventStats medicalEvents;
    private LabEventStats labEvents;
    private ClinicStats clinics;
    private ApplicationStats clinicApplications;
    private DmedSyncStats dmedSync;
    private UserStats users;
    private List<SystemAlert> systemAlerts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalEventStats {
        private long total;
        private long today;
        private long thisWeek;
        private long thisMonth;
        private Map<String, Long> byStatus;
        private Map<String, Long> byUrgency;
        private List<DailyCount> last30Days;
        private List<ClinicEventCount> topClinics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClinicEventCount {
        private String clinicId;
        private String clinicName;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClinicStats {
        private long total;
        private long active;
        private long inactive;
        private Map<String, Long> byType;
        private long newThisMonth;
        private long expiringKeysIn30Days;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationStats {
        private long pending;
        private long approvedThisMonth;
        private long rejectedThisMonth;
        private long totalApproved;
        private long totalRejected;
        private double approvalRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DmedSyncStats {
        private long total;
        private long success;
        private long failed;
        private double successRate;
        private long failedToday;
        private long successToday;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private long total;
        private long admins;
        private long operators;
        private long doctors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemAlert {
        private String type;
        private String message;
        private String severity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabEventStats {
        private long total;
        private long today;
        private long normal;
        private long abnormal;
        private long critical;
    }
}
