package uz.sdg.sos.dto.clinicapplication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.sdg.sos.entity.enums.ApplicationType;

@Getter
@Setter
@NoArgsConstructor
public class ClinicApplicationRequest {
    private String firstName;
    private String lastName;
    private String clinicName;
    private String phoneNumber1;
    private String phoneNumber2;
    private String login;
    private String password;
    private ApplicationType applicationType;
}
