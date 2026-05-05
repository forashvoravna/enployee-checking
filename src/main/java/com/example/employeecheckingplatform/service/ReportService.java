package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.dto.vedemost.VedemostFilterDTO;
import com.example.employeecheckingplatform.dto.vedemost.VedemostProjection;
import com.example.employeecheckingplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserRepository userRepository;

    public List<VedemostProjection> getVedemost(VedemostFilterDTO filter) {
        List<Long> units = filter.getUnitIds();
        List<Long> tests = filter.getTestIds();

        // 0 bormi yoki list bo'shmidigini aniqlaymiz
        boolean isAllUnits = units == null || units.isEmpty() || units.contains(0L);
        boolean isAllTests = tests == null || tests.isEmpty() || tests.contains(0L);

        // SQL xato bermasligi uchun list bo'sh bo'lsa dummy qiymat beramiz
        List<Long> finalUnits = isAllUnits ? List.of(-1L) : units;
        List<Long> finalTests = isAllTests ? List.of(-1L) : tests;

        return userRepository.findVedemostData(
                finalUnits,
                isAllUnits,
                finalTests,
                isAllTests,
                filter.getStartDate(),
                filter.getEndDate()
        );
    }
    // Excel faylini shakllantirish
    public byte[] generateVedemostExcel(List<VedemostProjection> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Imtihon Vedemosti");

            // 1. Stil: Sarlavha (Header) uchun
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // 2. Sarlavhalarni yaratish
            Row headerRow = sheet.createRow(0);
            String[] columns = {"№", "F.I.O", "Bo'linma", "Unvoni", "Imtihon nomi", "Natija (Stat)", "Ball", "Baho", "Sana"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. Ma'lumotlarni yozish
            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            for (VedemostProjection p : data) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(rowIdx - 1); // Tartib raqami
                row.createCell(1).setCellValue(p.getFullname());
                row.createCell(2).setCellValue(p.getUnitName());
                row.createCell(3).setCellValue(p.getRankName());
                row.createCell(4).setCellValue(p.getTestNomi());
                row.createCell(5).setCellValue(p.getNatijaStat()); // "4/20" kabi
                row.createCell(6).setCellValue(p.getBall() != null ? p.getBall() : 0);
                row.createCell(7).setCellValue(p.getBaho());
                row.createCell(8).setCellValue(p.getSana() != null ? p.getSana().format(formatter) : "");

                // Bahoga qarab rang berish (Ixtiyoriy: QONIQARSIZ bo'lsa qizilroq qilish mumkin)
            }

            // 4. Ustun o'lchamlarini avtomatik sozlash
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }}