package uz.sdg.sos.dto.intake;

import lombok.Data;

@Data
public class UnifiedIntakeRequest {

    // ── Umumiy (har ikki turdagi tashkilot uchun) ──────────────────────────
    private String jshshir;
    private String patientFullName;
    private String familyDoctorId;
    private String familyDoctorPhone;
    private String familyDoctorFcmToken;

    // ── Klinika uchun (DAVLAT / XUSUSIY / TEZYOR_103) ─────────────────────
    private String diagnosis;
    private String conclusion;

    // ── Laboratoriya uchun ─────────────────────────────────────────────────
    private String testName;
    private String testResult;
    private String unit;
    private String referenceRange;
}
