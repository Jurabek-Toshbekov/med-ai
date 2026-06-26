package uz.sdg.sos.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.sdg.sos.entity.enums.ClinicType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicCreateRequest {

    @NotBlank(message = "Klinika nomi bo'sh bo'lishi mumkin emas")
    private String name;

    private String address;

    private String phoneNumber;

    @NotNull(message = "Klinika turi ko'rsatilishi shart")
    private ClinicType clinicType;
}
