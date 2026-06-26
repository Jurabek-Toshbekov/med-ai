package uz.sdg.sos.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.clinicapplication.ClinicApplicationRequest;
import uz.sdg.sos.entity.enums.ClinicApplicationStatus;
import uz.sdg.sos.service.ClinicApplicationService;

import java.util.UUID;

@Api(tags = "Clinic Application")
@RestController
@RequestMapping("/api/v1/clinic-applications")
@RequiredArgsConstructor
public class ClinicApplicationController {

    private final ClinicApplicationService clinicApplicationService;

    @ApiOperation(value = "Submit clinic registration request (public, no auth required)")
    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody ClinicApplicationRequest request) {
        return ApiResponse.controller(clinicApplicationService.apply(request));
    }

    @ApiOperation(value = "Get all applications (ADMIN). Filter by status: PENDING, APPROVED, REJECTED")
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) ClinicApplicationStatus status) {
        return ApiResponse.controller(clinicApplicationService.getAll(status));
    }

    @ApiOperation(value = "Approve clinic application (ADMIN). Creates Clinic + User with secret key.")
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable UUID id) {
        return ApiResponse.controller(clinicApplicationService.approve(id));
    }

    @ApiOperation(value = "Reject clinic application (ADMIN)")
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable UUID id) {
        return ApiResponse.controller(clinicApplicationService.reject(id));
    }
}
