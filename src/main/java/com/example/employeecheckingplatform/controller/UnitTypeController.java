package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.OrgExamSummaryDto;
import com.example.employeecheckingplatform.dto.projection.OrgExamParticipantProjection;
import com.example.employeecheckingplatform.dto.unit.UnitCreateDto;
import com.example.employeecheckingplatform.dto.unit.UnitResponseDto;
import com.example.employeecheckingplatform.dto.unit.UnitTypeCreateDto;
import com.example.employeecheckingplatform.dto.unit.UnitUpdateDto;
import com.example.employeecheckingplatform.service.UnitService;
import com.example.employeecheckingplatform.service.UnitTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/unit_type")
@RequiredArgsConstructor
public class UnitTypeController {
    private final UnitTypeService service;


    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }


    @GetMapping("/all")
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(service.all());
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

}
