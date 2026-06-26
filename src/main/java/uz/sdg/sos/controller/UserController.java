package uz.sdg.sos.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.UserDto;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.service.UserService;


@RestController
@RequestMapping("/sdg/uz")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/admin")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody UserDto dto) {
        return ApiResponse.controller(userService.createByAdmin(dto));
    }

    @PutMapping("/edit")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN','BUSINESSMAN','CLIENT')")
    public ResponseEntity<?> editUser(@RequestBody UserDto dto) {
        return ApiResponse.controller(userService.edit(dto));
    }


    @GetMapping("/{id}")
//    @PreAuthorize(value = "hasAnyAuthority('ADMIN','CLIENT','STUDENT')")
    public ResponseEntity<?> getOneUser(@PathVariable Long id) {
        return ApiResponse.controller(userService.getOne(id));
    }

    @GetMapping
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getAllUser(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "30") int size,
                                        @RequestParam AccountTypeEnum accountType,
                                        @RequestParam(required = false) String phoneNumber,
                                        @RequestParam(required = false) String lastName,
                                        @RequestParam(required = false) String firstName,
                                        @RequestParam(required = false) String email,
                                        @RequestParam(required = false) String school,
                                        @RequestParam(required = false) String group,
                                        @RequestParam(required = false) String genderType) {
        return ApiResponse.controller(userService.getAll(
                page, size, accountType, phoneNumber, lastName, firstName, email, school, group, genderType
        ));
    }


    @DeleteMapping("/{id}")
//    @PreAuthorize(value = "hasAnyAuthority('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ApiResponse.controller(userService.delete(id));
    }


}
