package uz.sdg.sos.dto.clinicapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.sdg.sos.entity.ClinicApplicationEntity;
import uz.sdg.sos.entity.enums.ClinicApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicApplicationResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String clinicName;
    private String phoneNumber1;
    private String phoneNumber2;
    private String login;
    private ClinicApplicationStatus status;
    private LocalDateTime createdAt;

    // only populated after approval
    private String secretKey;
    private LocalDateTime secretKeyExpiresAt;

    public static ClinicApplicationResponse fromEntity(ClinicApplicationEntity e) {
        return ClinicApplicationResponse.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .clinicName(e.getClinicName())
                .phoneNumber1(e.getPhoneNumber1())
                .phoneNumber2(e.getPhoneNumber2())
                .login(e.getLogin())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
