package uz.sdg.sos.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.medicalevent.MedicalEventIntakeRequest;
import uz.sdg.sos.service.MedicalEventService;

@RestController
@RequestMapping("/api/v1/medical-events")
@RequiredArgsConstructor
@Api(tags = "Medical Event Intake")
public class MedicalEventController {

    private final MedicalEventService medicalEventService;

    @PostMapping("/intake")
    @ApiOperation(value = "Receive medical event from clinic (JWT + X-Secret-Key required)")
    public ResponseEntity<?> intake(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey,
            @RequestBody MedicalEventIntakeRequest request) {

        ApiResponse<?> response = medicalEventService.processIntake(secretKey, request);

        if (response.isSuccess()) {
            return ResponseEntity.status(201).body(response);
        }

        int status = resolveHttpStatus(response.getMessage());
        return ResponseEntity.status(status).body(response);
    }

    private int resolveHttpStatus(String errorCode) {
        if (errorCode == null) return 409;
        switch (errorCode) {
            case "INVALID_JWT":
            case "KEY_NOT_FOUND":
            case "KEY_EXPIRED":
                return 401;
            case "CLINIC_INACTIVE":
                return 403;
            case "INVALID_JSHSHIR":
                return 400;
            case "PATIENT_NOT_IN_DMED":
                return 404;
            default:
                return 409;
        }
    }
}
