package uz.sdg.sos.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.sdg.sos.entity.enums.ClinicType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicUpdateRequest {

    private String name;

    private String address;

    private String phoneNumber;

    private ClinicType clinicType;
}
