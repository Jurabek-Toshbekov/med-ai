package uz.sdg.sos.dto.dmed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmedWriteRequest {
    private String jshshir;
    private UUID medicalEventId;
    private UUID clinicId;
    private String clinicName;
    private String diagnosis;
    private String conclusion;
    private String eventDate;
    private String source;
}
