package uz.sdg.sos.dto.labevent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LabEventIntakeRequest {
    private String jshshir;
    private String patientFullName;
    private String testName;
    private String testResult;
    private String unit;
    private String referenceRange;
    private String familyDoctorId;
    private String familyDoctorPhone;
    private String familyDoctorFcmToken;
}
