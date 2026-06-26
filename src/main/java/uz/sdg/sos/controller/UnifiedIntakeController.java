package uz.sdg.sos.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.dto.intake.UnifiedIntakeRequest;
import uz.sdg.sos.service.UnifiedIntakeService;

@RestController
@RequestMapping("/api/v1/intake")
@RequiredArgsConstructor
@Api(tags = "Unified Intake")
public class UnifiedIntakeController {

    private final UnifiedIntakeService unifiedIntakeService;

    @PostMapping
    @ApiOperation(value = "Klinika yoki laboratoriya hodisasini qayd etish. " +
            "Backend secretKey orqali tashkilot turini o'zi aniqlaydi.")
    public ResponseEntity<?> intake(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey,
            @RequestBody UnifiedIntakeRequest request) {
        return ApiResponse.controller(unifiedIntakeService.processIntake(authHeader, secretKey, request));
    }
}
