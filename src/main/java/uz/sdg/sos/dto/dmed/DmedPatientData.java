package uz.sdg.sos.dto.dmed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmedPatientData {
    private String jshshir;
    private String fullName;
    private String birthDate;
    private String gender;
    private String address;
    private List<String> chronicDiseases;
    private List<String> currentMedications;
    private String bloodType;
    private List<String> allergies;
    private String lastHospitalization;
    private FamilyDoctorInfo familyDoctor;
}
