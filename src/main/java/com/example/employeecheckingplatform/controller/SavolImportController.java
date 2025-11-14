package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.service.SavolImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/savollar")
@RequiredArgsConstructor
public class SavolImportController {

    private final SavolImportService importService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fanId") Long fanId
    ) {
        int count = importService.importFromExcel(file, fanId);
        return ResponseEntity.ok("Yuklangan savollar soni: " + count);
    }
}
