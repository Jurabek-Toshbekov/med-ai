package uz.sdg.sos.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.sdg.sos.entity.UserEntity;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.repository.UserRepository;
import uz.sdg.sos.security.JwtService;
import uz.sdg.sos.service.AuthService;
import uz.sdg.sos.service.UserService;
import uz.sdg.sos.utils.ResMessages;

import java.util.HashMap;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public HashMap<String, Object> login(String login, String password) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("object", null);
        response.put("code", null);
        response.put("status", false);
        response.put("message", null);

        if (login == null || password == null) {
            response.put("code", 404);
            response.put("message", ResMessages.OBJECT_IS_NULL);
            return response;
        }

        Optional<UserEntity> userEntityOptional = userRepository.findByPhoneNumber(login);

        if (userEntityOptional.isEmpty()) {
            response.put("code", 404);
            response.put("message", "Telefon raqam xato");
            return response;
        }

        if (!passwordEncoder.matches(password, userEntityOptional.get().getPassword())) {
            response.put("code", 404);
            response.put("message", "Parol xato");
            return response;
        }


        response.put("object", userEntityOptional.get());
        response.put("code", 200);
        response.put("status", true);
        response.put("message", "Yaxshi");
        response.put("token", jwtService.generateToken(userEntityOptional.get()));
        return response;
    }


}
