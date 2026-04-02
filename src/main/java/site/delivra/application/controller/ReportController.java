package site.delivra.application.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import site.delivra.application.service.ReportService;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/reports/export/excel")
    @PreAuthorize("hasAnyAuthority('DISPATCHER', 'ADMIN', 'SUPER_ADMIN')")
    public void exportExcel(HttpServletResponse response) throws IOException {
        byte[] bytes = reportService.generateLast30DaysReport();
        String filename = "delivra-report-" + LocalDate.now() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}
