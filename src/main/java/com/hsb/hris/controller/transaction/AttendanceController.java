package com.hsb.hris.controller.transaction;

import com.hsb.hris.service.transaction.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transaction/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) { this.service = service; }

    @PostMapping("/excel-upload")
    public ResponseEntity<Map<String,Object>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "businessCenter", required = false) String businessCenter) throws Exception {
        Map<String,Object> res = service.uploadExcel(file, businessCenter);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/auto-update")
    public ResponseEntity<Map<String,Object>> autoUpdate(@RequestParam String year, @RequestParam String month,
                                                          @RequestParam(required = false) String businessCenter) {
        Map<String,Object> res = service.autoUpdate(year, month, businessCenter);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/query-analyzer")
    public ResponseEntity<List<Map<String,Object>>> queryAnalyzer(@RequestBody Map<String,Object> filters) {
        List<Map<String,Object>> res = service.queryAnalyzer(filters);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/excel-template")
    public ResponseEntity<org.springframework.core.io.ByteArrayResource> downloadTemplate() throws Exception {
        String[] headers = new String[]{
                "Att_Year", "Att_Month", "EPF_No", "Plant_Code", "Day_In (yyyy-MM-dd)", "Time_In (HH:mm)",
                "Day_Out (yyyy-MM-dd HH:mm)", "Time_Out (HH:mm)", "Half_Day (0/1)", "Day_Shift (Y/N)",
                "Second_Shift (Y/N)", "Night_Shift (Y/N)", "Full_Night (Y/N)", "Normal_Day (Y/N)",
                "Saturday_Poya (Y/N)", "Special_Day (Y/N)", "Statutory_Holidays (0/1)", "No_Of_Meal", "Business_Center"
        };

        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("AttendanceTemplate");
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 20 * 256);
            }
            wb.write(out);
            byte[] bytes = out.toByteArray();
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(bytes);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-template.xlsx")
                    .contentLength(bytes.length)
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        }
    }
}
