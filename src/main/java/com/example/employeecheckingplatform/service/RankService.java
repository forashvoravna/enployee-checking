package com.example.employeecheckingplatform.service;


import com.example.employeecheckingplatform.entity.Rank;
import com.example.employeecheckingplatform.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankRepository rankRepository;


    public ResponseEntity<?> get(Long id) {
        if (id == null || id <= 0) return ResponseEntity.badRequest().body("Id kiritilmadi!!!");
        Rank rank = rankRepository.findByIdAndDeleted(id, false).orElse(null);
        if (rank == null) return ResponseEntity.badRequest().body("Harbiy unvon topilmadi!!!");
        return ResponseEntity.ok(rank);
    }

    public ResponseEntity<?> getRanksList() {
        return ResponseEntity.ok(rankRepository.findAllByDeleted(false));
    }
}
