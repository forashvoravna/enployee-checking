package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.imtihon.*;
import com.example.employeecheckingplatform.service.ImtihonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/imtihon")
public class ImtihonController {

    private final ImtihonService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid ImtihonCreateDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ImtihonUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @PostMapping("/{imtihonId}/ruxsat/qoshish")
    public ResponseEntity<?> grantUsers(@PathVariable Long imtihonId, @RequestBody java.util.Set<Long> userIds) {
        return ResponseEntity.ok(service.grantUsers(imtihonId, userIds));
    }

    /**
     * Ixtiyoriy: to‘liq almashtirish
     */
    @PutMapping("/{imtihonId}/ruxsat")
    public ResponseEntity<?> setUsers(@PathVariable Long imtihonId, @RequestBody java.util.Set<Long> userIds) {
        return ResponseEntity.ok(service.setUsers(imtihonId, userIds));
    }

    /**
     * Ixtiyoriy: ruxsatdan olib tashlash
     */
    @PatchMapping("/{imtihonId}/ruxsat/revoke")
    public ResponseEntity<?> revokeUsers(@PathVariable Long imtihonId, @RequestBody java.util.Set<Long> userIds) {
        return ResponseEntity.ok(service.revokeUsers(imtihonId, userIds));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myExams(Principal principal) {
        return ResponseEntity.ok(service.myExams(principal.getName()));

    }
}
