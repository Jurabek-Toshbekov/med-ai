package uz.sdg.sos.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicCreateRequest {

    @NotBlank(message = "Klinika nomi bo'sh bo'lishi mumkin emas")
    private String name;

    private String address;

    private String phoneNumber;
}
