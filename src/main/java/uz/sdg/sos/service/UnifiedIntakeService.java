package uz.sdg.sos.service;

import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.intake.UnifiedIntakeRequest;

public interface UnifiedIntakeService {
    ApiResponse<?> processIntake(String authHeader, String secretKey, UnifiedIntakeRequest request);
}
