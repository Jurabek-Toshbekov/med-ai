package uz.sdg.sos.dto.medicalevent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalEventIntakeRequest {
    private String jshshir;
    private String diagnosis;
    private String conclusion;
}
