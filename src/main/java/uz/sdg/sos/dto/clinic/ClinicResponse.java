package uz.sdg.sos.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.entity.enums.ClinicType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicResponse {

    private UUID id;
    private String name;
    private String address;
    private String phoneNumber;
    private ClinicType clinicType;
    private ClinicStatus status;
    private String secretKey;
    private LocalDateTime secretKeyGeneratedAt;
    private LocalDateTime secretKeyExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ClinicResponse fromEntity(ClinicEntity entity) {
        return ClinicResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .phoneNumber(entity.getPhoneNumber())
                .clinicType(entity.getClinicType())
                .status(entity.getStatus())
                .secretKey(entity.getStatus() == ClinicStatus.ACTIVE ? entity.getSecretKey() : null)
                .secretKeyGeneratedAt(entity.getSecretKeyGeneratedAt())
                .secretKeyExpiresAt(entity.getSecretKeyExpiresAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
