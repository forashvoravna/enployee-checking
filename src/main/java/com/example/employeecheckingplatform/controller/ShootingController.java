package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.ShootingResultDto;
import com.example.employeecheckingplatform.entity.ShootingResult;
import com.example.employeecheckingplatform.service.ShootingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shooting")
@RequiredArgsConstructor
public class ShootingController {

    private final ShootingService shootingService;

    // Saqlash
    @PostMapping("/bulk-save")
    public ResponseEntity<?> save(@RequestBody List<ShootingResultDto> results) {
        shootingService.saveResults(results);
        return ResponseEntity.ok("Natijalar muvaffaqiyatli saqlandi.");
    }

    // Barcha natijalar (Teskari tartibda)
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<?> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(shootingService.getAllByUserId(userId));
    }

    // Eng yaxshi natija
    @GetMapping("/user/{userId}/best")
    public ResponseEntity<?> getBest(@PathVariable Long userId) {
        return ResponseEntity.ok(shootingService.getBestResultByUserId(userId));
    }

    @GetMapping("/all-best")
    public ResponseEntity<?> getAllBestResults() {
        return ResponseEntity.ok(shootingService.getBestResultsForAllUsers());
    }
}