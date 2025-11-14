package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.foydalanuvchi.FoydalanuvchiCreateDto;
import com.example.employeecheckingplatform.dto.foydalanuvchi.FoydalanuvchiUpdateDto;
import com.example.employeecheckingplatform.service.FoydalanuvchiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foydalanuvchi")
@RequiredArgsConstructor
public class FoydalanuvchiController {
    private final FoydalanuvchiService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody FoydalanuvchiCreateDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list());
    }
    @GetMapping("/faol")
    public ResponseEntity<?> faolo() {
        return ResponseEntity.ok(service.faol());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody FoydalanuvchiUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> setPassword(@PathVariable Long id, @RequestParam String parol) {
        return ResponseEntity.ok(service.setPassword(id, parol));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
