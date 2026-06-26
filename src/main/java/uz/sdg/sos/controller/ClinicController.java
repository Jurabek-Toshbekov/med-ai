package uz.sdg.sos.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.clinic.ClinicCreateRequest;
import uz.sdg.sos.dto.clinic.ClinicStatusRequest;
import uz.sdg.sos.dto.clinic.ClinicUpdateRequest;
import uz.sdg.sos.entity.enums.ClinicStatus;
import uz.sdg.sos.service.ClinicService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clinics")
@RequiredArgsConstructor
@Api(tags = "Clinic Management")
public class ClinicController {

    private final ClinicService clinicService;

    @PostMapping
    @ApiOperation(value = "Create a new clinic")
    public ResponseEntity<?> create(@Valid @RequestBody ClinicCreateRequest request) {
        return ApiResponse.controller(clinicService.create(request));
    }

    @GetMapping
    @ApiOperation(value = "Get all clinics")
    public ResponseEntity<?> getAll() {
        return ApiResponse.controller(clinicService.getAll());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Get clinic by ID")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return ApiResponse.controller(clinicService.getById(id));
    }

    @GetMapping("/status/{status}")
    @ApiOperation(value = "Get all clinics by status")
    public ResponseEntity<?> getAllByStatus(@PathVariable ClinicStatus status) {
        return ApiResponse.controller(clinicService.getAllByStatus(status));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update clinic info")
    public ResponseEntity<?> update(@PathVariable UUID id,
                                    @RequestBody ClinicUpdateRequest request) {
        return ApiResponse.controller(clinicService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @ApiOperation(value = "Change clinic status (ACTIVE / INACTIVE). First ACTIVE generates secret key.")
    public ResponseEntity<?> changeStatus(@PathVariable UUID id,
                                          @RequestBody ClinicStatusRequest request) {
        return ApiResponse.controller(clinicService.changeStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete clinic")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        return ApiResponse.controller(clinicService.delete(id));
    }

    @GetMapping("/validate-key")
    @ApiOperation(value = "Validate clinic secret key (no auth required)")
    public ResponseEntity<?> validateSecretKey(@RequestParam String secretKey) {
        return ApiResponse.controller(clinicService.validateSecretKey(secretKey));
    }
}
