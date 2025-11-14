package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.fan.FanCreateDto;
import com.example.employeecheckingplatform.dto.fan.FanUpdateDto;
import com.example.employeecheckingplatform.service.FanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fan")
@RequiredArgsConstructor
public class FanController {
    private final FanService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody FanCreateDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/faol")
    public ResponseEntity<?> faol() {
        return ResponseEntity.ok(service.faol());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody FanUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }
}
