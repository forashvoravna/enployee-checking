package com.example.employeecheckingplatform.service;

import com.example.employeecheckingplatform.entity.Fan;
import com.example.employeecheckingplatform.entity.Savol;
import com.example.employeecheckingplatform.entity.Variant;
import com.example.employeecheckingplatform.repository.FanRepository;
import com.example.employeecheckingplatform.repository.SavolRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavolImportService {

    private final SavolRepository savolRepo;
    private final FanRepository fanRepo;

    @Transactional
    public int importFromExcel(MultipartFile file, Long fanId) {
        try (InputStream is = file.getInputStream()) {

            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);

            Fan fan = fanRepo.findById(fanId)
                    .orElseThrow(() -> new RuntimeException("Fan topilmadi: " + fanId));

            int saved = 0;

            // 0-qator — header, 1-qatordan boshlaymiz
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String savolText = getString(row.getCell(0));
                if (savolText == null || savolText.isBlank()) continue;

                String variantA = getString(row.getCell(1));
                String variantB = getString(row.getCell(2));
                String variantC = getString(row.getCell(3));
                String variantD = getString(row.getCell(4));
                String correct  = getString(row.getCell(5));  // A/B/C/D

                // --- Savol yaratamiz ---
                Savol savol = Savol.builder()
                        .fan(fan)
                        .matn(savolText)
                        .build();

                List<Variant> variants = new ArrayList<>();

                // A
                if (variantA != null && !variantA.isBlank()) {
                    variants.add(Variant.builder()
                            .savol(savol)
                            .matn(variantA)
                            .togri("A".equalsIgnoreCase(correct))
                            .build());
                }

                // B
                if (variantB != null && !variantB.isBlank()) {
                    variants.add(Variant.builder()
                            .savol(savol)
                            .matn(variantB)
                            .togri("B".equalsIgnoreCase(correct))
                            .build());
                }

                // C
                if (variantC != null && !variantC.isBlank()) {
                    variants.add(Variant.builder()
                            .savol(savol)
                            .matn(variantC)
                            .togri("C".equalsIgnoreCase(correct))
                            .build());
                }

                // D
                if (variantD != null && !variantD.isBlank()) {
                    variants.add(Variant.builder()
                            .savol(savol)
                            .matn(variantD)
                            .togri("D".equalsIgnoreCase(correct))
                            .build());
                }

                // Savolga variantlar ro‘yxatini o‘rnatamiz
                savol.setVariantlar(variants);

                // Cascade.ALL bo‘lgani uchun faqat savolni saqlaymiz — variantlar ham saqlanadi
                savolRepo.save(savol);
                saved++;
            }

            return saved;

        } catch (Exception e) {
            throw new RuntimeException("Excel import xatosi: " + e.getMessage(), e);
        }
    }

    private String getString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }
}
