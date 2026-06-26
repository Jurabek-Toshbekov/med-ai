package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.UserDto;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.repository.UserRepository;
import uz.sdg.sos.security.JwtService;
import uz.sdg.sos.service.SupportClass.UserSpecification;
import uz.sdg.sos.service.UserService;
import uz.sdg.sos.utils.ResMessages;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ApiResponse<?> createByAdmin(UserDto dto) {
        try {
            if (dto.getFirstName() == null || dto.getLastName() == null ||
                    dto.getPhoneNumber() == null || dto.getPassword() == null) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }

            UserEntity caller = getCurrentUser();
            if (caller == null) return new ApiResponse<>(false, "UNAUTHORIZED");
            if (!caller.getAccountType().equals(AccountTypeEnum.ADMIN)) {
                return new ApiResponse<>(false, "Faqat ADMIN bu amalni bajarishi mumkin");
            }

            if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                return new ApiResponse<>(false, ResMessages.EXISTENT_PHONE_NUM);
            }

            if (!isValidPassword(dto.getPassword())) {
                return new ApiResponse<>(false, "Parol 5ta belgidan ko'p va harf + raqamdan iborat bo'lishi kerak");
            }

            UserEntity entity = UserDto.toEntity(dto, new UserEntity());
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(entity);
            return new ApiResponse<>(true, ResMessages.SUCCESS, entity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> edit(Long id, UserDto dto) {
        try {
            if (id == null) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }

            UserEntity caller = getCurrentUser();
            if (caller == null) return new ApiResponse<>(false, "UNAUTHORIZED");
            if (!caller.getAccountType().equals(AccountTypeEnum.ADMIN)) {
                return new ApiResponse<>(false, "Faqat ADMIN bu amalni bajarishi mumkin");
            }

            Optional<UserEntity> optional = userRepository.findById(id);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, "Foydalanuvchi topilmadi");
            }

            UserEntity entity = UserDto.toEntity(dto, optional.get());
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                if (!isValidPassword(dto.getPassword())) {
                    return new ApiResponse<>(false, "Parol 5ta belgidan ko'p va harf + raqamdan iborat bo'lishi kerak");
                }
                entity.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            userRepository.save(entity);
            return new ApiResponse<>(true, ResMessages.SUCCESS, entity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> getOne(Long userId) {
        try {
            return new ApiResponse<>(true, ResMessages.SUCCESS, findBy(userId));
        } catch (Throwable e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> getAll(int page, int size, AccountTypeEnum accountType,
                                 String phone, String lastName, String firstName, String genderType) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            Specification<UserEntity> spec = Specification.where(UserSpecification.hasPhoneNumber(phone))
                    .and(UserSpecification.hasFirstName(firstName))
                    .and(UserSpecification.hasLastName(lastName))
                    .and(UserSpecification.hasAccountType(accountType))
                    .and(UserSpecification.hasGenderType(genderType));

            Page<UserEntity> users = userRepository.findAll(spec, pageable);
            return new ApiResponse<>(true, ResMessages.SUCCESS, users);
        } catch (Throwable e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<?> delete(Long userId) {
        try {
            if (userId == null) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }

            UserEntity caller = getCurrentUser();
            if (caller == null) return new ApiResponse<>(false, "UNAUTHORIZED");
            if (!caller.getAccountType().equals(AccountTypeEnum.ADMIN)) {
                return new ApiResponse<>(false, "Faqat ADMIN bu amalni bajarishi mumkin");
            }

            Optional<UserEntity> optional = userRepository.findById(userId);
            if (optional.isEmpty()) {
                return new ApiResponse<>(false, "Foydalanuvchi topilmadi");
            }
            userRepository.delete(optional.get());
            return new ApiResponse<>(true, ResMessages.DELETE);
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }

    @Override
    public UserEntity getById(Long userId) {
        try {
            if (userId == null) return null;
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public UserEntity findBy(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByPhoneNumber(auth.getName()).orElse(null);
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() <= 5) return false;
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
            if (hasLetter && hasDigit) break;
        }
        return hasLetter && hasDigit;
    }
}
