package uz.sdg.sos.dto.medicalevent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalEventIntakeResponse {
    private UUID eventId;
    private String patientName;
    private String familyDoctorPhone;
    private Integer hoursUntilContact;
    private LocalDateTime notifyAt;
    private String status;
}
