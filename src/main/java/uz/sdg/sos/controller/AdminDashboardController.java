package uz.sdg.sos.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.sdg.sos.base.ApiResponse;
import uz.sdg.sos.service.AdminDashboardService;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Api(tags = "Admin Dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    @ApiOperation(value = "Get admin dashboard statistics")
    public ResponseEntity<?> getDashboard() {
        return ApiResponse.controller(adminDashboardService.getDashboard());
    }
}
