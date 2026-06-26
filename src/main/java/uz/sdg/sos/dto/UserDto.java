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


//@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor

public class UserDto extends BaseDto {
    private Long id;
    private Long creatorId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;
    private GenderType genderType;
    private LocalDate dateBirth;
    private String email;
    private String address;
    private String addressRegion;
    private String addressDistrict;
    private String addressMFY;
    private String school;
    private String group;

    private Long courseId;

    @Enumerated(EnumType.STRING)
    private AccountTypeEnum accountType;


    public static UserEntity toEntity(UserDto dto, UserEntity entity) {
        entity.setId(dto.getId() != null ? dto.getId() : entity.getId());
        entity.setFirstName(dto.getFirstName() != null ? dto.getFirstName() : entity.getFirstName());
        entity.setLastName(dto.getLastName() != null ? dto.getLastName() : entity.getLastName());
        entity.setPhoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber() : entity.getPhoneNumber());
        entity.setPassword(dto.getPassword() != null ? dto.getPassword() : entity.getPassword());
        entity.setPassword02(dto.getPassword() != null ? dto.getPassword() : entity.getPassword());
        entity.setGenderType(dto.getGenderType() != null ? dto.getGenderType() : entity.getGenderType());
        entity.setDateBirth(dto.getDateBirth() != null ? dto.getDateBirth() : entity.getDateBirth());
        entity.setEmail(dto.getEmail() != null ? dto.getEmail() : entity.getEmail());
        entity.setAddress(dto.getAddress() != null ? dto.getAddress() : entity.getAddress());
        entity.setAddressRegion(dto.getAddressRegion() != null ? dto.getAddressRegion() : entity.getAddressRegion());
        entity.setAddressDistrict(dto.getAddressDistrict() != null ? dto.getAddressDistrict() : entity.getAddressDistrict());
        entity.setAddressMFY(dto.getAddressMFY() != null ? dto.getAddressMFY() : entity.getAddressMFY());
        entity.setSchool(dto.getSchool() != null ? dto.getSchool() : entity.getSchool());
        entity.setGroup(dto.getGroup() != null ? dto.getGroup() : entity.getGroup());
        entity.setAccountType(dto.getAccountType() != null ? dto.getAccountType() : entity.getAccountType());

        entity.setCourseId(dto.getCourseId() != null ? dto.getCourseId() : entity.getCourseId());
        return entity;
    }



}
