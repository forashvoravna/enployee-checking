package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.OrgExamSummaryDto;
import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;

import com.example.employeecheckingplatform.dto.unit.UnitCreateDto;
import com.example.employeecheckingplatform.dto.unit.UnitResponseDto;
import com.example.employeecheckingplatform.dto.unit.UnitUpdateDto;
import com.example.employeecheckingplatform.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/unit")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService service;

    @GetMapping("/roots")
    public List<UnitResponseDto> roots() {
        return service.listRoots();
    }

    @GetMapping("/{id}/children")
    public List<UnitResponseDto> children(@PathVariable Long id) {
        return service.children(id);
    }

    @GetMapping("/{id}")
    public UnitResponseDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/turi")
    public List<UnitResponseDto> turi(@RequestParam Long branchTypeId) {
        return service.turi(branchTypeId);
    }

    @GetMapping("/all")
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(service.all());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UnitUpdateDto dto) {
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
