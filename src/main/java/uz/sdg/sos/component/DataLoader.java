package uz.sdg.sos.component;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.entity.enums.GenderType;
import uz.sdg.sos.repository.UserRepository;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;


    public DataLoader(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        long count = userRepository.count();

        if (count == 0) {
            UserEntity admin = new UserEntity();
            admin.setFirstName("Jurabek");
            admin.setLastName("Toshbekov");
            admin.setPassword(passwordEncoder.encode("1+1=11"));
            admin.setPhoneNumber("+998994059000");
            admin.setPassword02("1+1=11");
            admin.setAccountType(AccountTypeEnum.ADMIN);
            admin.setGenderType(GenderType.ERKAK);
            admin.setDateBirth(LocalDate.of(1997, 11, 29));
            admin.setEmail("jurabek@gmail.com");
            userRepository.save(admin);
            System.out.println("save data");
        }
    }


}