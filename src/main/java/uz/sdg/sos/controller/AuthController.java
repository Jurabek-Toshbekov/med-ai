package uz.sdg.sos.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.sdg.sos.service.AuthService;
import uz.sdg.sos.service.UserService;

@RestController
@RequestMapping("/sdg/uz")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;


    @PostMapping("login")
    public ResponseEntity<?> login(
            @RequestParam String login,
            @RequestParam String password) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(login, password));
    }


}
