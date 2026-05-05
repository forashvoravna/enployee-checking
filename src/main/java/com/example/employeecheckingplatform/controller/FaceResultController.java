package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.service.FaceResultService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/face-result")
@RequiredArgsConstructor
public class FaceResultController {

    private final FaceResultService service;

    @PostMapping
    public ResponseEntity<?> getResult(@RequestBody String base64Image) throws JsonProcessingException {
        ResponseEntity<?> resp = service.recognizeAndGetLastGrade(base64Image);
        System.out.println(resp);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/shoots")
    public ResponseEntity<?> getResultWithShoots(@RequestBody String base64Image) throws JsonProcessingException {
        ResponseEntity<?> resp = service.recognizeAndGetLastGradeAndShoot(base64Image);
        System.out.println(resp);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/by-jshshir/{jshshir}")
    public ResponseEntity<?> byJshshir(@PathVariable String jshshir) {
        return service.recognizeByJshshir(jshshir);
    }

    @GetMapping("/by-jshshir-and-shoot/{jshshir}")
    public ResponseEntity<?> byJshshirAndShoot(@PathVariable String jshshir) {
        return service.recognizeByJshshirWithShoot(jshshir);
    }
}
