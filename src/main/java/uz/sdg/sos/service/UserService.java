package uz.sdg.sos.service;

import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.UserDto;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;

public interface UserService {

    ApiResponse<?> createByAdmin(UserDto dto);
    ApiResponse<?> edit(Long id, UserDto dto);
    ApiResponse<?> getOne(Long userId);
    ApiResponse<?> getAll(int page, int size, AccountTypeEnum accountType, String phone, String lastName, String firstName, String genderType);
    ApiResponse<?> delete(Long userId);
    UserEntity getById(Long userId);

}
