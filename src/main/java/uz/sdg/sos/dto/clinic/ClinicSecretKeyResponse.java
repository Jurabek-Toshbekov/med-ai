package uz.sdg.sos.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicSecretKeyResponse {

    private UUID clinicId;
    private String clinicName;
    private boolean valid;
    private String reason;
}
