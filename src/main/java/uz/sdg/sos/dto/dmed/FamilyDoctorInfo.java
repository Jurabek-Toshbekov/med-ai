package uz.sdg.sos.dto.dmed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDoctorInfo {
    private String id;
    private String fullName;
    private String phoneNumber;
    private String polyclinicName;
    private String workingHours;

    // Mobile push notification
    private String fcmToken;
    private String devicePlatform;   // ANDROID | IOS

    // Extra
    private String email;
    private String specialty;
    private String district;
    private String language;         // UZ | RU
}
