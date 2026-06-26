package uz.sdg.sos.service;

import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.clinicapplication.ClinicApplicationRequest;
import uz.sdg.sos.entity.enums.ClinicApplicationStatus;

import java.util.UUID;

public interface ClinicApplicationService {

    ApiResponse<?> apply(ClinicApplicationRequest request);

    ApiResponse<?> getAll(ClinicApplicationStatus status);

    ApiResponse<?> approve(UUID id);

    ApiResponse<?> reject(UUID id);
}
