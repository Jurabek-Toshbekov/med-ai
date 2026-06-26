package uz.sdg.sos.service;

import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.medicalevent.MedicalEventIntakeRequest;

public interface MedicalEventService {
    ApiResponse<?> processIntake(String secretKey, MedicalEventIntakeRequest request);
}
