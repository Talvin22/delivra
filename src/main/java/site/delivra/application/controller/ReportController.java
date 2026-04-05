package site.delivra.application.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.delivra.application.service.ReportService;
import site.delivra.application.utils.ApiUtils;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ApiUtils apiUtils;

    @GetMapping("/reports/export/excel")
    @PreAuthorize("hasAnyAuthority('DISPATCHER', 'ADMIN', 'SUPER_ADMIN')")
    public void exportExcel(HttpServletResponse response,
                            @RequestParam(required = false) Integer companyId) throws IOException {
        Integer effectiveCompanyId = apiUtils.getCompanyIdFromAuthentication();
        if (effectiveCompanyId == null && companyId != null) {
            effectiveCompanyId = companyId;
        }
        byte[] bytes = reportService.generateLast30DaysReport(effectiveCompanyId);
        String filename = "delivra-report-" + LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
