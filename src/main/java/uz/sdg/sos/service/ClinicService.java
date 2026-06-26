package uz.sdg.sos.service;

import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.clinic.ClinicCreateRequest;
import uz.sdg.sos.dto.clinic.ClinicUpdateRequest;
import uz.sdg.sos.entity.enums.ClinicStatus;

import java.util.UUID;

public interface ClinicService {

    ApiResponse<?> create(ClinicCreateRequest request);

    ApiResponse<?> getById(UUID id);

    ApiResponse<?> getAll();

    ApiResponse<?> getAllByStatus(ClinicStatus status);

    ApiResponse<?> update(UUID id, ClinicUpdateRequest request);

    ApiResponse<?> changeStatus(UUID id, ClinicStatus newStatus);

    ApiResponse<?> delete(UUID id);

    ApiResponse<?> validateSecretKey(String secretKey);
}
