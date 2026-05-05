package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.vedemost.VedemostFilterDTO;
import com.example.employeecheckingplatform.dto.vedemost.VedemostProjection;
import com.example.employeecheckingplatform.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/vedemost-json")
    public ResponseEntity<List<VedemostProjection>> getJson(@RequestBody VedemostFilterDTO filter) {
        return ResponseEntity.ok(reportService.getVedemost(filter));
    }

    @PostMapping("/vedemost-excel")
    public ResponseEntity<byte[]> downloadExcel(@RequestBody VedemostFilterDTO filter) throws IOException {
        // 1. Ma'lumotlarni bazadan olish
        List<VedemostProjection> data = reportService.getVedemost(filter);

        // 2. Excelga o'girish
        byte[] excelFile = reportService.generateVedemostExcel(data);

        // 3. Faylni qaytarish
        String fileName = "vedemost_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);
    }
}