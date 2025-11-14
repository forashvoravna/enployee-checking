package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.savol.SavolCreateDto;
import com.example.employeecheckingplatform.dto.savol.SavolUpdateDto;
import com.example.employeecheckingplatform.service.SavolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/savol")
@RequiredArgsConstructor
public class SavolController {
    private final SavolService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SavolCreateDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/fan/{fanId}")
    public ResponseEntity<?> listByFan(@PathVariable Long fanId) {
        return ResponseEntity.ok(service.listByFan(fanId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SavolUpdateDto dto) {
        return ResponseEntity.ok(service.updateOnlyExistingVariants(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
