package uz.sdg.sos.dto.makeObjects;

import lombok.*;
import uz.sdg.sos.entity.enums.AccountTypeEnum;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "phoneNumber")
public class UserDtoMakeForChat {
  private Long id;
  private String firstName;
  private String lastName;
  private LocalDate dateBirth;
  private String phoneNumber;
  private AccountTypeEnum accountType;

}
