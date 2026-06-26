package uz.sdg.sos.service.SupportClass;

import org.springframework.data.jpa.domain.Specification;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;

public class UserSpecification {

    public static Specification<UserEntity> hasPhoneNumber(String phoneNumber) {
        return (root, query, cb) ->
                phoneNumber != null && !phoneNumber.isEmpty()
                        ? cb.like(root.get("phoneNumber"), "%" + phoneNumber + "%")
                        : null;
    }

    public static Specification<UserEntity> hasFirstName(String firstName) {
        return (root, query, cb) ->
                firstName != null && !firstName.isEmpty()
                        ? cb.like(root.get("firstName"), "%" + firstName + "%")
                        : null;
    }

    public static Specification<UserEntity> hasLastName(String lastName) {
        return (root, query, cb) ->
                lastName != null && !lastName.isEmpty()
                        ? cb.like(root.get("lastName"), "%" + lastName + "%")
                        : null;
    }

    public static Specification<UserEntity> hasAccountType(AccountTypeEnum accountType) {
        return (root, query, cb) ->
                accountType != null
                        ? cb.equal(root.get("accountType"), accountType)
                        : null;
    }

    public static Specification<UserEntity> hasGenderType(String genderType) {
        return (root, query, cb) ->
                genderType != null && !genderType.isEmpty()
                        ? cb.equal(root.get("genderType"), genderType)
                        : null;
    }
}
