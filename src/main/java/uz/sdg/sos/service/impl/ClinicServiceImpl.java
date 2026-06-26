package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.clinic.ClinicCreateRequest;
import uz.sdg.sos.dto.clinic.ClinicResponse;
import uz.sdg.sos.dto.clinic.ClinicSecretKeyResponse;
import uz.sdg.sos.dto.clinic.ClinicUpdateRequest;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.repository.ClinicRepository;
import uz.sdg.sos.service.ClinicService;
import uz.sdg.sos.utils.ResMessages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;

    @Transactional
    @Override
    public ApiResponse<?> create(ClinicCreateRequest request) {
        try {
            if (request.getName() == null || request.getName().isBlank()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }
            ClinicEntity clinic = ClinicEntity.builder()
                    .name(request.getName())
                    .address(request.getAddress())
                    .phoneNumber(request.getPhoneNumber())
                    .clinicType(request.getClinicType())
                    .status(ClinicStatus.INACTIVE)
                    .build();
            clinicRepository.save(clinic);
            return new ApiResponse<>(true, ResMessages.SUCCESS, ClinicResponse.fromEntity(clinic));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR + " " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getById(UUID id) {
        try {
            Optional<ClinicEntity> optional = clinicRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_NOT_FOUND);
            }
            return new ApiResponse<>(true, ResMessages.SUCCESS, ClinicResponse.fromEntity(optional.get()));
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> getAll() {
        try {
            List<ClinicResponse> list = clinicRepository.findAll()
                    .stream()
                    .map(ClinicResponse::fromEntity)
                    .collect(Collectors.toList());
            return new ApiResponse<>(true, ResMessages.SUCCESS, list);
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> getAllByStatus(ClinicStatus status) {
        try {
            List<ClinicResponse> list = clinicRepository.findAllByStatus(status)
                    .stream()
                    .map(ClinicResponse::fromEntity)
                    .collect(Collectors.toList());
            return new ApiResponse<>(true, ResMessages.SUCCESS, list);
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Transactional
    @Override
    public ApiResponse<?> update(UUID id, ClinicUpdateRequest request) {
        try {
            Optional<ClinicEntity> optional = clinicRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_NOT_FOUND);
            }
            ClinicEntity clinic = optional.get();
            if (request.getName() != null && !request.getName().isBlank()) {
                clinic.setName(request.getName());
            }
            if (request.getAddress() != null) {
                clinic.setAddress(request.getAddress());
            }
            if (request.getPhoneNumber() != null) {
                clinic.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getClinicType() != null) {
                clinic.setClinicType(request.getClinicType());
            }
            clinicRepository.save(clinic);
            return new ApiResponse<>(true, ResMessages.SUCCESS, ClinicResponse.fromEntity(clinic));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR + " " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public ApiResponse<?> changeStatus(UUID id, ClinicStatus newStatus) {
        try {
            Optional<ClinicEntity> optional = clinicRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_NOT_FOUND);
            }
            ClinicEntity clinic = optional.get();

            if (newStatus == ClinicStatus.ACTIVE && clinic.getSecretKey() == null) {
                String key = UUID.randomUUID().toString().replace("-", "");
                clinic.setSecretKey(key);
                clinic.setSecretKeyGeneratedAt(LocalDateTime.now());
                clinic.setSecretKeyExpiresAt(LocalDateTime.now().plusDays(30));
            }

            clinic.setStatus(newStatus);
            clinicRepository.save(clinic);
            return new ApiResponse<>(true, ResMessages.SUCCESS, ClinicResponse.fromEntity(clinic));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR + " " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public ApiResponse<?> delete(UUID id) {
        try {
            Optional<ClinicEntity> optional = clinicRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, ResMessages.OBJECT_NOT_FOUND);
            }
            clinicRepository.delete(optional.get());
            return new ApiResponse<>(true, ResMessages.DELETE);
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> validateSecretKey(String secretKey) {
        try {
            if (secretKey == null || secretKey.isBlank()) {
                return new ApiResponse<>(true, ResMessages.SUCCESS,
                        ClinicSecretKeyResponse.builder()
                                .valid(false)
                                .reason("KEY_NOT_FOUND")
                                .build());
            }

            Optional<ClinicEntity> optional = clinicRepository.findBySecretKey(secretKey);
            if (optional.isEmpty()) {
                return new ApiResponse<>(true, ResMessages.SUCCESS,
                        ClinicSecretKeyResponse.builder()
                                .valid(false)
                                .reason("KEY_NOT_FOUND")
                                .build());
            }

            ClinicEntity clinic = optional.get();

            if (clinic.getStatus() == ClinicStatus.INACTIVE) {
                return new ApiResponse<>(true, ResMessages.SUCCESS,
                        ClinicSecretKeyResponse.builder()
                                .clinicId(clinic.getId())
                                .clinicName(clinic.getName())
                                .valid(false)
                                .reason("CLINIC_INACTIVE")
                                .build());
            }

            if (LocalDateTime.now().isAfter(clinic.getSecretKeyExpiresAt())) {
                return new ApiResponse<>(true, ResMessages.SUCCESS,
                        ClinicSecretKeyResponse.builder()
                                .clinicId(clinic.getId())
                                .clinicName(clinic.getName())
                                .valid(false)
                                .reason("KEY_EXPIRED")
                                .build());
            }

            return new ApiResponse<>(true, ResMessages.SUCCESS,
                    ClinicSecretKeyResponse.builder()
                            .clinicId(clinic.getId())
                            .clinicName(clinic.getName())
                            .valid(true)
                            .build());
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }
}
