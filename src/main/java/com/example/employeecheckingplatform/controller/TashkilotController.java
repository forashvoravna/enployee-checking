package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.OrgExamSummaryDto;
import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;
import com.example.employeecheckingplatform.dto.tashkilot.TashkilotCreateDto;
import com.example.employeecheckingplatform.dto.tashkilot.TashkilotResponseDto;
import com.example.employeecheckingplatform.dto.tashkilot.TashkilotUpdateDto;
import com.example.employeecheckingplatform.service.TashkilotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tashkilot")
@RequiredArgsConstructor
public class TashkilotController {
    private final TashkilotService service;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TashkilotCreateDto dto) {
        var saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/tashkilot/" + saved.id())).body(saved);
    }

    @GetMapping("/roots")
    public List<TashkilotResponseDto> roots() {
        return service.listRoots();
    }

    @GetMapping("/{id}/children")
    public List<TashkilotResponseDto> children(@PathVariable Long id) {
        return service.children(id);
    }

    @GetMapping("/{id}")
    public TashkilotResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/turi")
    public List<TashkilotResponseDto> turi(@RequestParam String turi) {
        return service.turi(turi);
    }

    @GetMapping("/all")
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(service.all());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TashkilotUpdateDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<?> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(service.getOrganizationResults(id));
    }

    @GetMapping("/okrug/results")
    public ResponseEntity<?> getOkrugResults() {
        return ResponseEntity.ok(service.getOkrugResults());
    }

    @GetMapping("/{id}/exams")
    public ResponseEntity<OrgExamSummaryDto> exams(@PathVariable("id") Long orgId) {
        return ResponseEntity.ok(service.examsOfOrg(orgId));
    }

    @GetMapping("/{orgId}/imtihon/{examId}/participants")
    public ResponseEntity<List<OrgExamParticipantProjection>> participants(@PathVariable Long orgId, @PathVariable Long examId) {
        return ResponseEntity.ok(service.findParticipants(orgId, examId));
    }
}
