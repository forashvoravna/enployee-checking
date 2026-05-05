package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.dto.AnswerDto;
import com.example.employeecheckingplatform.dto.urinish.UrinishDetailDto;
import com.example.employeecheckingplatform.entity.User;
import com.example.employeecheckingplatform.service.UrinishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/urinish")
@RequiredArgsConstructor
@Validated
public class UrinishController {

    private final UrinishService service;


    @PostMapping("/start")
    public ResponseEntity<?> start(Principal principal, @RequestParam Long imtihonId) {
        return ResponseEntity.ok(service.start(imtihonId, principal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<?> questions(@PathVariable Long id) {
        return ResponseEntity.ok(service.getQuestionsForUrinish(id));
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<?> answer(@PathVariable Long id, @RequestBody AnswerDto dto, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.answer(id, dto, user));
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<?> finish(@PathVariable Long id) {
        return ResponseEntity.ok(service.finish(id));
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<UrinishDetailDto>> myResults(Principal principal) {
        return ResponseEntity.ok(service.myResults(principal));
    }
}
