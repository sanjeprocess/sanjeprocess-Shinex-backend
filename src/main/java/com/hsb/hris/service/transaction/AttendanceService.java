package com.hsb.hris.service.transaction;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface AttendanceService {
    Map<String, Object> uploadExcel(MultipartFile file, String businessCenter) throws Exception;
    Map<String, Object> autoUpdate(String year, String month, String businessCenter);
    List<Map<String, Object>> queryAnalyzer(Map<String, Object> filters);
}
