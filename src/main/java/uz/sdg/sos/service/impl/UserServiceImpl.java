package uz.sdg.sos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
            if (dto.getFirstName() == null || dto.getLastName() == null || dto.getPhoneNumber() == null ||
                    dto.getPassword() == null) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }

            // tel raqam mavjudligini tekshirish
            if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                return new ApiResponse<>(false, ResMessages.EXISTENT_PHONE_NUM);
            }


            if (isValidPassword(dto.getPassword())) {
                return new ApiResponse<>(false, "Parol 5ta belgidan ko'p bo'lishi va raqam harfdan iborat bo'lishi kerak  ");
            }

            // userni rolini tekshirish
            Optional<UserEntity> optionalUser = userRepository.findById(dto.getCreatorId());
            if (optionalUser.isEmpty()) {
                return new ApiResponse<>(false, "Yaratuvchi ID si xato");
            }

            if (!optionalUser.get().getAccountType().equals(AccountTypeEnum.ADMIN)) {
                return new ApiResponse<>(false, "Yaratuvchi ADMIN bo'lishi kerak ");
            }


            UserEntity entity = UserDto.toEntity(dto, new UserEntity());
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(entity);
            return new ApiResponse<>(true, ResMessages.SUCCESS, entity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR + "<br> " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<?> edit(UserDto dto) {
        try {
            if (dto.getId() == null || dto.getPhoneNumber() == null) {
                return new ApiResponse<>(false, ResMessages.OBJECT_IS_NULL);
            }

            // userni rolini tekshirish
            Optional<UserEntity> optionalUser = userRepository.findById(dto.getCreatorId());
            if (optionalUser.isEmpty()) {
                return new ApiResponse<>(false, "Yaratuvchi ID si xato");
            }

            if (!optionalUser.get().getAccountType().equals(AccountTypeEnum.ADMIN)) {
                return new ApiResponse<>(false, "Yaratuvchi ADMIN bo'lishi kerak ");
            }

            Optional<UserEntity> byPhoneNumber = userRepository.findByPhoneNumber(dto.getPhoneNumber());
            if (byPhoneNumber.isEmpty()) {
                return new ApiResponse<>(false, "User topilmadi");
            }

            UserEntity entity = UserDto.toEntity(dto, byPhoneNumber.get());
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
            userRepository.save(entity);
            return new ApiResponse<>(true, ResMessages.SUCCESS, entity);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR + "<br> " + e.getMessage());
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
    public ApiResponse<?> getAll(int page, int size, AccountTypeEnum accountType, String phone, String lastName, String firstName,
                                 String email, String school, String group, String genderType) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            Specification<UserEntity> spec = Specification.where(UserSpecification.hasPhoneNumber(phone))
                    .and(UserSpecification.hasFirstName(firstName))
                    .and(UserSpecification.hasLastName(lastName))
                    .and(UserSpecification.hasEmail(email))
                    .and(UserSpecification.hasSchool(school))
                    .and(UserSpecification.belongsToGroup(group))
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
            Optional<UserEntity> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                return new ApiResponse<>(false, "ID xato ");
            }
            if (isSpecialUser(optionalUser.get().getPhoneNumber())) {
                return new ApiResponse<>(false, "BU userni o'chirish mumkin emas , bu mahsus user ");
            }
            userRepository.delete(optionalUser.get());
            return new ApiResponse<>(true, ResMessages.DELETE);
        } catch (Exception e) {
            return new ApiResponse<>(false, ResMessages.SERVER_ERROR);
        }
    }


    @Override
    public UserEntity getById(Long userId) {
        try {
            if (userId == null) {
                return null;
            }
            Optional<UserEntity> userEntityOptional = userRepository.findById(userId);
            if (userEntityOptional.isEmpty()) {
                return null;
            }
            return userEntityOptional.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public UserEntity findBy(Long userId) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return userOptional.get();
    }

    public static boolean isSpecialUser(String phoneNumber) {
        for (String file : specialUserPhoneNumberList) {
            if (phoneNumber.equals(file)) {
                return true;
            }
        }
        return false;
    }

    private static final String[] specialUserPhoneNumberList = {
            "+998994059000",
            "+998991234568"
    };

    private boolean isValidPassword(String password) {
        if (password == null || password.length() <= 5) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            // Agar ikkalasi ham topilsa, loopni to'xtatamiz
            if (hasLetter && hasDigit) {
                break;
            }
        }

        return hasLetter && hasDigit;
    }

}
