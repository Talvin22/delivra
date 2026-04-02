package site.delivra.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.delivra.application.model.entities.DeliveryTask;
import site.delivra.application.model.enums.DeliveryTaskStatus;
import site.delivra.application.repository.DeliveryTaskRepository;
import site.delivra.application.service.ReportService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String UNASSIGNED = "Unassigned";

    private final DeliveryTaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateLast30DaysReport() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<DeliveryTask> tasks = taskRepository.findAllByDeletedFalseAndCreatedAfter(
                since, Sort.by("created").descending());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = buildHeaderStyle(workbook);

            buildTasksSheet(workbook, headerStyle, tasks);
            buildSummarySheet(workbook, headerStyle, tasks, since);
            buildDriverActivitySheet(workbook, headerStyle, tasks);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private void buildTasksSheet(XSSFWorkbook workbook, CellStyle headerStyle, List<DeliveryTask> tasks) {
        Sheet sheet = workbook.createSheet("Tasks");
        String[] headers = {"#", "ID", "Address", "Status", "Driver", "Created By", "Created", "Updated", "Start Time", "End Time"};
        createHeaderRow(sheet, headerStyle, headers);

        int rowIdx = 1;
        for (DeliveryTask t : tasks) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(rowIdx - 1);
            row.createCell(1).setCellValue(t.getId());
            row.createCell(2).setCellValue(t.getAddress());
            row.createCell(3).setCellValue(t.getStatus().name());
            row.createCell(4).setCellValue(t.getUser() != null ? t.getUser().getUsername() : UNASSIGNED);
            row.createCell(5).setCellValue(t.getCreatedBy());
            row.createCell(6).setCellValue(fmt(t.getCreated()));
            row.createCell(7).setCellValue(fmt(t.getUpdated()));
            row.createCell(8).setCellValue(fmt(t.getStartTime()));
            row.createCell(9).setCellValue(fmt(t.getEndTime()));
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void buildSummarySheet(XSSFWorkbook workbook, CellStyle headerStyle,
                                   List<DeliveryTask> tasks, LocalDateTime since) {
        Sheet sheet = workbook.createSheet("Summary");
        String[] headers = {"Metric", "Value"};
        createHeaderRow(sheet, headerStyle, headers);

        int rowIdx = 1;
        addRow(sheet, rowIdx++, "Report from", since.format(DT_FMT));
        addRow(sheet, rowIdx++, "Report to", LocalDateTime.now().format(DT_FMT));
        addRow(sheet, rowIdx++, "Total tasks", String.valueOf(tasks.size()));

        Map<DeliveryTaskStatus, Long> counts = tasks.stream()
                .collect(Collectors.groupingBy(DeliveryTask::getStatus, Collectors.counting()));

        for (DeliveryTaskStatus status : DeliveryTaskStatus.values()) {
            long count = counts.getOrDefault(status, 0L);
            double pct = tasks.isEmpty() ? 0.0 : (count * 100.0 / tasks.size());
            addRow(sheet, rowIdx++, status.name(), count + " (" + String.format("%.1f", pct) + "%)");
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void buildDriverActivitySheet(XSSFWorkbook workbook, CellStyle headerStyle, List<DeliveryTask> tasks) {
        Sheet sheet = workbook.createSheet("Driver Activity");
        String[] headers = {"Driver", "Total", "Completed", "In Progress", "Pending", "Canceled"};
        createHeaderRow(sheet, headerStyle, headers);

        Map<String, List<DeliveryTask>> byDriver = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getUser() != null ? t.getUser().getUsername() : UNASSIGNED));

        List<String> drivers = new ArrayList<>(byDriver.keySet());
        Collections.sort(drivers);

        int rowIdx = 1;
        for (String driver : drivers) {
            List<DeliveryTask> driverTasks = byDriver.get(driver);
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(driver);
            row.createCell(1).setCellValue(driverTasks.size());
            row.createCell(2).setCellValue(countByStatus(driverTasks, DeliveryTaskStatus.COMPLETED));
            row.createCell(3).setCellValue(countByStatus(driverTasks, DeliveryTaskStatus.IN_PROGRESS));
            row.createCell(4).setCellValue(countByStatus(driverTasks, DeliveryTaskStatus.PENDING));
            row.createCell(5).setCellValue(countByStatus(driverTasks, DeliveryTaskStatus.CANCELED));
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void createHeaderRow(Sheet sheet, CellStyle style, String[] headers) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void addRow(Sheet sheet, int rowIdx, String key, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
    }

    private CellStyle buildHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private long countByStatus(List<DeliveryTask> tasks, DeliveryTaskStatus status) {
        return tasks.stream().filter(t -> t.getStatus() == status).count();
    }

    private String fmt(LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : "";
    }
}
