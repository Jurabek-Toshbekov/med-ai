package uz.sdg.sos.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import uz.sdg.sos.base.BaseEntity;
import uz.sdg.sos.dto.makeObjects.FCMTokenMake;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.entity.enums.GenderType;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
//@ToString(onlyExplicitlyIncluded = true)
@Entity(name = "users")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Table(indexes = {
        @Index(name = "idx_phone_number", columnList = "username")
})
public class UserEntity extends BaseEntity implements UserDetails {
    @Column (nullable = false)
    private String firstName;
    @Column (nullable = false)
    private String lastName;
    @Column(nullable = false, unique = true, name = "username")
    private String phoneNumber;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private GenderType genderType;
    private LocalDate dateBirth;
    private String email;
    private String address;

    @Enumerated(EnumType.STRING)
    private AccountTypeEnum accountType;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(accountType.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    public UserEntity(String phoneNumber, String password, AccountTypeEnum accountType) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.accountType = accountType;
    }

    public UserEntity(String firstName, String lastName, LocalDate dateBirth, String phoneNumber, AccountTypeEnum accountType ,String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateBirth = dateBirth;
        this.phoneNumber = phoneNumber;
        this.accountType = accountType;
        this.password = password;
    }



}
