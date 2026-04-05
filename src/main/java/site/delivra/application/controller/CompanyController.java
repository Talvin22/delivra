package site.delivra.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.delivra.application.model.dto.company.CompanyDTO;
import site.delivra.application.model.dto.company.CompanyStatsDTO;
import site.delivra.application.model.dto.company.GlobalStatsDTO;
import site.delivra.application.model.dto.user.UserProfileDTO;
import site.delivra.application.model.enums.CompanyStatus;
import site.delivra.application.model.request.company.CompanyRegistrationRequest;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.service.CompanyService;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/companies/register")
    public ResponseEntity<DelivraResponse<UserProfileDTO>> register(
            @RequestBody @Valid CompanyRegistrationRequest request) {
        return ResponseEntity.ok(companyService.registerCompany(request));
    }

    @GetMapping("/companies/my")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DelivraResponse<CompanyDTO>> getMyCompany() {
        return ResponseEntity.ok(companyService.getMyCompany());
    }

    @GetMapping("/companies/my/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DISPATCHER')")
    public ResponseEntity<DelivraResponse<CompanyStatsDTO>> getMyStats() {
        return ResponseEntity.ok(companyService.getMyStats());
    }

    @GetMapping("/admin/companies")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<DelivraResponse<ArrayList<CompanyDTO>>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/admin/companies/{id}/stats")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<DelivraResponse<CompanyStatsDTO>> getCompanyStats(@PathVariable Integer id) {
        return ResponseEntity.ok(companyService.getCompanyStats(id));
    }

    @GetMapping("/admin/stats/overview")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<DelivraResponse<GlobalStatsDTO>> getGlobalStats() {
        return ResponseEntity.ok(companyService.getGlobalStats());
    }

    @PutMapping("/admin/companies/{id}/status")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<DelivraResponse<CompanyDTO>> updateStatus(
            @PathVariable Integer id,
            @RequestParam CompanyStatus status) {
        return ResponseEntity.ok(companyService.updateCompanyStatus(id, status));
    }
}
