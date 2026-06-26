package uz.sdg.sos.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.UserDto;
import uz.sdg.sos.entity.enums.AccountTypeEnum;
import uz.sdg.sos.service.UserService;


@Api(tags = "User Management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @ApiOperation("Create a new user (ADMIN only)")
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto dto) {
        return ApiResponse.controller(userService.createByAdmin(dto));
    }

    @ApiOperation("Edit user by ID (ADMIN only)")
    @PutMapping("/{id}")
    public ResponseEntity<?> editUser(@PathVariable Long id, @RequestBody UserDto dto) {
        return ApiResponse.controller(userService.edit(id, dto));
    }

    @ApiOperation("Get one user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getOneUser(@PathVariable Long id) {
        return ApiResponse.controller(userService.getOne(id));
    }

    @ApiOperation("Get all users with filters")
    @GetMapping
    public ResponseEntity<?> getAllUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) AccountTypeEnum accountType,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String genderType) {
        return ApiResponse.controller(userService.getAll(
                page, size, accountType, phoneNumber, lastName, firstName, genderType
        ));
    }

    @ApiOperation("Delete user by ID (ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ApiResponse.controller(userService.delete(id));
    }

}
