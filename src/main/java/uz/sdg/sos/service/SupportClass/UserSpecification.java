package uz.sdg.sos.service.SupportClass;

import org.springframework.data.jpa.domain.Specification;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;

public class UserSpecification {

    public static Specification<UserEntity> hasPhoneNumber(String phoneNumber) {
        return (root, query, criteriaBuilder) ->
                phoneNumber != null && !phoneNumber.isEmpty()
                        ? criteriaBuilder.like(root.get("phoneNumber"), "%" + phoneNumber + "%")
                        : null;
    }

    public static Specification<UserEntity> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) ->
                firstName != null && !firstName.isEmpty()
                        ? criteriaBuilder.like(root.get("firstName"), "%" + firstName + "%")
                        : null;
    }

    public static Specification<UserEntity> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) ->
                lastName != null && !lastName.isEmpty()
                        ? criteriaBuilder.like(root.get("lastName"), "%" + lastName + "%")
                        : null;
    }

    public static Specification<UserEntity> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                email != null && !email.isEmpty()
                        ? criteriaBuilder.like(root.get("email"), "%" + email + "%")
                        : null;
    }

    public static Specification<UserEntity> hasSchool(String school) {
        return (root, query, criteriaBuilder) ->
                school != null && !school.isEmpty()
                        ? criteriaBuilder.like(root.get("school"), "%" + school + "%")
                        : null;
    }

    public static Specification<UserEntity> belongsToGroup(String group) {
        return (root, query, criteriaBuilder) ->
                group != null && !group.isEmpty()
                        ? criteriaBuilder.like(root.get("group"), "%" + group + "%")
                        : null;
    }

    public static Specification<UserEntity> hasAccountType(AccountTypeEnum accountType) {
        return (root, query, criteriaBuilder) ->
                accountType != null
                        ? criteriaBuilder.equal(root.get("accountType"), accountType)
                        : null;
    }

    public static Specification<UserEntity> hasGenderType(String genderType) {
        return (root, query, criteriaBuilder) ->
                genderType != null && !genderType.isEmpty()
                        ? criteriaBuilder.equal(root.get("genderType"), genderType)
                        : null;
    }
}

