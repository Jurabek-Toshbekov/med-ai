package uz.sdg.sos.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.sdg.sos.entity.enums.ClinicStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicStatusRequest {

    private ClinicStatus status;
}
