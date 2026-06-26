package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.clinicapplication.ClinicApplicationRequest;
import uz.sdg.sos.dto.clinicapplication.ClinicApplicationResponse;
import uz.sdg.sos.entity.ClinicApplicationEntity;
import uz.sdg.sos.entity.ClinicEntity;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.entity.enums.ClinicApplicationStatus;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.repository.ClinicApplicationRepository;
import uz.sdg.sos.repository.ClinicRepository;
import uz.sdg.sos.repository.UserRepository;
import uz.sdg.sos.service.ClinicApplicationService;
import uz.sdg.sos.utils.ResMessages;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicApplicationServiceImpl implements ClinicApplicationService {

    private final ClinicApplicationRepository applicationRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<?> apply(ClinicApplicationRequest request) {
        try {
            if (request.getFirstName() == null || request.getLastName() == null ||
                    request.getClinicName() == null || request.getPhoneNumber1() == null ||
                    request.getLogin() == null || request.getPassword() == null) {
                return new ApiResponse<>(false, "Barcha majburiy maydonlarni to'ldiring");
            }

            if (applicationRepository.existsByLogin(request.getLogin())) {
                return new ApiResponse<>(false, "Bu login allaqachon ariza berishda ishlatilgan");
            }
            if (userRepository.existsByPhoneNumber(request.getLogin())) {
                return new ApiResponse<>(false, "Bu login allaqachon ro'yxatdan o'tgan");
            }

            ClinicApplicationEntity application = ClinicApplicationEntity.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .clinicName(request.getClinicName())
                    .phoneNumber1(request.getPhoneNumber1())
                    .phoneNumber2(request.getPhoneNumber2())
                    .login(request.getLogin())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .status(ClinicApplicationStatus.PENDING)
                    .build();

            applicationRepository.save(application);
            log.info("Yangi klinika arizasi: {} - {}", request.getClinicName(), request.getLogin());
            return new ApiResponse<>(true, "Arizangiz qabul qilindi. Admin ko'rib chiqadi.",
                    ClinicApplicationResponse.fromEntity(application));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> getAll(ClinicApplicationStatus status) {
        try {
            List<ClinicApplicationResponse> list;
            if (status != null) {
                list = applicationRepository.findAllByStatusOrderByCreatedAtDesc(status)
                        .stream()
                        .map(ClinicApplicationResponse::fromEntity)
                        .collect(Collectors.toList());
            } else {
                list = applicationRepository.findAll()
                        .stream()
                        .map(ClinicApplicationResponse::fromEntity)
                        .collect(Collectors.toList());
            }
            return new ApiResponse<>(true, ResMessages.SUCCESS, list);
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Transactional
    @Override
    public ApiResponse<?> approve(UUID id) {
        try {
            Optional<ClinicApplicationEntity> optional = applicationRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, "Ariza topilmadi");
            }
            ClinicApplicationEntity application = optional.get();
            if (application.getStatus() != ClinicApplicationStatus.PENDING) {
                return new ApiResponse<>(false, "Bu ariza allaqachon ko'rib chiqilgan");
            }

            // 1. Klinikani yaratish
            String secretKey = UUID.randomUUID().toString().replace("-", "");
            LocalDateTime now = LocalDateTime.now();
            ClinicEntity clinic = ClinicEntity.builder()
                    .name(application.getClinicName())
                    .phoneNumber(application.getPhoneNumber1())
                    .status(ClinicStatus.ACTIVE)
                    .secretKey(secretKey)
                    .secretKeyGeneratedAt(now)
                    .secretKeyExpiresAt(now.plusYears(1))
                    .build();
            clinicRepository.save(clinic);

            // 2. Klinika uchun UserEntity yaratish (OPERATOR)
            UserEntity user = new UserEntity();
            user.setFirstName(application.getFirstName());
            user.setLastName(application.getLastName());
            user.setPhoneNumber(application.getLogin());
            user.setPassword(application.getPassword()); // already encoded
            user.setAccountType(AccountTypeEnum.OPERATOR);
            userRepository.save(user);

            // 3. Ariza statusini yangilash
            application.setStatus(ClinicApplicationStatus.APPROVED);
            applicationRepository.save(application);

            ClinicApplicationResponse response = ClinicApplicationResponse.fromEntity(application);
            response.setSecretKey(secretKey);
            response.setSecretKeyExpiresAt(clinic.getSecretKeyExpiresAt());

            log.info("Klinika arizasi tasdiqlandi: {} → clinicId={}, userId={}",
                    application.getClinicName(), clinic.getId(), user.getId());

            return new ApiResponse<>(true, "Klinika muvaffaqiyatli tasdiqlandi", response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR + " " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public ApiResponse<?> reject(UUID id) {
        try {
            Optional<ClinicApplicationEntity> optional = applicationRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, "Ariza topilmadi");
            }
            ClinicApplicationEntity application = optional.get();
            if (application.getStatus() != ClinicApplicationStatus.PENDING) {
                return new ApiResponse<>(false, "Bu ariza allaqachon ko'rib chiqilgan");
            }
            application.setStatus(ClinicApplicationStatus.REJECTED);
            applicationRepository.save(application);
            log.info("Klinika arizasi rad etildi: {}", application.getClinicName());
            return new ApiResponse<>(true, "Ariza rad etildi");
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }
}
