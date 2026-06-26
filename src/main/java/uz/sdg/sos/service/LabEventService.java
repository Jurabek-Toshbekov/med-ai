package uz.sdg.sos.service;

import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.labevent.LabEventIntakeRequest;

public interface LabEventService {

    ApiResponse<?> processIntake(String authHeader, String secretKey, LabEventIntakeRequest request);
}
