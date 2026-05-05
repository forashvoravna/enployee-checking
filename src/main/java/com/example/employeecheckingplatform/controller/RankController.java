package com.example.employeecheckingplatform.controller;

import com.example.employeecheckingplatform.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return rankService.get(id);
    }


    @GetMapping("")
    public ResponseEntity<?> getList() {
        return rankService.getRanksList();
    }

}
