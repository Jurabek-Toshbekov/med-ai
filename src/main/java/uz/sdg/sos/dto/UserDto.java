package uz.sdg.sos.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.sdg.sos.base.BaseDto;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.entity.enums.GenderType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
public class UserDto extends BaseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;
    private GenderType genderType;
    private LocalDate dateBirth;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private AccountTypeEnum accountType;


    public static UserEntity toEntity(UserDto dto, UserEntity entity) {
        entity.setId(dto.getId() != null ? dto.getId() : entity.getId());
        entity.setFirstName(dto.getFirstName() != null ? dto.getFirstName() : entity.getFirstName());
        entity.setLastName(dto.getLastName() != null ? dto.getLastName() : entity.getLastName());
        entity.setPhoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber() : entity.getPhoneNumber());
        entity.setGenderType(dto.getGenderType() != null ? dto.getGenderType() : entity.getGenderType());
        entity.setDateBirth(dto.getDateBirth() != null ? dto.getDateBirth() : entity.getDateBirth());
        entity.setEmail(dto.getEmail() != null ? dto.getEmail() : entity.getEmail());
        entity.setAddress(dto.getAddress() != null ? dto.getAddress() : entity.getAddress());
        entity.setAccountType(dto.getAccountType() != null ? dto.getAccountType() : entity.getAccountType());
        return entity;
    }


}
